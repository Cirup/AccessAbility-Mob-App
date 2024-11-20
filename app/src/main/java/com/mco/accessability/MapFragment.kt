package com.mco.accessability

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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        val markerAdderButton: Button = view.findViewById(R.id.markerAdderModeButton)
        markerAdderButton.setOnClickListener {
            isMarkerAdderModeEnabled = !isMarkerAdderModeEnabled
            markerAdderButton.text = if (isMarkerAdderModeEnabled) "Done" else "+"
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

            val currentUser = auth.currentUser // Get the current user
            if (currentUser == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            val review = ReviewModel(
                author = currentUser.uid,
                notes = noteText,
                imageId = R.drawable.placeholder, // Placeholder image ID
                rating = 0 // Default rating
            )

            val markerData = MarkerData(
                nameOfPlace = markerName,
                lat = latLng.latitude,
                lng = latLng.longitude,
                imageres = R.drawable.placeholder,
                notes = listOf(review)
            )

            val newMarkerRef = database.push()
            newMarkerRef.setValue(markerData).addOnSuccessListener {
                addMarkerToMap(markerData, newMarkerRef.key)
                dialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add marker", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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

    private fun addMarkerToMap(markerData: MarkerData, markerId: String?) {
        val markerOptions = MarkerOptions()
            .position(LatLng(markerData.lat, markerData.lng))
            .title(markerData.nameOfPlace)

        val marker = googleMap?.addMarker(markerOptions)
        marker?.tag = markerId
        marker?.let { markers.add(it) }
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

    private fun showMarkerPopup(markerData: MarkerData) {
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
}
