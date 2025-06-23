package com.example.nutrisense.utils

import com.example.nutrisense.managers.SharedPreferencesManager

object ProfileUtils {

    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> "${days / 7}w ago"
        }
    }

    fun formatWeightDisplay(preferencesManager: SharedPreferencesManager): String {
        val weight = preferencesManager.getUserWeight()
        val height = preferencesManager.getUserHeight()
        val age = preferencesManager.getUserAge()
        val units = preferencesManager.getPreferredUnits()

        return buildString {
            if (weight > 0) {
                append("Weight: ${NutritionCalculator.Formatter.formatWeight(weight, units)}")
            } else {
                append("Weight: Not set")
            }

            if (age > 0) {
                append("\nAge: $age years")
            }

            if (height > 0) {
                append("\nHeight: ${NutritionCalculator.Formatter.formatHeight(height, units)}")
            }
        }
    }

    fun getBMIDisplay(preferencesManager: SharedPreferencesManager): Pair<String, Int> {
        val weight = preferencesManager.getUserWeight()
        val height = preferencesManager.getUserHeight()
        val units = preferencesManager.getPreferredUnits()

        return if (weight > 0 && height > 0) {
            val bmi = NutritionCalculator.calculateBMIWithUnits(weight, height, units)
            val text = NutritionCalculator.Formatter.formatBMI(bmi)
            val color = NutritionCalculator.getBMIColorResource(bmi)
            Pair(text, color)
        } else {
            Pair("BMI: Set weight & height in settings", android.R.color.tertiary_text_light)
        }
    }

    fun getSettingsButtonText(preferencesManager: SharedPreferencesManager): String {
        val weight = preferencesManager.getUserWeight()
        val height = preferencesManager.getUserHeight()

        return if (weight > 0 && height > 0) {
            "Update Settings & Goals"
        } else {
            "Complete Your Profile"
        }
    }

    fun setDefaultNutritionGoals(preferencesManager: SharedPreferencesManager) {
        preferencesManager.apply {
            setDailyCalorieGoal(AppConstants.DEFAULT_CALORIE_GOAL)
            setDailyWaterGoal(AppConstants.DEFAULT_WATER_GOAL_ML)
            setNotificationEnabled(true)
            setWaterReminderInterval(AppConstants.DEFAULT_WATER_REMINDER_INTERVAL)
            setMealReminderEnabled(true)
            setPreferredUnits(AppConstants.DEFAULT_UNITS)
        }
    }
}