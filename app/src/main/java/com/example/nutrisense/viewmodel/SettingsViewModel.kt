package com.example.nutrisense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nutrisense.managers.SharedPreferencesManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val globalPreferencesManager = SharedPreferencesManager.getGlobalInstance(application)
    private val userPreferencesManager: SharedPreferencesManager

    init {
        val currentUserEmail = globalPreferencesManager.getUserEmail()
        userPreferencesManager = if (currentUserEmail != null) {
            SharedPreferencesManager.getInstance(application, currentUserEmail)
        } else {
            SharedPreferencesManager.getGlobalInstance(application)
        }
    }

    private val _dailyCalorieGoal = MutableLiveData<Int>()
    val dailyCalorieGoal: LiveData<Int> = _dailyCalorieGoal

    private val _dailyWaterGoal = MutableLiveData<Int>()
    val dailyWaterGoal: LiveData<Int> = _dailyWaterGoal

    private val _userWeight = MutableLiveData<Float>()
    val userWeight: LiveData<Float> = _userWeight

    private val _userHeight = MutableLiveData<Float>()
    val userHeight: LiveData<Float> = _userHeight

    private val _notificationEnabled = MutableLiveData<Boolean>()
    val notificationEnabled: LiveData<Boolean> = _notificationEnabled

    private val _preferredUnits = MutableLiveData<String>()
    val preferredUnits: LiveData<String> = _preferredUnits

    init {
        loadCurrentSettings()
    }

    private fun loadCurrentSettings() {
        _dailyCalorieGoal.value = userPreferencesManager.getDailyCalorieGoal()
        _dailyWaterGoal.value = userPreferencesManager.getDailyWaterGoal()
        _userWeight.value = userPreferencesManager.getUserWeight()
        _userHeight.value = userPreferencesManager.getUserHeight()
        _notificationEnabled.value = userPreferencesManager.isNotificationEnabled()
        _preferredUnits.value = userPreferencesManager.getPreferredUnits()
    }

    fun setDailyCalorieGoal(calories: Int) {
        userPreferencesManager.setDailyCalorieGoal(calories)
        _dailyCalorieGoal.value = calories
    }

    fun setDailyWaterGoal(waterMl: Int) {
        userPreferencesManager.setDailyWaterGoal(waterMl)
        _dailyWaterGoal.value = waterMl
    }

    fun setUserWeight(weight: Float) {
        userPreferencesManager.setUserWeight(weight)
        _userWeight.value = weight
    }

    fun setUserHeight(height: Float) {
        userPreferencesManager.setUserHeight(height)
        _userHeight.value = height
    }

    fun setActivityLevel(level: String) {
        userPreferencesManager.setActivityLevel(level)
    }

    fun getActivityLevel(): String = userPreferencesManager.getActivityLevel()

    fun setNotificationEnabled(enabled: Boolean) {
        userPreferencesManager.setNotificationEnabled(enabled)
        _notificationEnabled.value = enabled
    }

    fun setWaterReminderInterval(minutes: Int) {
        userPreferencesManager.setWaterReminderInterval(minutes)
    }

    fun getWaterReminderInterval(): Int = userPreferencesManager.getWaterReminderInterval()

    fun setMealReminderEnabled(enabled: Boolean) {
        userPreferencesManager.setMealReminderEnabled(enabled)
    }

    fun isMealReminderEnabled(): Boolean = userPreferencesManager.isMealReminderEnabled()

    fun setPreferredUnits(units: String) {
        userPreferencesManager.setPreferredUnits(units)
        _preferredUnits.value = units
    }

    fun setThemeMode(mode: String) {
        userPreferencesManager.setThemeMode(mode)
    }

    fun getThemeMode(): String = userPreferencesManager.getThemeMode()

    fun getUserAge(): Int = userPreferencesManager.getUserAge()

    fun setUserAge(age: Int) = userPreferencesManager.setUserAge(age)
}