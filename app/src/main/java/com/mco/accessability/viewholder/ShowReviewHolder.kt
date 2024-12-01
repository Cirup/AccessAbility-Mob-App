package com.mco.accessability.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.mco.accessability.databinding.ItemsReviewLayoutBinding
import com.mco.accessability.models.CombinedReviewModel

class ShowReviewHolder(private val viewBinding: ItemsReviewLayoutBinding): RecyclerView.ViewHolder(viewBinding.root){

    fun bindData(review: CombinedReviewModel){
        viewBinding.place.text = review.placeName
        viewBinding.note.text = review.reviewNotes
        viewBinding.rate.text = review.placeRating.toString()
    }
}