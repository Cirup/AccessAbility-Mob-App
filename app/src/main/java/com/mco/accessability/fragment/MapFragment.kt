package com.mco.accessability.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.models.MarkerData
import com.mco.accessability.R
import com.mco.accessability.models.ReviewModel

class MapFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var isMarkerAdderModeEnabled = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val markers: MutableList<Marker> = mutableListOf()
    private lateinit var database: DatabaseReference
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var auth: FirebaseAuth // Add FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
        database = FirebaseDatabase.getInstance().getReference("markers")

        val markerAdderButton: ImageView = view.findViewById(R.id.markerAdderModeButton)
        markerAdderButton.setOnClickListener {
            isMarkerAdderModeEnabled = !isMarkerAdderModeEnabled
            if (isMarkerAdderModeEnabled) {
                markerAdderButton.setImageResource(R.drawable.cancel_add_marker)
            } else {
                markerAdderButton.setImageResource(R.drawable.rate_location_icon)
            }
        }



        val zoomInButton: Button = view.findViewById(R.id.zoomInButton)
        val zoomOutButton: Button = view.findViewById(R.id.zoomOutButton)
        zoomInButton.setOnClickListener { googleMap?.animateCamera(CameraUpdateFactory.zoomIn()) }
        zoomOutButton.setOnClickListener { googleMap?.animateCamera(CameraUpdateFactory.zoomOut()) }

        val searchEditText: EditText = view.findViewById(R.id.searchMarkerEditText)
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                val query = searchEditText.text.toString()
                searchMarker(query)
                true
            } else {
                false
            }
        }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        requestLocationPermission()

        googleMap?.setOnMapClickListener { latLng ->
            if (isMarkerAdderModeEnabled) {
                showAddMarkerDialog(latLng)
            } else {
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }

        googleMap?.setOnMarkerClickListener { marker ->
            val markerId = marker.tag as? String
            markerId?.let { fetchMarkerDetails(it) }
            true
        }

        loadMarkersFromFirebase()
    }

    private fun showAddMarkerDialog(latLng: LatLng) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(requireContext(), "You need to log in to add a marker", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserEmail = currentUser.email ?: "Anonymous"

        getUsernameFromFirestore(currentUserEmail) { username ->
            if (username == null) {
                Toast.makeText(requireContext(), "Failed to retrieve username", Toast.LENGTH_SHORT).show()
                return@getUsernameFromFirestore
            }

            val dialogView = layoutInflater.inflate(R.layout.add_marker_details_pop_up, null)
            val etMarkerName = dialogView.findViewById<EditText>(R.id.et_marker_name)
            val etNote = dialogView.findViewById<EditText>(R.id.et_note)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

            dialogView.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
                val markerName = etMarkerName.text.toString().ifBlank { "New Marker" }
                val noteText = etNote.text.toString()

                val markerData = MarkerData(
                    nameOfPlace = markerName,
                    lat = latLng.latitude,
                    lng = latLng.longitude,
                    imageres = R.drawable.placeholder // Placeholder image ID
                )

                val db = FirebaseFirestore.getInstance()

                if (noteText.isNotBlank()) {
                    val review = ReviewModel(
                        author = username,  // Use the retrieved username
                        notes = noteText,
                        imageId = R.drawable.placeholder, // Placeholder image ID
                        rating = 0 // Default rating
                    )

                    db.collection("review")
                        .add(review)
                        .addOnSuccessListener { reviewRef ->
                            val reviewId = reviewRef.id
                            Log.d("MapFragment", "Review added with ID: $reviewId")

                            markerData.notes = listOf(reviewId)

                            db.collection("marker")
                                .add(markerData)
                                .addOnSuccessListener { markerRef ->
                                    Log.d("MapFragment", "Marker added with ID: ${markerRef.id}")
                                    addMarkerToMap(markerData, markerRef.id)
                                    dialog.dismiss()
                                }
                                .addOnFailureListener { e ->
                                    Log.d("MapFragment", "Failed to add marker: ${e.message}")
                                    Toast.makeText(requireContext(), "Failed to add marker", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.d("MapFragment", "Failed to add review: ${e.message}")
                            Toast.makeText(requireContext(), "Failed to add review", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    db.collection("marker")
                        .add(markerData)
                        .addOnSuccessListener { markerRef ->
                            Log.d("MapFragment", "Marker without review added with ID: ${markerRef.id}")
                            addMarkerToMap(markerData, markerRef.id)
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            Log.d("MapFragment", "Failed to add marker: ${e.message}")
                            Toast.makeText(requireContext(), "Failed to add marker", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }



    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableUserLocation()
        }
    }


    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                }
            }
        }
    }

    private fun loadMarkersFromFirebase() {
        Log.d("MapFragment", "Loading markers from Firebase...") // Log the function call
        val db = FirebaseFirestore.getInstance()

        db.collection("marker")
            .get()
            .addOnSuccessListener { result ->
                markers.clear() // Clear any existing markers on the map
                Log.d("MapFragment", "Markers fetched: ${result.size()}")
                for (document in result) {
                    val markerData = document.toObject(MarkerData::class.java)
                    markerData?.let {
                        addMarkerToMap(it, document.id)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapFragment", "Error loading markers: ${exception.message}")
                Toast.makeText(requireContext(), "Failed to load markers", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addMarkerToMap(markerData: MarkerData, markerId: String?) {
        val markerOptions = MarkerOptions()
            .position(LatLng(markerData.lat, markerData.lng))
            .title(markerData.nameOfPlace)

        val marker = googleMap?.addMarker(markerOptions)
        marker?.tag = markerId
        marker?.let { markers.add(it) }
    }


    private fun fetchMarkerDetails(markerId: String) {
        Log.d("MapFragment", "fetchMarkerDetails")

        val db = FirebaseFirestore.getInstance()
        db.collection("marker")  // Reference to the "markers" collection
            .document(markerId)    // Reference to the specific marker document
            .get()  // Fetch the document
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val markerData = document.toObject(MarkerData::class.java)
                    markerData?.let { showMarkerPopup(it) }
                } else {
                    Toast.makeText(requireContext(), "Marker not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to fetch marker details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showMarkerPopup(markerData: MarkerData) {
        Log.d("MapFragment", "showMarkerPopup")

        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_dialog, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<TextView>(R.id.markername).text = markerData.nameOfPlace
        //val reviewsText = markerData.notes.joinToString("\n")
        //bottomSheetView.findViewById<TextView>(R.id.re).text = reviewsText

        bottomSheetDialog.show()
    }

    private fun searchMarker(query: String) {
        database.orderByChild("nameOfPlace").equalTo(query).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.first().getValue(MarkerData::class.java)?.let {
                        val markerPosition = LatLng(it.lat, it.lng)
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 15f))
                    }
                } else {
                    Toast.makeText(requireContext(), "Marker not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUsernameFromFirestore(currentUserEmail: String, onComplete: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("email", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Assuming each email is unique and only one document will match
                    val document = querySnapshot.documents[0]
                    val username = document.getString("username")
                    onComplete(username) // Pass the retrieved username to the callback
                } else {
                    Log.d("MapFragment", "No user found with email: $currentUserEmail")
                    onComplete(null) // No user found
                }
            }
            .addOnFailureListener { e ->
                Log.d("MapFragment", "Failed to retrieve username: ${e.message}")
                onComplete(null) // Handle failure
            }
    }

}