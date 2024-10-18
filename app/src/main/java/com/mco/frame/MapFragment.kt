package com.mco.frame

import MarkerData
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog

class MapFragment(private val sharedViewModel: SharedViewModel) : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var dialogPostAdapter: DialogPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize BottomSheetDialog
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

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Load existing markers
        val allMarkerData = sharedViewModel.getAllMarkerData()
        allMarkerData.forEach { markerData ->
            addMarker(markerData)
        }

        googleMap?.setOnMapClickListener { latLng ->
            showBottomSheetForLocation(latLng)
        }

        googleMap?.setOnMarkerClickListener { marker ->
            val markerData = marker.tag as? MarkerData
            markerData?.let {
                showBottomSheetForExistingMarker(it)
            }
            true
        }
    }

    private fun showBottomSheetForLocation(latLng: LatLng) {
        val bottomSheetView = bottomSheetDialog.findViewById<View>(android.R.id.content)


        // Update RecyclerView data if needed
        // For now, we're using the same data for all locations
        dialogPostAdapter.notifyDataSetChanged()

        bottomSheetDialog.show()
    }

    private fun showBottomSheetForExistingMarker(markerData: MarkerData) {
        val bottomSheetView = bottomSheetDialog.findViewById<View>(android.R.id.content)
        val locationNameTextView = bottomSheetView?.findViewById<TextView>(R.id.textView2)

        locationNameTextView?.text = markerData.name

        // Update RecyclerView data if needed
        // For now, we're using the same data for all markers
        dialogPostAdapter.notifyDataSetChanged()

        bottomSheetDialog.show()
    }

    private fun addMarker(markerData: MarkerData) {
        val markerOptions = MarkerOptions()
            .position(LatLng(markerData.lat, markerData.lng))
            .title(markerData.name)

        val marker = googleMap?.addMarker(markerOptions)
        marker?.tag = markerData
        markerData.markerID = marker?.id ?: ""
    }

    companion object {
        fun newInstance(sharedViewModel: SharedViewModel): MapFragment {
            return MapFragment(sharedViewModel)
        }
    }
}