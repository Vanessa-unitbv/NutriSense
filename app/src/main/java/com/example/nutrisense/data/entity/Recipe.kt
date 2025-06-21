package com.example.nutrisense.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val ingredients: String,
    val servings: String,
    val instructions: String,
    val searchQuery: String,
    val addedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

fun com.example.nutrisense.data.model.RecipeResponse.toUserRecipe(
    userId: Long,
    searchQuery: String
): Recipe {
    return Recipe(
        userId = userId,
        title = this.title,
        ingredients = this.ingredients,
        servings = this.servings,
        instructions = this.instructions,
        searchQuery = searchQuery
    )
}