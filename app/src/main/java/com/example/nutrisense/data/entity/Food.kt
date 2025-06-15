package com.example.nutrisense.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_foods")
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val originalQuery: String,
    val requestedQuantityG: Double,
    val calories: Double,
    val servingSizeG: Double,
    val fatTotalG: Double,
    val fatSaturatedG: Double,
    val proteinG: Double,
    val sodiumMg: Double,
    val potassiumMg: Double,
    val cholesterolMg: Double,
    val carbohydratesTotalG: Double,
    val fiberG: Double,
    val sugarG: Double,
    val addedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val consumedAt: Long? = null
)

fun com.example.nutrisense.data.model.NutritionResponse.toUserFood(
    userId: Long,
    originalQuery: String,
    requestedQuantity: Double
): Food {
    return Food(
        userId = userId,
        name = "${this.name} (${requestedQuantity.toInt()}g)",
        originalQuery = originalQuery,
        requestedQuantityG = requestedQuantity,
        calories = this.calories,
        servingSizeG = this.servingSizeG,
        fatTotalG = this.fatTotalG,
        fatSaturatedG = this.fatSaturatedG,
        proteinG = this.proteinG,
        sodiumMg = this.sodiumMg,
        potassiumMg = this.potassiumMg,
        cholesterolMg = this.cholesterolMg,
        carbohydratesTotalG = this.carbohydratesTotalG,
        fiberG = this.fiberG,
        sugarG = this.sugarG
    )
}