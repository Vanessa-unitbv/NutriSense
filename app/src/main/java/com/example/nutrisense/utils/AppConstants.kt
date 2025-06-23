package com.example.nutrisense.utils

object AppConstants {

    const val DEFAULT_CALORIE_GOAL = 2000
    const val DEFAULT_WATER_GOAL_ML = 2000
    const val DEFAULT_ACTIVITY_LEVEL = "moderate"
    const val DEFAULT_WATER_REMINDER_INTERVAL = 60
    const val DEFAULT_UNITS = "metric"

    const val ACTIVITY_SEDENTARY = "sedentary"
    const val ACTIVITY_LIGHT = "light"
    const val ACTIVITY_MODERATE = "moderate"
    const val ACTIVITY_ACTIVE = "active"
    const val ACTIVITY_VERY_ACTIVE = "very_active"

    const val UNITS_METRIC = "metric"
    const val UNITS_IMPERIAL = "imperial"

    const val BMI_UNDERWEIGHT = 18.5f
    const val BMI_NORMAL = 25.0f
    const val BMI_OVERWEIGHT = 30.0f

    const val LBS_TO_KG = 0.453592f
    const val INCHES_TO_METERS = 0.0254f
    const val CM_TO_METERS = 0.01f

    object HarrisBenedict {
        const val MALE_BMR_CONSTANT = 88.362f
        const val MALE_WEIGHT_FACTOR = 13.397f
        const val MALE_HEIGHT_FACTOR = 4.799f
        const val MALE_AGE_FACTOR = 5.677f

        const val FEMALE_BMR_CONSTANT = 447.593f
        const val FEMALE_WEIGHT_FACTOR = 9.247f
        const val FEMALE_HEIGHT_FACTOR = 3.098f
        const val FEMALE_AGE_FACTOR = 4.330f
    }

    object ActivityMultipliers {
        const val SEDENTARY = 1.2f
        const val LIGHT = 1.375f
        const val MODERATE = 1.55f
        const val ACTIVE = 1.725f
        const val VERY_ACTIVE = 1.9f
    }

    const val WATER_ML_PER_KG = 35

    object PrefsKeys {
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val USER_AGE = "user_age"
        const val IS_LOGGED_IN = "is_logged_in"
        const val DAILY_CALORIE_GOAL = "daily_calorie_goal"
        const val DAILY_WATER_GOAL = "daily_water_goal"
        const val USER_WEIGHT = "user_weight"
        const val USER_HEIGHT = "user_height"
        const val ACTIVITY_LEVEL = "activity_level"
        const val PREFERRED_UNITS = "preferred_units"
        const val NOTIFICATION_ENABLED = "notification_enabled"
        const val WATER_REMINDER_INTERVAL = "water_reminder_interval"
        const val MEAL_REMINDER_ENABLED = "meal_reminder_enabled"
        const val LAST_WEIGHT_UPDATE = "last_weight_update"
        const val FIRST_TIME_USER = "first_time_user"
        const val THEME_MODE = "theme_mode"
    }

    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_AGE = 13
    const val MAX_AGE = 120
    const val MIN_WEIGHT_KG = 30
    const val MAX_WEIGHT_KG = 300
    const val MIN_HEIGHT_CM = 100
    const val MAX_HEIGHT_CM = 250
    const val MIN_CALORIE_GOAL = 800
    const val MAX_CALORIE_GOAL = 5000
    const val MIN_WATER_GOAL_ML = 500
    const val MAX_WATER_GOAL_ML = 5000

    const val DATE_FORMAT_SIMPLE = "dd/MM/yyyy"
    const val DATE_FORMAT_FULL = "dd MMMM yyyy"
    const val TIME_FORMAT = "HH:mm"

    const val WATER_REMINDER_NOTIFICATION_ID = 1001
    const val MEAL_REMINDER_NOTIFICATION_ID = 1002
}
