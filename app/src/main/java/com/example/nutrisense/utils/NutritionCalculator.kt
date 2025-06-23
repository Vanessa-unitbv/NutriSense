package com.example.nutrisense.utils

import com.example.nutrisense.utils.AppConstants.ActivityMultipliers
import com.example.nutrisense.utils.AppConstants.HarrisBenedict

object NutritionCalculator {

    fun calculateBMI(weightKg: Float, heightMeters: Float): Float {
        return if (weightKg > 0 && heightMeters > 0) {
            weightKg / (heightMeters * heightMeters)
        } else {
            0f
        }
    }

    fun calculateBMIWithUnits(weight: Float, height: Float, units: String): Float {
        val heightInMeters = convertHeightToMeters(height, units)
        val weightInKg = convertWeightToKg(weight, units)
        return calculateBMI(weightInKg, heightInMeters)
    }

    fun getBMICategory(bmi: Float): String {
        return when {
            bmi < AppConstants.BMI_UNDERWEIGHT -> "Underweight"
            bmi < AppConstants.BMI_NORMAL -> "Normal"
            bmi < AppConstants.BMI_OVERWEIGHT -> "Overweight"
            else -> "Obese"
        }
    }

    fun getBMIColorResource(bmi: Float): Int {
        return when (getBMICategory(bmi)) {
            "Normal" -> android.R.color.holo_green_dark
            "Underweight" -> android.R.color.holo_blue_dark
            "Overweight" -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_red_dark
        }
    }

    fun calculateBMR(weightKg: Float, heightCm: Float, age: Int, gender: String): Float {
        return if (gender.lowercase() == "male") {
            HarrisBenedict.MALE_BMR_CONSTANT +
                    (HarrisBenedict.MALE_WEIGHT_FACTOR * weightKg) +
                    (HarrisBenedict.MALE_HEIGHT_FACTOR * heightCm) -
                    (HarrisBenedict.MALE_AGE_FACTOR * age)
        } else {
            HarrisBenedict.FEMALE_BMR_CONSTANT +
                    (HarrisBenedict.FEMALE_WEIGHT_FACTOR * weightKg) +
                    (HarrisBenedict.FEMALE_HEIGHT_FACTOR * heightCm) -
                    (HarrisBenedict.FEMALE_AGE_FACTOR * age)
        }
    }

    fun calculateDailyCalorieNeeds(bmr: Float, activityLevel: String): Int {
        val multiplier = getActivityMultiplier(activityLevel)
        return (bmr * multiplier).toInt()
    }

    fun calculateRecommendedCalories(
        weight: Float,
        height: Float,
        age: Int,
        gender: String,
        activityLevel: String,
        units: String = AppConstants.UNITS_METRIC
    ): Int {
        val weightKg = convertWeightToKg(weight, units)
        val heightCm = convertHeightToCm(height, units)

        val bmr = calculateBMR(weightKg, heightCm, age, gender)
        return calculateDailyCalorieNeeds(bmr, activityLevel)
    }

    fun calculateWaterIntake(weightKg: Float, activityLevel: String = AppConstants.ACTIVITY_MODERATE): Int {
        val baseWater = weightKg * AppConstants.WATER_ML_PER_KG
        val activityBonus = getActivityWaterBonus(activityLevel)
        return (baseWater + activityBonus).toInt()
    }

    fun calculateRecommendedWater(
        weight: Float,
        activityLevel: String,
        units: String = AppConstants.UNITS_METRIC
    ): Int {
        val weightKg = convertWeightToKg(weight, units)
        return calculateWaterIntake(weightKg, activityLevel)
    }

    fun convertWeightToKg(weight: Float, fromUnits: String): Float {
        return if (fromUnits == AppConstants.UNITS_IMPERIAL) {
            weight * AppConstants.LBS_TO_KG
        } else {
            weight
        }
    }

    fun convertWeightFromKg(weightKg: Float, toUnits: String): Float {
        return if (toUnits == AppConstants.UNITS_IMPERIAL) {
            weightKg / AppConstants.LBS_TO_KG
        } else {
            weightKg
        }
    }

    fun convertHeightToMeters(height: Float, fromUnits: String): Float {
        return if (fromUnits == AppConstants.UNITS_IMPERIAL) {
            height * AppConstants.INCHES_TO_METERS
        } else {
            height / 100f
        }
    }

    fun convertHeightToCm(height: Float, fromUnits: String): Float {
        return if (fromUnits == AppConstants.UNITS_IMPERIAL) {
            height * 2.54f 
        } else {
            height
        }
    }

    fun calculateIdealWeightRange(heightMeters: Float): Pair<Float, Float> {
        val heightSquared = heightMeters * heightMeters
        val minWeight = AppConstants.BMI_UNDERWEIGHT * heightSquared
        val maxWeight = (AppConstants.BMI_NORMAL - 0.1f) * heightSquared
        return Pair(minWeight, maxWeight)
    }

    fun calculateCaloriesForWeightGoal(
        currentWeight: Float,
        targetWeight: Float,
        timeFrameWeeks: Int,
        bmr: Float,
        activityLevel: String
    ): Int {
        val weightDifference = targetWeight - currentWeight
        val totalCalorieDeficit = weightDifference * 7700 // 1kg = ~7700 calories
        val dailyCalorieAdjustment = totalCalorieDeficit / (timeFrameWeeks * 7)

        val maintenanceCalories = calculateDailyCalorieNeeds(bmr, activityLevel)
        return maintenanceCalories + dailyCalorieAdjustment.toInt()
    }

    private fun getActivityMultiplier(activityLevel: String): Float {
        return when (activityLevel.lowercase()) {
            AppConstants.ACTIVITY_SEDENTARY -> ActivityMultipliers.SEDENTARY
            AppConstants.ACTIVITY_LIGHT -> ActivityMultipliers.LIGHT
            AppConstants.ACTIVITY_MODERATE -> ActivityMultipliers.MODERATE
            AppConstants.ACTIVITY_ACTIVE -> ActivityMultipliers.ACTIVE
            AppConstants.ACTIVITY_VERY_ACTIVE -> ActivityMultipliers.VERY_ACTIVE
            else -> ActivityMultipliers.MODERATE
        }
    }

    private fun getActivityWaterBonus(activityLevel: String): Float {
        return when (activityLevel.lowercase()) {
            AppConstants.ACTIVITY_SEDENTARY -> 0f
            AppConstants.ACTIVITY_LIGHT -> 200f
            AppConstants.ACTIVITY_MODERATE -> 400f
            AppConstants.ACTIVITY_ACTIVE -> 600f
            AppConstants.ACTIVITY_VERY_ACTIVE -> 800f
            else -> 400f
        }
    }

    object Validator {
        fun isValidWeight(weight: Float, units: String): Boolean {
            val weightKg = convertWeightToKg(weight, units)
            return weightKg >= AppConstants.MIN_WEIGHT_KG && weightKg <= AppConstants.MAX_WEIGHT_KG
        }

        fun isValidHeight(height: Float, units: String): Boolean {
            val heightCm = convertHeightToCm(height, units)
            return heightCm >= AppConstants.MIN_HEIGHT_CM && heightCm <= AppConstants.MAX_HEIGHT_CM
        }

        fun isValidAge(age: Int): Boolean {
            return age >= AppConstants.MIN_AGE && age <= AppConstants.MAX_AGE
        }

        fun isValidCalorieGoal(calories: Int): Boolean {
            return calories >= AppConstants.MIN_CALORIE_GOAL && calories <= AppConstants.MAX_CALORIE_GOAL
        }

        fun isValidWaterGoal(waterMl: Int): Boolean {
            return waterMl >= AppConstants.MIN_WATER_GOAL_ML && waterMl <= AppConstants.MAX_WATER_GOAL_ML
        }
    }

    object Formatter {
        fun formatBMI(bmi: Float): String {
            val category = getBMICategory(bmi)
            return "BMI: %.1f (%s)".format(bmi, category)
        }

        fun formatWeight(weight: Float, units: String): String {
            val unit = if (units == AppConstants.UNITS_IMPERIAL) "lbs" else "kg"
            return "%.1f %s".format(weight, unit)
        }

        fun formatHeight(height: Float, units: String): String {
            val unit = if (units == AppConstants.UNITS_IMPERIAL) "in" else "cm"
            return "%.1f %s".format(height, unit)
        }

        fun formatCalories(calories: Int): String {
            return "$calories kcal"
        }

        fun formatWater(waterMl: Int): String {
            return "$waterMl ml"
        }
    }
}
