package com.mco.accessability.models


data class MarkerData(
    var nameOfPlace: String = "",  // Default empty string for nameOfPlace
    var imageres: Int = 0,        // Default value for image resource
    var lat: Double = 0.0,        // Default value for latitude
    var lng: Double = 0.0,        // Default value for longitude
    var rating: Int = 0,          // Default rating is 0 (unrated)
    var notes: List<String> = emptyList() // Default empty list for notes
) {
    // No-argument constructor
    constructor() : this("", 0, 0.0, 0.0, 0, emptyList())
}
