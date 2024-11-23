package com.mco.accessability.models

data class ReviewModel(
    val author:String,
    val notes: String,
    val imageId: Int,
    val rating: Int,
    val upvotes: List<String> = emptyList(),
    val downvotes: List<String> = emptyList()
)