package com.example.nutrisense.data.model

import com.google.gson.annotations.SerializedName

data class RecipeResponse(
    @SerializedName("title")
    val title: String,

    @SerializedName("ingredients")
    val ingredients: String,

    @SerializedName("servings")
    val servings: String,

    @SerializedName("instructions")
    val instructions: String
)