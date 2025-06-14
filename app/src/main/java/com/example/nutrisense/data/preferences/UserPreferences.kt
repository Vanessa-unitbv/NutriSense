package com.example.nutrisense.data.preferences

data class UserPreferences(
    val email: String? = null,
    val name: String? = null,
    val isLoggedIn: Boolean = false,
    val age: Int = 0,
    val dailyCalorieGoal: Int = 2000,
    val dailyWaterGoal: Int = 2000,
    val weight: Float = 0f,
    val height: Float = 0f,
    val activityLevel: String = "moderate",
    val preferredUnits: String = "metric",
    val isNotificationEnabled: Boolean = true,
    val waterReminderInterval: Int = 60,
    val isMealReminderEnabled: Boolean = true,
    val lastWeightUpdate: Long = 0L,
    val isFirstTimeUser: Boolean = true,
    val themeMode: String = "system"
)