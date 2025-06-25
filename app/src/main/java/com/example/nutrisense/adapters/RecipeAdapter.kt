package com.example.nutrisense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisense.R
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.helpers.extensions.show
import com.example.nutrisense.helpers.extensions.hide
import java.text.SimpleDateFormat
import java.util.*

class RecipeAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onFavoriteClick: (Recipe) -> Unit,
    private val onDeleteClick: (Recipe) -> Unit,
    private val showActionButtons: Boolean = true
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRecipeTitle: TextView = itemView.findViewById(R.id.tv_recipe_title)
        private val tvSearchQuery: TextView = itemView.findViewById(R.id.tv_search_query)
        private val tvServings: TextView = itemView.findViewById(R.id.tv_servings)
        private val tvIngredientsPreview: TextView =
            itemView.findViewById(R.id.tv_ingredients_preview)
        private val tvAddedDate: TextView = itemView.findViewById(R.id.tv_added_date)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btn_favorite)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)


        fun bind(recipe: Recipe) {
            tvRecipeTitle.text = recipe.title.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }

            tvSearchQuery.text = "Search: \"${recipe.searchQuery}\""
            tvServings.text = "Servings: ${recipe.servings}"

            val ingredientsPreview = if (recipe.ingredients.length > 100) {
                "${recipe.ingredients.take(100)}..."
            } else {
                recipe.ingredients
            }
            tvIngredientsPreview.text = "Ingredients: $ingredientsPreview"

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvAddedDate.text = "Added: ${dateFormat.format(Date(recipe.addedAt))}"


            if (recipe.id == 0L) {
                btnFavorite.hide()
                btnDelete.hide()
            } else {
                btnFavorite.show()
                btnDelete.show()

                btnFavorite.setImageResource(
                    if (recipe.isFavorite) R.drawable.ic_favorite_filled
                    else R.drawable.ic_favorite_outline
                )

                btnFavorite.setOnClickListener {
                    onFavoriteClick(recipe.copy(isFavorite = !recipe.isFavorite))
                }

                btnDelete.setOnClickListener { onDeleteClick(recipe) }
            }

            itemView.setOnClickListener { onItemClick(recipe) }
        }
    }
    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}