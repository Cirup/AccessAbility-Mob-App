package com.mco.accessability.models

data class MarkerData(
    var markerID: String,  // Unique ID for the marker
    var name: String,       // Name of the marker
    var imageResId: Int,   // Resource ID for the marker image
    var lat: Double,       // Latitude of the marker
    var lng: Double,       // Longitude of the marker
    var rating: Int = 0,  // default rating is 0 to say it is unrated
    var notes: List<String> = emptyList()
)
