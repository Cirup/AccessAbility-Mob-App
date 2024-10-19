package com.mco.frame

import android.Manifest
import MarkerData
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mco.frame.databinding.BottomDialogBinding

class MapFragment(private val sharedViewModel: SharedViewModel) : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var isMarkerAdderModeEnabled = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var allMarkerData: List<MarkerData> = emptyList() // holds marker data (like their likes and etc)
    private val markers: MutableList<Marker> = mutableListOf()  // List to hold markers (google api marker)
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var dialogPostAdapter: DialogPostAdapter


    // Access the same SharedViewModel as the activity
    //private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Find the marker adder mode button and set the click listener
        val markerAdderButton: Button = view.findViewById(R.id.markerAdderModeButton)
        markerAdderButton.setOnClickListener {
            isMarkerAdderModeEnabled = !isMarkerAdderModeEnabled
            if (isMarkerAdderModeEnabled) {
                markerAdderButton.text = "Done"
            } else {
                markerAdderButton.text = "+"
            }
        }

        // Find zoom buttons and set their click listeners
        val zoomInButton: Button = view.findViewById(R.id.zoomInButton)
        val zoomOutButton: Button = view.findViewById(R.id.zoomOutButton)

        zoomInButton.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }

        zoomOutButton.setOnClickListener {
            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }

        // Find the search EditText and set a listener
        val searchEditText: EditText = view.findViewById(R.id.searchMarkerEditText)
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
        googleMap = map

        // Check if location permissions are granted
        requestLocationPermission()
        /*
        Code to supposedly move camera to current location after giving permission but not working for now
        will debug later on
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true

            // Use FusedLocationProviderClient to get the last known location
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLocation = LatLng(it.latitude, it.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)) // Zoom level 15
                    }
                }
        } else {
            requestLocationPermission()
        }*/

        Log.d("MapFragment", "Loading markers from ViewModel...")

        sharedViewModel.logMarkerData()

        allMarkerData = sharedViewModel.getAllMarkerData()
        allMarkerData.forEach { markerData ->
            addMarker(markerData) // Call the addMarker function
        }

        for (x in 1..5){
            val markerData = MarkerData(
                name = "Marker " + x,
                imageResId = R.drawable.placeholder, // Placeholder image
                rating = 0,
                lat = 14.5995 + x*4,
                lng = 120.9842 + x*4,
                markerID = ""
            )

            addMarker(markerData)
            sharedViewModel.addMarkerData(markerData.markerID, markerData)
        }

        googleMap?.setOnMapClickListener { latLng ->
            if (isMarkerAdderModeEnabled) {
                // Inflate and show the dialog for adding marker details
                val dialogView = layoutInflater.inflate(R.layout.add_marker_details_pop_up, null)
                val etMarkerName = dialogView.findViewById<EditText>(R.id.et_marker_name)
                val rgRating = dialogView.findViewById<RadioGroup>(R.id.rg_rating)
                val etNote = dialogView.findViewById<EditText>(R.id.et_note)

                // Create an AlertDialog
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setCancelable(false) // Prevent dismiss on outside touch
                    .create()

                // Set up the Confirm button listener
                dialogView.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
                    val markerName = etMarkerName.text.toString().ifBlank { "New Marker" }
                    val selectedRatingId = rgRating.checkedRadioButtonId
                    val rating = when (selectedRatingId) {
                        R.id.rb_star_1 -> 1
                        R.id.rb_star_2 -> 2
                        R.id.rb_star_3 -> 3
                        R.id.rb_star_4 -> 4
                        R.id.rb_star_5 -> 5
                        else -> 0 // Default rating if none is selected
                    }
                    val note = etNote.text.toString()

                    // Create MarkerData and add it
                    val markerData = MarkerData(
                        name = markerName,
                        imageResId = R.drawable.placeholder, // Placeholder image
                        rating = rating,
                        lat = latLng.latitude,
                        lng = latLng.longitude,
                        markerID = "",
                        notes = listOf(note) // Add the note to MarkerData
                    )

                    // Add marker and save to sharedViewModel
                    addMarker(markerData)
                    sharedViewModel.addMarkerData(markerData.markerID, markerData)

                    dialog.dismiss() // Close the dialog after confirming
                }

                // Set up the Cancel button listener
                dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
                    dialog.dismiss() // Close the dialog without adding a marker
                }

                dialog.show()
            } else {
                // Move the map camera to the clicked location if marker adder mode is off
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }


        googleMap?.setOnMarkerClickListener { marker ->
            val markerData = marker.tag as? MarkerData
            markerData?.let {
                showMarkerPopup(marker, it)
            }
            true
        }
    }

    private fun searchMarker(query: String) {
        allMarkerData = sharedViewModel.getAllMarkerData()
        Log.d("MapFragment", "Searching for marker with query: $query") // Log the search query

        // Find the marker data that matches the query
        val markerData = allMarkerData.find {
            it.name.equals(query, ignoreCase = true) || it.markerID.equals(query, ignoreCase = true)
        }

        if (markerData != null) {
            Log.d("MapFragment", "Marker found: $markerData") // Log the found marker data
            val markerPosition = LatLng(markerData.lat, markerData.lng)

            // Animate camera to the marker's position
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 15f))

            // Find the marker in the markers list
            markers.find { marker -> marker.tag == markerData }?.let { marker ->
                Log.d("MapFragment", "Displaying popup for marker: ${marker.title}") // Log the marker title being displayed
                showMarkerPopup(marker, markerData)
            } ?: Log.d("MapFragment", "Marker not found in the markers list.") // Log if the marker is not found
        } else {
            Log.d("MapFragment", "Marker not found for query: $query") // Log if no marker matches the query
            Toast.makeText(requireContext(), "Marker not found", Toast.LENGTH_SHORT).show()
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location and move camera
                enableUserLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun addMarker(markerData: MarkerData) {
        // Create MarkerOptions using the data from markerData
        val markerOptions = MarkerOptions()
            .position(LatLng(markerData.lat, markerData.lng))
            .title(markerData.name)

        // Add the marker to the Google Map
        val marker = googleMap?.addMarker(markerOptions)

        // Update the marker's tag with the associated MarkerData
        marker?.tag = markerData

        // Update the markerID after it has been created
        markerData.markerID = marker?.id ?: ""

        // Add the marker to the markers list
        marker?.let { markers.add(it) }
    }

    private fun showMarkerPopup(marker: Marker, markerData: MarkerData) {
//      // Custom popup dialog with the marker's name, image, and action buttons

        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_dialog, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        // Set up the close button
        val closeButton = bottomSheetView.findViewById<ImageView>(R.id.imageView)
        closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Set up RecyclerView
        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.rcv_dialog)
        recyclerView.layoutManager = LinearLayoutManager(context)
        dialogPostAdapter = DialogPostAdapter(DataHelper.loadTweetData())
        recyclerView.adapter = dialogPostAdapter

        bottomSheetDialog.show()

//        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_marker_details, null)
//        val popupDialog = AlertDialog.Builder(requireContext())
//            .setView(popupView)
//            .create()
//
//        val markerNameTextView = popupView.findViewById<TextView>(R.id.markerName)
//        val markerImageView = popupView.findViewById<ImageView>(R.id.markerImage)
//        val ratingTextView = popupView.findViewById<TextView>(R.id.ratingCount)
//
//        // Update initial data
//        markerNameTextView.text = markerData.name
//        markerImageView.setImageResource(markerData.imageResId)
//        ratingTextView.text = "Rating: ${markerData.rating} / 5"
//
//        // Handle star rating
//        val starButtons = listOf(
//            popupView.findViewById<Button>(R.id.star1Button),
//            popupView.findViewById<Button>(R.id.star2Button),
//            popupView.findViewById<Button>(R.id.star3Button),
//            popupView.findViewById<Button>(R.id.star4Button),
//            popupView.findViewById<Button>(R.id.star5Button)
//        )
//
//        starButtons.forEachIndexed { index, button ->
//            button.setOnClickListener {
//                markerData.rating = index + 1  // Set rating to the button number (1-5)
//                ratingTextView.text = "Rating: ${markerData.rating} / 5"
//            }
//        }
//
//        // Handle edit marker name
//        val editButton = popupView.findViewById<Button>(R.id.editButton)
//        editButton.setOnClickListener {
//            val editText = EditText(context)
//            AlertDialog.Builder(requireContext())
//                .setTitle("Edit Marker Name")
//                .setView(editText)
//                .setPositiveButton("Save") { dialog, _ ->
//                    markerData.name = editText.text.toString()
//                    markerNameTextView.text = markerData.name
//                    dialog.dismiss()
//                }
//                .setNegativeButton("Cancel", null)
//                .show()
//        }
//
//        popupDialog.show()
    }

}