package com.mco.frame

import MarkerData
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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment(private val sharedViewModel: SharedViewModel) : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var isMarkerAdderModeEnabled = false

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
                showMarkerPopup(marker, it)
            }
            true
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
    }

    private fun showMarkerPopup(marker: Marker, markerData: MarkerData) {
        // Custom popup dialog with the marker's name, image, and action buttons
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_marker_details, null)
        val popupDialog = AlertDialog.Builder(requireContext())
            .setView(popupView)
            .create()

        val markerNameTextView = popupView.findViewById<TextView>(R.id.markerName)
        val markerImageView = popupView.findViewById<ImageView>(R.id.markerImage)
        val voteCountTextView = popupView.findViewById<TextView>(R.id.voteCount)
        val upvoteButton = popupView.findViewById<Button>(R.id.upvoteButton)
        val downvoteButton = popupView.findViewById<Button>(R.id.downvoteButton)
        val editButton = popupView.findViewById<Button>(R.id.editButton)
        val deleteButton = popupView.findViewById<Button>(R.id.deleteButton)

        // Set initial data
        markerNameTextView.text = markerData.name
        markerImageView.setImageResource(markerData.imageResId)
        voteCountTextView.text = markerData.voteCount.toString()

        // Handle upvote
        upvoteButton.setOnClickListener {
            markerData.voteCount++
            voteCountTextView.text = markerData.voteCount.toString()
        }

        // Handle downvote
        downvoteButton.setOnClickListener {
            markerData.voteCount--
            voteCountTextView.text = markerData.voteCount.toString()
        }

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