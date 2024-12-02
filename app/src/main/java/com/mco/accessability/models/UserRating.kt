package com.mco.accessability.models

data class UserRating(
    val username: String = "",
    val rating: Int = 0
) {
    constructor() : this("", 0)
}
