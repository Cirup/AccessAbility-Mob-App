package com.example.accessability_mob_app

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Marker
import java.util.HashMap

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var isMarkerAdderMode = false
    private val markersData = HashMap<Marker, MarkerData>()  // Store marker data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mappage)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            // Go back to MainActivity
            finish()
        }

        // Button to toggle marker adder mode
        val markerAdderModeButton = findViewById<Button>(R.id.markerAdderModeButton)
        markerAdderModeButton.setOnClickListener {
            isMarkerAdderMode = !isMarkerAdderMode
            markerAdderModeButton.text = if (isMarkerAdderMode) {
                "Marker Adder Mode: ON"
            } else {
                "Marker Adder Mode: OFF"
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a default marker and move the camera
        val location = LatLng(-34.0, 151.0)
        addMarker(location, "Default Marker", R.drawable.placeholder2) // Placeholder image
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        // Set an OnMapClickListener
        mMap.setOnMapClickListener { latLng ->
            if (isMarkerAdderMode) {
                addMarker(latLng, "New Marker", R.drawable.placeholder2) // Placeholder image
            }
        }

        // Set an OnMarkerClickListener
        mMap.setOnMarkerClickListener { marker ->
            showMarkerDetails(marker)
            true // Returning true indicates we have handled the click
        }
    }

    private fun addMarker(latLng: LatLng, name: String, imageResId: Int) {
        val marker = mMap.addMarker(MarkerOptions().position(latLng).title(name))
        marker?.let { // Only proceed if marker is not null
            markersData[it] = MarkerData(name, imageResId) // Store marker data
        }
    }

    private fun showMarkerDetails(marker: Marker) {
        val markerData = markersData[marker]
        if (markerData != null) {
            // Create a dialog to show marker details
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.popup_marker_details)

            val markerNameTextView = dialog.findViewById<TextView>(R.id.markerName)
            val markerImageView = dialog.findViewById<ImageView>(R.id.markerImage)
            val voteCountTextView = dialog.findViewById<TextView>(R.id.voteCountTextView)
            val upvoteButton = dialog.findViewById<Button>(R.id.upvoteButton)
            val downvoteButton = dialog.findViewById<Button>(R.id.downvoteButton)
            val editButton = dialog.findViewById<Button>(R.id.editButton)
            val deleteButton = dialog.findViewById<Button>(R.id.deleteButton)
            val closeButton = dialog.findViewById<Button>(R.id.closeButton)

            // Set the marker details in the dialog
            markerNameTextView.text = markerData.name
            markerImageView.setImageResource(markerData.imageResId)
            voteCountTextView.text = "Votes: ${markerData.voteCount}" // Display initial vote count

            upvoteButton.setOnClickListener {
                markerData.voteCount += 1 // Increment vote count
                voteCountTextView.text = "Votes: ${markerData.voteCount}" // Update displayed count
            }

            downvoteButton.setOnClickListener {
                markerData.voteCount -= 1 // Decrement vote count
                voteCountTextView.text = "Votes: ${markerData.voteCount}" // Update displayed count
            }

            editButton.setOnClickListener {
                // Handle edit action (change name)
                // For simplicity, we'll just change it to "Edited Name"
                val newName = "Edited Name"
                markerData.name = newName
                marker.title = newName // Update the marker title
                markerNameTextView.text = newName // Update the displayed name
                dialog.dismiss()
            }

            deleteButton.setOnClickListener {
                // Handle delete action
                marker.remove() // Remove the marker from the map
                markersData.remove(marker) // Remove the marker data
                dialog.dismiss()
            }

            closeButton.setOnClickListener {
                // Close the dialog
                dialog.dismiss()
            }

            dialog.show()
        }
    }

}
