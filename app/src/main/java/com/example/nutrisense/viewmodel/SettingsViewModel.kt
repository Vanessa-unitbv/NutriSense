package com.example.nutrisense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nutrisense.data.preferences.SharedPreferencesManager

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = SharedPreferencesManager.getInstance(application)

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
        _dailyCalorieGoal.value = preferencesManager.getDailyCalorieGoal()
        _dailyWaterGoal.value = preferencesManager.getDailyWaterGoal()
        _userWeight.value = preferencesManager.getUserWeight()
        _userHeight.value = preferencesManager.getUserHeight()
        _notificationEnabled.value = preferencesManager.isNotificationEnabled()
        _preferredUnits.value = preferencesManager.getPreferredUnits()
    }

    fun setDailyCalorieGoal(calories: Int) {
        preferencesManager.setDailyCalorieGoal(calories)
        _dailyCalorieGoal.value = calories
    }

    fun setDailyWaterGoal(waterMl: Int) {
        preferencesManager.setDailyWaterGoal(waterMl)
        _dailyWaterGoal.value = waterMl
    }

    fun setUserWeight(weight: Float) {
        preferencesManager.setUserWeight(weight)
        _userWeight.value = weight
    }

    fun setUserHeight(height: Float) {
        preferencesManager.setUserHeight(height)
        _userHeight.value = height
    }

    fun setActivityLevel(level: String) {
        preferencesManager.setActivityLevel(level)
    }

    fun getActivityLevel(): String = preferencesManager.getActivityLevel()

    fun setNotificationEnabled(enabled: Boolean) {
        preferencesManager.setNotificationEnabled(enabled)
        _notificationEnabled.value = enabled
    }

    fun setWaterReminderInterval(minutes: Int) {
        preferencesManager.setWaterReminderInterval(minutes)
    }

    fun getWaterReminderInterval(): Int = preferencesManager.getWaterReminderInterval()

    fun setMealReminderEnabled(enabled: Boolean) {
        preferencesManager.setMealReminderEnabled(enabled)
    }

    fun isMealReminderEnabled(): Boolean = preferencesManager.isMealReminderEnabled()

    fun setPreferredUnits(units: String) {
        preferencesManager.setPreferredUnits(units)
        _preferredUnits.value = units
    }

    fun setThemeMode(mode: String) {
        preferencesManager.setThemeMode(mode)
    }

    fun getThemeMode(): String = preferencesManager.getThemeMode()

    fun getBMI(): Float {
        val weight = _userWeight.value ?: 0f
        val height = _userHeight.value ?: 0f

        return if (weight > 0 && height > 0) {
            val heightInMeters = if (_preferredUnits.value == "imperial") {
                height * 0.0254f
            } else {
                height / 100f
            }
            weight / (heightInMeters * heightInMeters)
        } else {
            0f
        }
    }

    fun getRecommendedCalorieGoal(age: Int, gender: String): Int {
        val weight = _userWeight.value ?: 70f
        val height = _userHeight.value ?: 170f
        val activityLevel = getActivityLevel()

        val bmr = if (gender.lowercase() == "male") {
            88.362f + (13.397f * weight) + (4.799f * height) - (5.677f * age)
        } else {
            447.593f + (9.247f * weight) + (3.098f * height) - (4.330f * age)
        }

        val activityMultiplier = when (activityLevel) {
            "sedentary" -> 1.2f
            "light" -> 1.375f
            "moderate" -> 1.55f
            "active" -> 1.725f
            "very_active" -> 1.9f
            else -> 1.55f
        }

        return (bmr * activityMultiplier).toInt()
    }

    fun getRecommendedWaterGoal(): Int {
        val weight = _userWeight.value ?: 70f
        return (weight * 35).toInt()
    }
}