package com.mco.frame

import android.Manifest
import MarkerData
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog

class MapFragment(private val sharedViewModel: SharedViewModel) : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var isMarkerAdderModeEnabled = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var allMarkerData: List<MarkerData> = emptyList() // holds marker data (like their likes and etc)
    private val markers: MutableList<Marker> = mutableListOf()  // List to hold markers (google api marker)

    // Access the same SharedViewModel as the activity
    //private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize BottomSheetDialog
        var bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_dialog, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the close button
        val closeButton = bottomSheetView.findViewById<ImageView>(R.id.imageView)
        closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        //sharedViewModel.logMarkerData()

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        Log.d("MapFragment", "Loading markers from ViewModel...")

        sharedViewModel.logMarkerData()

        val allMarkerData = sharedViewModel.getAllMarkerData()
        allMarkerData.forEach { markerData ->
            addMarker(markerData) // Call the addMarker function
        }

        googleMap?.setOnMapClickListener { latLng ->
            if (isMarkerAdderModeEnabled) {
                // Create a new marker and its corresponding data
                //val markerOptions = MarkerOptions().position(latLng).title("New Marker")
                //val marker = googleMap?.addMarker(markerOptions)

                val markerData = MarkerData(
                    name = "New Marker",
                    imageResId = R.drawable.placeholder, // Placeholder image
                    voteCount = 0,
                    lat = latLng.latitude,
                    lng = latLng.longitude,
                    markerID = ""
                )

                addMarker(markerData)
                sharedViewModel.addMarkerData(markerData.markerID, markerData)
            } else {
                // Move the map camera to the clicked location if marker adder mode is off
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }


        googleMap?.setOnMarkerClickListener { marker ->
            val markerData = marker.tag as? MarkerData
            markerData?.let {
                showBottomSheetForExistingMarker(it)
            }
            true
        }
    }

    private fun showBottomSheetForExistingMarker(it: MarkerData) {
        TODO("Not yet implemented")
    }

    private fun addMarker(markerData: MarkerData) {
        val markerOptions = MarkerOptions()
            .position(LatLng(markerData.lat, markerData.lng))
            .title(markerData.name)

        val marker = googleMap?.addMarker(markerOptions)
        marker?.tag = markerData
        markerData.markerID = marker?.id ?: ""

        // Add the marker to the markers list
        marker?.let { markers.add(it) }
    }

    private fun showMarkerPopup(marker: Marker, markerData: MarkerData) {
        // Custom popup dialog with the marker's name, image, and action buttons
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_marker_details, null)
        val popupDialog = AlertDialog.Builder(requireContext())
            .setView(popupView)
            .create()

        val markerNameTextView = popupView.findViewById<TextView>(R.id.markerName)
        val markerImageView = popupView.findViewById<ImageView>(R.id.markerImage)
//        val voteCountTextView = popupView.findViewById<TextView>(R.id.voteCount)
//        val upvoteButton = popupView.findViewById<Button>(R.id.upvoteButton)
//        val downvoteButton = popupView.findViewById<Button>(R.id.downvoteButton)
        val editButton = popupView.findViewById<Button>(R.id.editButton)
        val deleteButton = popupView.findViewById<Button>(R.id.deleteButton)

        // Set initial data
        markerNameTextView.text = markerData.name
        markerImageView.setImageResource(markerData.imageResId)
//        voteCountTextView.text = markerData.voteCount.toString()

//        // Handle upvote
//        upvoteButton.setOnClickListener {
//            markerData.voteCount++
//            voteCountTextView.text = markerData.voteCount.toString()
//        }
//
//        // Handle downvote
//        downvoteButton.setOnClickListener {
//            markerData.voteCount--
//            voteCountTextView.text = markerData.voteCount.toString()
//        }

        // Handle edit marker name
        editButton.setOnClickListener {
            val editText = EditText(context)
            AlertDialog.Builder(requireContext())
                .setTitle("Edit Marker Name")
                .setView(editText)
                .setPositiveButton("Save") { dialog, _ ->
                    markerData.name = editText.text.toString()
                    markerNameTextView.text = markerData.name
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Handle delete marker
        deleteButton.setOnClickListener {
            marker.remove()  // Remove the marker from the map
            sharedViewModel.removeMarkerData(marker.id)  // Remove the data from the shared ViewModel
            popupDialog.dismiss()
        }

        popupDialog.show()
    }

    companion object {
        fun newInstance(sharedViewModel: SharedViewModel): MapFragment {
            return MapFragment(sharedViewModel)
        }
    }
}