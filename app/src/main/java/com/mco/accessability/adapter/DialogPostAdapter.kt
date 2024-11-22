package com.mco.accessability.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mco.accessability.ReviewModel
import com.mco.accessability.viewholder.DialogViewHolder
import com.mco.accessability.databinding.ItemNotesLayoutBinding
import com.mco.accessability.models.AddedNotesModel

class DialogPostAdapter (private val notes: ArrayList<ReviewModel>): RecyclerView.Adapter<DialogViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        val itemViewBinding :ItemNotesLayoutBinding= ItemNotesLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DialogViewHolder(itemViewBinding)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
        holder.bindData(notes[position])
    }

}