package com.example.nutrisense.helpers.extensions

import android.util.Patterns
import com.example.nutrisense.utils.AppConstants

fun String.isValidEmail(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= AppConstants.MIN_PASSWORD_LENGTH
}

fun String.isValidAge(): Boolean {
    val age = this.toIntOrNull()
    return age != null && age >= AppConstants.MIN_AGE && age <= AppConstants.MAX_AGE
}

fun String.isValidWeight(units: String = AppConstants.UNITS_METRIC): Boolean {
    val weight = this.toFloatOrNull()
    if (weight == null || weight <= 0) return false

    return if (units == AppConstants.UNITS_IMPERIAL) {
        val weightKg = weight * AppConstants.LBS_TO_KG
        weightKg >= AppConstants.MIN_WEIGHT_KG && weightKg <= AppConstants.MAX_WEIGHT_KG
    } else {
        weight >= AppConstants.MIN_WEIGHT_KG && weight <= AppConstants.MAX_WEIGHT_KG
    }
}

fun String.isValidHeight(units: String = AppConstants.UNITS_METRIC): Boolean {
    val height = this.toFloatOrNull()
    if (height == null || height <= 0) return false

    return if (units == AppConstants.UNITS_IMPERIAL) {
        val heightCm = height * 2.54f
        heightCm >= AppConstants.MIN_HEIGHT_CM && heightCm <= AppConstants.MAX_HEIGHT_CM
    } else {
        height >= AppConstants.MIN_HEIGHT_CM && height <= AppConstants.MAX_HEIGHT_CM
    }
}

fun String.isValidCalorieGoal(): Boolean {
    val calories = this.toIntOrNull()
    return calories != null &&
            calories >= AppConstants.MIN_CALORIE_GOAL &&
            calories <= AppConstants.MAX_CALORIE_GOAL
}

fun String.isValidWaterGoal(): Boolean {
    val water = this.toIntOrNull()
    return water != null &&
            water >= AppConstants.MIN_WATER_GOAL_ML &&
            water <= AppConstants.MAX_WATER_GOAL_ML
}

fun String.isValidQuantity(): Boolean {
    val quantity = this.toDoubleOrNull()
    return quantity != null && quantity > 0 && quantity <= 10000 // Max 10kg
}

fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}