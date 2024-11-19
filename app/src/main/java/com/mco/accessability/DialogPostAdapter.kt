package com.mco.accessability

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mco.accessability.databinding.ItemNotesLayoutBinding

class DialogPostAdapter (private val notes: ArrayList<AddedNotesModel>): RecyclerView.Adapter<DialogViewHolder>(){
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