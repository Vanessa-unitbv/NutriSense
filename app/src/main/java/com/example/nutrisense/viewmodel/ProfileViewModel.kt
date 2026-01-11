@file:Suppress("DEPRECATION")

package com.example.nutrisense.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.managers.PreferencesRepository
import com.example.nutrisense.utils.ProfileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val profileUiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentEmail: String? = null

    fun loadProfile(email: String) {
        currentEmail = email
        viewModelScope.launch {
            try {
                val userPrefs = preferencesRepository.getManagerForUser(email)
                userPrefs.let { com.example.nutrisense.managers.SharedPreferencesManager.setCurrentUser(email) }

                Log.d(TAG, "Loading profile for $email: weight=${userPrefs.getUserWeight()}, height=${userPrefs.getUserHeight()}, age=${userPrefs.getUserAge()}, calorieGoal=${userPrefs.getDailyCalorieGoal()}")

                val calorieText = "Daily Calorie Goal: ${userPrefs.getDailyCalorieGoal()} kcal"
                val waterText = "Daily Water Goal: ${userPrefs.getDailyWaterGoal()} ml"
                val weightDisplay = ProfileUtils.formatWeightDisplay(userPrefs)
                val (bmiText, bmiColor) = ProfileUtils.getBMIDisplay(userPrefs)
                val lastUpdate = userPrefs.getLastWeightUpdate()
                val lastUpdateText = if (lastUpdate > 0) {
                    "Last weight update: ${ProfileUtils.formatTimeAgo(lastUpdate)}"
                } else {
                    "Last weight update: Never"
                }
                val settingsButtonText = ProfileUtils.getSettingsButtonText(userPrefs)

                _uiState.value = ProfileUiState(
                    email = email,
                    welcomeTitle = if (userPrefs.getUserName()?.isNotEmpty() == true) "${userPrefs.getUserName()}'s Profile" else "My Profile",
                    dailyCalorieGoalText = calorieText,
                    dailyWaterGoalText = waterText,
                    weightDisplay = weightDisplay,
                    bmiText = bmiText,
                    bmiColor = bmiColor,
                    lastUpdateText = lastUpdateText,
                    settingsButtonText = settingsButtonText,
                    rawWeight = userPrefs.getUserWeight()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile for $email", e)
                _uiState.value = _uiState.value.copy(
                    email = email,
                    dailyCalorieGoalText = "Daily Calorie Goal: -",
                    dailyWaterGoalText = "Daily Water Goal: -",
                    weightDisplay = "Weight: -",
                    bmiText = "BMI: -",
                    lastUpdateText = "Last weight update: -"
                )
            }
        }
    }

    fun refresh() {
        currentEmail?.let { loadProfile(it) }
    }

    data class ProfileUiState(
        val email: String = "",
        val welcomeTitle: String = "My Profile",
        val dailyCalorieGoalText: String = "",
        val dailyWaterGoalText: String = "",
        val weightDisplay: String = "",
        val bmiText: String = "",
        val bmiColor: Int = android.R.color.tertiary_text_light,
        val lastUpdateText: String = "",
        val settingsButtonText: String = "",
        val rawWeight: Float = 0f
    )
}
