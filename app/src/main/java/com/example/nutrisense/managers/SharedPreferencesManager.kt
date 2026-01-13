package com.example.nutrisense.managers

import android.content.Context
import android.content.SharedPreferences
import com.example.nutrisense.utils.AppConstants
import com.example.nutrisense.data.preferences.UserPreferences
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPreferencesManager private constructor(context: Context, userEmail: String? = null) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        getPrefsName(userEmail), Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "nutrisense_preferences"
        private const val GLOBAL_PREFS_NAME = "nutrisense_global_preferences"

        @Volatile
        private var INSTANCE: SharedPreferencesManager? = null

        @Volatile
        private var currentUserEmail: String? = null

        fun getGlobalInstance(context: Context): SharedPreferencesManager {
            return SharedPreferencesManager(context.applicationContext, null)
        }

        fun getInstance(context: Context, userEmail: String? = null): SharedPreferencesManager {
            val email = userEmail ?: currentUserEmail
            return if (email != null) {
                SharedPreferencesManager(context.applicationContext, email)
            } else {
                getGlobalInstance(context)
            }
        }

        fun setCurrentUser(userEmail: String?) {
            currentUserEmail = userEmail
            INSTANCE = null
        }

        private fun hashEmail(email: String): String {
            return try {
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(email.lowercase().toByteArray(Charsets.UTF_8))
                digest.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                email.replace("@", "_").replace(".", "_")
            }
        }

        private fun getPrefsName(userEmail: String?): String {
            return if (userEmail != null) {
                "${PREFS_NAME}_${hashEmail(userEmail)}"
            } else {
                GLOBAL_PREFS_NAME
            }
        }
    }

    suspend fun setUserLoggedInAsync(email: String, name: String?) = withContext(Dispatchers.IO) {
        savePreferences {
            putBoolean(AppConstants.PrefsKeys.IS_LOGGED_IN, true)
            putString(AppConstants.PrefsKeys.USER_EMAIL, email)
            putString(AppConstants.PrefsKeys.USER_NAME, name)
        }
        setCurrentUser(email)
    }

    suspend fun setUserLoggedOutAsync() = withContext(Dispatchers.IO) {
        savePreferences {
            putBoolean(AppConstants.PrefsKeys.IS_LOGGED_IN, false)
            remove(AppConstants.PrefsKeys.USER_EMAIL)
            remove(AppConstants.PrefsKeys.USER_NAME)
        }
        setCurrentUser(null)
    }

    suspend fun setUserWeightAsync(weight: Float) = withContext(Dispatchers.IO) {
        savePreferences {
            putFloat(AppConstants.PrefsKeys.USER_WEIGHT, weight)
            putLong(AppConstants.PrefsKeys.LAST_WEIGHT_UPDATE, System.currentTimeMillis())
        }
    }

    suspend fun setUserHeightAsync(height: Float) = withContext(Dispatchers.IO) {
        saveFloat(AppConstants.PrefsKeys.USER_HEIGHT, height)
    }

    suspend fun setUserAgeAsync(age: Int) = withContext(Dispatchers.IO) {
        saveInt(AppConstants.PrefsKeys.USER_AGE, age)
    }

    suspend fun setDailyCalorieGoalAsync(goal: Int) = withContext(Dispatchers.IO) {
        saveInt(AppConstants.PrefsKeys.DAILY_CALORIE_GOAL, goal)
    }

    suspend fun setDailyWaterGoalAsync(goal: Int) = withContext(Dispatchers.IO) {
        saveInt(AppConstants.PrefsKeys.DAILY_WATER_GOAL, goal)
    }

    suspend fun setActivityLevelAsync(level: String) = withContext(Dispatchers.IO) {
        saveString(AppConstants.PrefsKeys.ACTIVITY_LEVEL, level)
    }

    suspend fun setPreferredUnitsAsync(units: String) = withContext(Dispatchers.IO) {
        saveString(AppConstants.PrefsKeys.PREFERRED_UNITS, units)
    }

    suspend fun setNotificationEnabledAsync(enabled: Boolean) = withContext(Dispatchers.IO) {
        saveBoolean(AppConstants.PrefsKeys.NOTIFICATION_ENABLED, enabled) // Removed the 'S'
    }

    suspend fun setWaterReminderIntervalAsync(interval: Int) = withContext(Dispatchers.IO) {
        saveInt(AppConstants.PrefsKeys.WATER_REMINDER_INTERVAL, interval)
    }

    // Read operations - also async to ensure consistency
    suspend fun getUserEmailAsync(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(AppConstants.PrefsKeys.USER_EMAIL, null)
    }

    suspend fun getUserWeightAsync(): Float = withContext(Dispatchers.IO) {
        getFloat(AppConstants.PrefsKeys.USER_WEIGHT, 0f)
    }

    suspend fun getUserHeightAsync(): Float = withContext(Dispatchers.IO) {
        getFloat(AppConstants.PrefsKeys.USER_HEIGHT, 0f)
    }

    suspend fun getUserAgeAsync(): Int = withContext(Dispatchers.IO) {
        getInt(AppConstants.PrefsKeys.USER_AGE, 0)
    }

    suspend fun getDailyCalorieGoalAsync(): Int = withContext(Dispatchers.IO) {
        getInt(AppConstants.PrefsKeys.DAILY_CALORIE_GOAL, 2000)
    }

    suspend fun getDailyWaterGoalAsync(): Int = withContext(Dispatchers.IO) {
        getInt(AppConstants.PrefsKeys.DAILY_WATER_GOAL, 2000)
    }

    suspend fun getActivityLevelAsync(): String = withContext(Dispatchers.IO) {
        getString(AppConstants.PrefsKeys.ACTIVITY_LEVEL, "moderate") ?: "moderate"
    }

    suspend fun getPreferredUnitsAsync(): String = withContext(Dispatchers.IO) {
        getString(AppConstants.PrefsKeys.PREFERRED_UNITS, "metric") ?: "metric"
    }

    suspend fun isNotificationEnabledAsync(): Boolean = withContext(Dispatchers.IO) {
        getBoolean(AppConstants.PrefsKeys.NOTIFICATION_ENABLED, true)
    }

    suspend fun getWaterReminderIntervalAsync(): Int = withContext(Dispatchers.IO) {
        getInt(AppConstants.PrefsKeys.WATER_REMINDER_INTERVAL, 60)
    }

    // Synchronous versions (kept for backward compatibility in UI operations)
    fun setUserLoggedIn(email: String, name: String?) {
        savePreferences {
            putBoolean(AppConstants.PrefsKeys.IS_LOGGED_IN, true)
            putString(AppConstants.PrefsKeys.USER_EMAIL, email)
            putString(AppConstants.PrefsKeys.USER_NAME, name)
        }
        setCurrentUser(email)
    }

    fun setUserLoggedOut() {
        savePreferences {
            putBoolean(AppConstants.PrefsKeys.IS_LOGGED_IN, false)
            remove(AppConstants.PrefsKeys.USER_EMAIL)
            remove(AppConstants.PrefsKeys.USER_NAME)
        }
        setCurrentUser(null)
    }

    fun isUserLoggedIn(): Boolean =
        sharedPreferences.getBoolean(AppConstants.PrefsKeys.IS_LOGGED_IN, false)

    fun getUserEmail(): String? =
        sharedPreferences.getString(AppConstants.PrefsKeys.USER_EMAIL, null)

    fun getUserName(): String? =
        sharedPreferences.getString(AppConstants.PrefsKeys.USER_NAME, null)

    fun setUserAge(age: Int) = saveInt(AppConstants.PrefsKeys.USER_AGE, age)
    fun getUserAge(): Int = getInt(AppConstants.PrefsKeys.USER_AGE, 0)

    fun setUserWeight(weight: Float) {
        savePreferences {
            putFloat(AppConstants.PrefsKeys.USER_WEIGHT, weight)
            putLong(AppConstants.PrefsKeys.LAST_WEIGHT_UPDATE, System.currentTimeMillis())
        }
    }
    fun getUserWeight(): Float = getFloat(AppConstants.PrefsKeys.USER_WEIGHT, 0f)

    fun setUserHeight(height: Float) = saveFloat(AppConstants.PrefsKeys.USER_HEIGHT, height)
    fun getUserHeight(): Float = getFloat(AppConstants.PrefsKeys.USER_HEIGHT, 0f)

    fun getLastWeightUpdate(): Long = getLong(AppConstants.PrefsKeys.LAST_WEIGHT_UPDATE, 0L)

    fun setDailyCalorieGoal(calories: Int) =
        saveInt(AppConstants.PrefsKeys.DAILY_CALORIE_GOAL, calories)

    fun getDailyCalorieGoal(): Int =
        getInt(AppConstants.PrefsKeys.DAILY_CALORIE_GOAL, AppConstants.DEFAULT_CALORIE_GOAL)

    fun setDailyWaterGoal(waterMl: Int) =
        saveInt(AppConstants.PrefsKeys.DAILY_WATER_GOAL, waterMl)

    fun getDailyWaterGoal(): Int =
        getInt(AppConstants.PrefsKeys.DAILY_WATER_GOAL, AppConstants.DEFAULT_WATER_GOAL_ML)

    fun setActivityLevel(level: String) = saveString(AppConstants.PrefsKeys.ACTIVITY_LEVEL, level)
    fun getActivityLevel(): String =
        getString(AppConstants.PrefsKeys.ACTIVITY_LEVEL, AppConstants.DEFAULT_ACTIVITY_LEVEL)

    fun setPreferredUnits(units: String) = saveString(AppConstants.PrefsKeys.PREFERRED_UNITS, units)
    fun getPreferredUnits(): String =
        getString(AppConstants.PrefsKeys.PREFERRED_UNITS, AppConstants.DEFAULT_UNITS)

    fun setNotificationEnabled(enabled: Boolean) =
        saveBoolean(AppConstants.PrefsKeys.NOTIFICATION_ENABLED, enabled)

    fun isNotificationEnabled(): Boolean =
        getBoolean(AppConstants.PrefsKeys.NOTIFICATION_ENABLED, true)

    fun setWaterReminderInterval(minutes: Int) =
        saveInt(AppConstants.PrefsKeys.WATER_REMINDER_INTERVAL, minutes)

    fun getWaterReminderInterval(): Int =
        getInt(AppConstants.PrefsKeys.WATER_REMINDER_INTERVAL, AppConstants.DEFAULT_WATER_REMINDER_INTERVAL)

    fun setMealReminderEnabled(enabled: Boolean) =
        saveBoolean(AppConstants.PrefsKeys.MEAL_REMINDER_ENABLED, enabled)

    fun isMealReminderEnabled(): Boolean =
        getBoolean(AppConstants.PrefsKeys.MEAL_REMINDER_ENABLED, true)

    fun setFirstTimeUser(isFirstTime: Boolean) =
        saveBoolean(AppConstants.PrefsKeys.FIRST_TIME_USER, isFirstTime)

    fun isFirstTimeUser(): Boolean =
        getBoolean(AppConstants.PrefsKeys.FIRST_TIME_USER, true)

    fun setThemeMode(mode: String) = saveString(AppConstants.PrefsKeys.THEME_MODE, mode)
    fun getThemeMode(): String = getString(AppConstants.PrefsKeys.THEME_MODE, "system")

    fun getUserPreferences(): UserPreferences {
        return UserPreferences(
            email = getUserEmail(),
            name = getUserName(),
            isLoggedIn = isUserLoggedIn(),
            age = getUserAge(),
            dailyCalorieGoal = getDailyCalorieGoal(),
            dailyWaterGoal = getDailyWaterGoal(),
            weight = getUserWeight(),
            height = getUserHeight(),
            activityLevel = getActivityLevel(),
            preferredUnits = getPreferredUnits(),
            isNotificationEnabled = isNotificationEnabled(),
            waterReminderInterval = getWaterReminderInterval(),
            isMealReminderEnabled = isMealReminderEnabled(),
            lastWeightUpdate = getLastWeightUpdate(),
            isFirstTimeUser = isFirstTimeUser(),
            themeMode = getThemeMode()
        )
    }

    fun saveUserPreferences(preferences: UserPreferences) {
        savePreferences {
            preferences.email?.let { putString(AppConstants.PrefsKeys.USER_EMAIL, it) }
            preferences.name?.let { putString(AppConstants.PrefsKeys.USER_NAME, it) }
            putBoolean(AppConstants.PrefsKeys.IS_LOGGED_IN, preferences.isLoggedIn)
            putInt(AppConstants.PrefsKeys.USER_AGE, preferences.age)
            putInt(AppConstants.PrefsKeys.DAILY_CALORIE_GOAL, preferences.dailyCalorieGoal)
            putInt(AppConstants.PrefsKeys.DAILY_WATER_GOAL, preferences.dailyWaterGoal)
            putFloat(AppConstants.PrefsKeys.USER_WEIGHT, preferences.weight)
            putFloat(AppConstants.PrefsKeys.USER_HEIGHT, preferences.height)
            putString(AppConstants.PrefsKeys.ACTIVITY_LEVEL, preferences.activityLevel)
            putString(AppConstants.PrefsKeys.PREFERRED_UNITS, preferences.preferredUnits)
            putBoolean(AppConstants.PrefsKeys.NOTIFICATION_ENABLED, preferences.isNotificationEnabled)
            putInt(AppConstants.PrefsKeys.WATER_REMINDER_INTERVAL, preferences.waterReminderInterval)
            putBoolean(AppConstants.PrefsKeys.MEAL_REMINDER_ENABLED, preferences.isMealReminderEnabled)
            putLong(AppConstants.PrefsKeys.LAST_WEIGHT_UPDATE, preferences.lastWeightUpdate)
            putBoolean(AppConstants.PrefsKeys.FIRST_TIME_USER, preferences.isFirstTimeUser)
            putString(AppConstants.PrefsKeys.THEME_MODE, preferences.themeMode)
        }
    }

    fun clearAllPreferences() {
        sharedPreferences.edit().clear().apply()
    }

    fun clearUserData() {
        savePreferences {
            remove(AppConstants.PrefsKeys.USER_EMAIL)
            remove(AppConstants.PrefsKeys.USER_NAME)
            remove(AppConstants.PrefsKeys.IS_LOGGED_IN)
            remove(AppConstants.PrefsKeys.DAILY_CALORIE_GOAL)
            remove(AppConstants.PrefsKeys.DAILY_WATER_GOAL)
            remove(AppConstants.PrefsKeys.USER_WEIGHT)
            remove(AppConstants.PrefsKeys.USER_HEIGHT)
            remove(AppConstants.PrefsKeys.USER_AGE)
            remove(AppConstants.PrefsKeys.ACTIVITY_LEVEL)
            remove(AppConstants.PrefsKeys.LAST_WEIGHT_UPDATE)
        }
    }

    private inline fun savePreferences(block: SharedPreferences.Editor.() -> Unit) {
        sharedPreferences.edit().apply(block).apply()
    }

    private fun saveString(key: String, value: String) =
        savePreferences { putString(key, value) }

    private fun saveInt(key: String, value: Int) =
        savePreferences { putInt(key, value) }

    private fun saveFloat(key: String, value: Float) =
        savePreferences { putFloat(key, value) }

    private fun saveBoolean(key: String, value: Boolean) =
        savePreferences { putBoolean(key, value) }

    private fun saveLong(key: String, value: Long) =
        savePreferences { putLong(key, value) }

    private fun getString(key: String, defaultValue: String): String =
        sharedPreferences.getString(key, defaultValue) ?: defaultValue

    private fun getInt(key: String, defaultValue: Int): Int =
        sharedPreferences.getInt(key, defaultValue)

    private fun getFloat(key: String, defaultValue: Float): Float =
        sharedPreferences.getFloat(key, defaultValue)

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    private fun getLong(key: String, defaultValue: Long): Long =
        sharedPreferences.getLong(key, defaultValue)
}