package com.pashuraksha

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.pashuraksha.databinding.ItemStatCardBinding

data class StatCard(val title: String, val value: String, val iconResId: Int, val colorResId: Int)

class StatCardAdapter(private val statCards: List<StatCard>) : RecyclerView.Adapter<StatCardAdapter.StatCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatCardViewHolder {
        val binding = ItemStatCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatCardViewHolder, position: Int) {
        val statCard = statCards[position]
        holder.bind(statCard)
    }

    override fun getItemCount(): Int = statCards.size

    class StatCardViewHolder(private val binding: ItemStatCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(statCard: StatCard) {
            binding.cardTitle.text = statCard.title
            binding.cardValue.text = statCard.value
            binding.cardIcon.setImageResource(statCard.iconResId)
            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, statCard.colorResId))
        }
    }
}
