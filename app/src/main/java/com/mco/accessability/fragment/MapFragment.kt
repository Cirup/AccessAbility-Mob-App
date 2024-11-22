package com.mco.accessability.fragment

import android.Manifest
import com.mco.accessability.models.MarkerData
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.mco.accessability.DataHelper
import com.mco.accessability.R
import com.mco.accessability.SharedViewModel
import com.mco.accessability.adapter.DialogPostAdapter
import com.mco.accessability.databinding.BottomDialogBinding
import com.mco.accessability.databinding.FragmentMapBinding
import com.mco.accessability.models.ReviewModel

class MapFragment(private val sharedViewModel: SharedViewModel) : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var isMarkerAdderModeEnabled = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var allMarkerData: List<MarkerData> = emptyList() // holds marker data (like their likes and etc)
    private val markers: MutableList<Marker> = mutableListOf()  // List to hold markers (google api marker)
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomDialogBinding: BottomDialogBinding
    private lateinit var dialogPostAdapter: DialogPostAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth // Add FirebaseAuth



    private lateinit var mapbinding: FragmentMapBinding


    // Access the same SharedViewModel as the activity
    //private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get Database Reference
        auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
        database = FirebaseDatabase.getInstance().getReference("markers")

        // MapBinding
        mapbinding = FragmentMapBinding.inflate(inflater)
        val view = mapbinding.root
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Find the marker adder mode button and set the click listener
        val markerAdderButton: ImageView = mapbinding.markerAdderModeButton
        markerAdderButton.setOnClickListener {
            isMarkerAdderModeEnabled = !isMarkerAdderModeEnabled
            if (isMarkerAdderModeEnabled) {
                markerAdderButton.setImageResource(R.drawable.cancel_add_marker)
            } else {
                markerAdderButton.setImageResource(R.drawable.rate_location_icon)
            }
        }

        // Find zoom buttons and set their click listeners
        val zoomInButton: Button = mapbinding.zoomInButton
        val zoomOutButton: Button = mapbinding.zoomOutButton

        zoomInButton.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }

        zoomOutButton.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }

        // Find the search EditText and set a listener
        val searchEditText: EditText = mapbinding.searchMarkerEditText
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            // Check if the action ID corresponds to a search action or if the Enter key was pressed
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                val query = searchEditText.text.toString()
                searchMarker(query)
                true
            } else {
                false
            }
        }


        //sharedViewModel.logMarkerData()
        return view
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d("MapFragment", "Map is ready")

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
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            Toast.makeText(requireContext(), "You need to log in to add a marker", Toast.LENGTH_SHORT).show()
            return
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

//            val currentUser = sharedPreferences.getString("username", null)  // Retrieve username
            if (currentUser == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            val markerData = MarkerData(
                nameOfPlace = markerName,
                lat = latLng.latitude,
                lng = latLng.longitude,
                imageResId = R.drawable.placeholder // Placeholder image ID
            )

            val db = FirebaseFirestore.getInstance()

            if (noteText.isNotBlank()) {
                // Create a new review
                val review = ReviewModel(
                    author = currentUser,  // Use the stored username
                    notes = noteText,
                    imageId = R.drawable.placeholder, // Placeholder image ID
                    rating = 0 // Default rating
                )

                // Add review to Firestore
                db.collection("review")
                    .add(review)
                    .addOnSuccessListener { reviewRef ->
                        val reviewId = reviewRef.id
                        Log.d("MapFragment", "Review added with ID: $reviewId")

                        // Associate the review ID with the marker
                        markerData.notes = listOf(reviewId)

                        // Add marker data to Firestore
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
                // Add marker without review if note is empty
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

    private fun loadMarkersFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                markers.clear()
                snapshot.children.forEach { data ->
                    val markerData = data.getValue(MarkerData::class.java)
                    markerData?.let { addMarkerToMap(it, data.key) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load markers", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchMarkerDetails(markerId: String) {
        database.child(markerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val markerData = snapshot.getValue(MarkerData::class.java)
                markerData?.let { showMarkerPopup(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to fetch marker details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMarkerToMap(markerData: MarkerData, markerId: String?) {
        val markerOptions = MarkerOptions()
            .position(LatLng(markerData.lat, markerData.lng))
            .title(markerData.nameOfPlace)

        val marker = googleMap?.addMarker(markerOptions)
        marker?.tag = markerId
        marker?.let { markers.add(it) }
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



    // moves camera to current location
    fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true

            // Get the current location and move the camera
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Move the camera to the current location
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)) // Zoom level 15
                }
            }
        }
    }

    // pop up for ask request of permission
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableUserLocation()
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, enable location and move camera
//                enableUserLocation()
//            } else {
//                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


//    private fun addMarker(markerData: MarkerData) {
//        // Create MarkerOptions using the data from markerData
//        val markerOptions = MarkerOptions()
//            .position(LatLng(markerData.lat, markerData.lng))
//            .title(markerData.name)
//
//        // Add the marker to the Google Map
//        val marker = googleMap?.addMarker(markerOptions)
//
//        // Update the marker's tag with the associated com.mco.accessability.models.MarkerData
//        marker?.tag = markerData
//
//        // Update the markerID after it has been created
//        markerData.markerID = marker?.id ?: ""
//
//        // Add the marker to the markers list
//        marker?.let { markers.add(it) }
//    }

    private fun showMarkerPopup(markerData: MarkerData) {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomDialogBinding = BottomDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomDialogBinding.root)

        bottomDialogBinding.markername.text = markerData.nameOfPlace

        // Set up RecyclerView using ViewBinding
        bottomDialogBinding.rcvDialog.layoutManager = LinearLayoutManager(context)
        dialogPostAdapter = DialogPostAdapter(DataHelper.loadTweetData())
        bottomDialogBinding.rcvDialog.adapter = dialogPostAdapter

        bottomSheetDialog.show()
    }

}