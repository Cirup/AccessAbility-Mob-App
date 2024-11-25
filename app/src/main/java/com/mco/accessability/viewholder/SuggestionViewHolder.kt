package com.mco.accessability.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.mco.accessability.databinding.SuggestionItemBinding
import com.mco.accessability.models.MarkerData

class SuggestionViewHolder(
    private val binding: SuggestionItemBinding,
    private val onSuggestionClicked: (MarkerData) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(markerData: MarkerData) {
        binding.textViewSuggestion.text = markerData.nameOfPlace
        binding.root.setOnClickListener { onSuggestionClicked(markerData) }
    }
}