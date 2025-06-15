package com.example.nutrisense.data.model

import com.google.gson.annotations.SerializedName

data class NutritionResponse(
    @SerializedName("name")
    val name: String,

    @SerializedName("calories")
    val calories: Double,

    @SerializedName("serving_size_g")
    val servingSizeG: Double,

    @SerializedName("fat_total_g")
    val fatTotalG: Double,

    @SerializedName("fat_saturated_g")
    val fatSaturatedG: Double,

    @SerializedName("protein_g")
    val proteinG: Double,

    @SerializedName("sodium_mg")
    val sodiumMg: Double,

    @SerializedName("potassium_mg")
    val potassiumMg: Double,

    @SerializedName("cholesterol_mg")
    val cholesterolMg: Double,

    @SerializedName("carbohydrates_total_g")
    val carbohydratesTotalG: Double,

    @SerializedName("fiber_g")
    val fiberG: Double,

    @SerializedName("sugar_g")
    val sugarG: Double
)