package com.mco.accessability.models

data class MarkerData(
    var nameOfPlace: String = "",
    var imageres: Int = 0,
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    val ratings: List<UserRating> = listOf(), // Replace Pair with UserRating
    var notes: List<String> = emptyList()
) {
    constructor() : this("", 0, 0.0, 0.0, listOf(), emptyList())
}
