package com.example.accessability_mob_app

data class MarkerData(
    var name: String,       // Name of the marker
    var imageResId: Int,   // Resource ID for the marker image
    var voteCount: Int = 0  // Initialize vote count to 0
)