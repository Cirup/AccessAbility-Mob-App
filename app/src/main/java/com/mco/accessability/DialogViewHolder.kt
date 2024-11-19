package com.mco.accessability

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mco.frame.databinding.ItemNotesLayoutBinding

class DialogViewHolder(private val viewBinding: ItemNotesLayoutBinding): RecyclerView.ViewHolder(viewBinding.root)  {
    fun bindData(notes: AddedNotesModel){
        this.viewBinding.author.text =notes.author
        this.viewBinding.notes.text = notes.note
        this.viewBinding.userImage.setImageResource(notes.imageId)
    }
}