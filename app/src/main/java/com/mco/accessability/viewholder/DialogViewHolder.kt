package com.mco.accessability.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.mco.accessability.databinding.ItemNotesLayoutBinding
import com.mco.accessability.models.ReviewModel


class DialogViewHolder(private val viewBinding: ItemNotesLayoutBinding): RecyclerView.ViewHolder(viewBinding.root)  {
    fun bindData(notes: ReviewModel){
        this.viewBinding.author.text =notes.author
        this.viewBinding.notes.text = notes.notes
        this.viewBinding.userImage.setImageResource(notes.imageId)
    }
}