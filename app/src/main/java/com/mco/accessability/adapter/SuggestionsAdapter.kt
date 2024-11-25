package com.mco.accessability.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mco.accessability.databinding.SuggestionItemBinding
import com.mco.accessability.models.MarkerData
import com.mco.accessability.viewholder.SuggestionViewHolder

class SuggestionsAdapter(
    private var suggestions: List<MarkerData>,
    private val onSuggestionClicked: (MarkerData) -> Unit
) : RecyclerView.Adapter<SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = SuggestionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SuggestionViewHolder(binding, onSuggestionClicked)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(suggestions[position])
    }

    override fun getItemCount(): Int = suggestions.size

    fun updateData(newSuggestions: List<MarkerData>) {
        this.suggestions = newSuggestions
        notifyDataSetChanged()
    }
}