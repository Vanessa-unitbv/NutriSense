package com.example.nutrisense.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisense.R
import com.example.nutrisense.data.entity.Food
import java.text.SimpleDateFormat
import java.util.*

class FoodAdapter(
    private val onItemClick: (Food) -> Unit,
    private val onFavoriteClick: (Food) -> Unit,
    private val onDeleteClick: (Food) -> Unit,
    private val onConsumeClick: ((Food) -> Unit)? = null
) : ListAdapter<Food, FoodAdapter.FoodViewHolder>(FoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName: TextView = itemView.findViewById(R.id.tv_food_name)
        private val tvOriginalQuery: TextView = itemView.findViewById(R.id.tv_original_query)
        private val tvCalories: TextView = itemView.findViewById(R.id.tv_calories)
        private val tvProtein: TextView = itemView.findViewById(R.id.tv_protein)
        private val tvCarbs: TextView = itemView.findViewById(R.id.tv_carbs)
        private val tvFat: TextView = itemView.findViewById(R.id.tv_fat)
        private val tvServing: TextView = itemView.findViewById(R.id.tv_serving)
        private val tvAddedDate: TextView = itemView.findViewById(R.id.tv_added_date)
        private val tvConsumedStatus: TextView = itemView.findViewById(R.id.tv_consumed_status)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btn_favorite)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
        private val btnConsume: ImageButton = itemView.findViewById(R.id.btn_consume)

        fun bind(food: Food) {
            tvFoodName.text = food.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

            tvOriginalQuery.text = "Search: \"${food.originalQuery}\""

            tvCalories.text = "${food.calories.toInt()} kcal"
            tvProtein.text = "Protein: ${String.format("%.1f", food.proteinG)}g"
            tvCarbs.text = "Carbs: ${String.format("%.1f", food.carbohydratesTotalG)}g"
            tvFat.text = "Fat: ${String.format("%.1f", food.fatTotalG)}g"
            tvServing.text = "Quantity: ${String.format("%.0f", food.requestedQuantityG)}g"

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvAddedDate.text = "Added: ${dateFormat.format(Date(food.addedAt))}"

            if (food.consumedAt != null) {
                val consumedFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvConsumedStatus.text = "✅ Consumed: ${consumedFormat.format(Date(food.consumedAt))}"
                tvConsumedStatus.visibility = View.VISIBLE
                tvConsumedStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
            } else {
                tvConsumedStatus.text = "⏳ Not consumed"
                tvConsumedStatus.visibility = View.VISIBLE
                tvConsumedStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
            }

            btnFavorite.setImageResource(
                if (food.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_outline
            )

            if (onConsumeClick != null) {
                btnConsume.visibility = View.VISIBLE
                if (food.consumedAt != null) {
                    btnConsume.setImageResource(R.drawable.ic_check_circle_filled)
                    btnConsume.alpha = 0.5f // Faded pentru consumed items
                } else {
                    btnConsume.setImageResource(R.drawable.ic_add_circle)
                    btnConsume.alpha = 1.0f
                }
            } else {
                btnConsume.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClick(food) }

            btnFavorite.setOnClickListener {
                onFavoriteClick(food.copy(isFavorite = !food.isFavorite))
            }

            btnDelete.setOnClickListener { onDeleteClick(food) }

            btnConsume.setOnClickListener {
                if (food.consumedAt == null) {
                    onConsumeClick?.invoke(food)
                }
            }
        }
    }

    class FoodDiffCallback : DiffUtil.ItemCallback<Food>() {
        override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
            return oldItem == newItem
        }
    }
}