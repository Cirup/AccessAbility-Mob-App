package com.mco.accessability.models

data class ReviewModel(
    val id: String = "",  // Firebase document ID
    val author: String = "",
    val notes: String = "",
    val imageId: Int = 0,
    val rating: Int = 0,
    val upvotes: List<String> = emptyList(),
    val downvotes: List<String> = emptyList()
)


