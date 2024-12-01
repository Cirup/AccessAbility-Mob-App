package com.mco.accessability.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mco.accessability.databinding.ItemsReviewLayoutBinding
import com.mco.accessability.models.CombinedReviewModel
import com.mco.accessability.viewholder.ShowReviewHolder

class ShowReviewAdapter(private var reviewList: List<CombinedReviewModel>) : RecyclerView.Adapter<ShowReviewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowReviewHolder {
        val itemViewBinding = ItemsReviewLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ShowReviewHolder(itemViewBinding)
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateList(newList: List<CombinedReviewModel>) {
        reviewList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ShowReviewHolder, position: Int) {
        holder.bindData(reviewList[position])
    }
}