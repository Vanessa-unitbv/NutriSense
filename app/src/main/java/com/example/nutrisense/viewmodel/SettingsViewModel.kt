package com.example.nutrisense.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.managers.PreferencesRepository
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.utils.AppConstants
import com.example.nutrisense.utils.NutritionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val globalPreferencesManager: SharedPreferencesManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var userPreferencesManager: SharedPreferencesManager = globalPreferencesManager

    init {
        initializeUserPreferences()
        loadCurrentSettings()
    }

    private fun initializeUserPreferences() {
        val currentUserEmail = globalPreferencesManager.getUserEmail()
        userPreferencesManager = preferencesRepository.getManagerForUser(currentUserEmail)
    }

    private fun resolveUserPrefs(): SharedPreferencesManager {
        val email = globalPreferencesManager.getUserEmail()
        return preferencesRepository.getManagerForUser(email)
    }

    private fun loadCurrentSettings() {
        val prefs = resolveUserPrefs()

        _uiState.value = _uiState.value.copy(
            dailyCalorieGoal = prefs.getDailyCalorieGoal(),
            dailyWaterGoal = prefs.getDailyWaterGoal(),
            userWeight = prefs.getUserWeight(),
            userHeight = prefs.getUserHeight(),
            userAge = prefs.getUserAge(),
            activityLevel = prefs.getActivityLevel(),
            preferredUnits = prefs.getPreferredUnits(),
            notificationEnabled = prefs.isNotificationEnabled(),
            waterReminderInterval = prefs.getWaterReminderInterval(),
            mealReminderEnabled = prefs.isMealReminderEnabled()
        )

        calculateAndUpdateBMI()
    }

    fun calculateAndUpdateBMI() {
        val weight = _uiState.value.userWeight
        val height = _uiState.value.userHeight
        val units = _uiState.value.preferredUnits

        if (weight > 0 && height > 0) {
            val bmi = NutritionCalculator.calculateBMIWithUnits(weight, height, units)
            val bmiCategory = NutritionCalculator.getBMICategory(bmi)
            val bmiColorResource = NutritionCalculator.getBMIColorResource(bmi)

            _uiState.value = _uiState.value.copy(
                bmiValue = bmi,
                bmiCategory = bmiCategory,
                bmiColorResource = bmiColorResource,
                bmiDisplayText = "BMI: %.1f (%s)".format(bmi, bmiCategory)
            )
        } else {
            _uiState.value = _uiState.value.copy(
                bmiValue = 0f,
                bmiCategory = "",
                bmiColorResource = android.R.color.tertiary_text_light,
                bmiDisplayText = "BMI: Enter weight and height"
            )
        }
    }

    fun calculateRecommendedGoals(
        weight: Float,
        height: Float,
        age: Int,
        gender: String,
        activityLevel: String,
        units: String
    ) {
        Log.d(TAG, "calculateRecommendedGoals called with activityLevel=$activityLevel")
        _uiState.value = _uiState.value.copy(activityLevel = activityLevel)

        // Validare
        val validation = validatePhysicalData(weight, height, age, units)
        if (validation is ValidationResult.Error) {
            _uiState.value = _uiState.value.copy(
                errorMessage = validation.message
            )
            return
        }

        viewModelScope.launch {
            try {
                val weightKg = if (units == AppConstants.UNITS_IMPERIAL) {
                    weight * AppConstants.LBS_TO_KG
                } else {
                    weight
                }

                val heightCm = if (units == AppConstants.UNITS_IMPERIAL) {
                    height * 2.54f
                } else {
                    height
                }

                val bmr = NutritionCalculator.calculateBMR(weightKg, heightCm, age, gender)
                val recommendedCalories = NutritionCalculator.calculateDailyCalorieNeeds(bmr, activityLevel)
                val recommendedWater = NutritionCalculator.calculateWaterIntake(weightKg, activityLevel)

                _uiState.value = _uiState.value.copy(
                    dailyCalorieGoal = recommendedCalories,
                    dailyWaterGoal = recommendedWater,
                    successMessage = "Goals calculated successfully!"
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error calculating goals: ${e.message}"
                )
            }
        }
    }

    fun updateActivityLevel(level: String) {
        Log.d(TAG, "updateActivityLevel called with level=$level")
        _uiState.value = _uiState.value.copy(activityLevel = level)
    }

    fun saveAllSettings(
        calorieGoal: Int,
        waterGoal: Int,
        weight: Float?,
        height: Float?,
        age: Int?,
        activityLevel: String,
        units: String,
        notificationsEnabled: Boolean,
        waterInterval: Int
    ) {
        val calorieValidation = validateCalorieGoal(calorieGoal)
        if (calorieValidation is ValidationResult.Error) {
            _uiState.value = _uiState.value.copy(errorMessage = calorieValidation.message)
            return
        }

        val waterValidation = validateWaterGoal(waterGoal)
        if (waterValidation is ValidationResult.Error) {
            _uiState.value = _uiState.value.copy(errorMessage = waterValidation.message)
            return
        }

        weight?.let { w ->
            val weightValidation = validateWeight(w, units)
            if (weightValidation is ValidationResult.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = weightValidation.message)
                return
            }
        }

        height?.let { h ->
            val heightValidation = validateHeight(h, units)
            if (heightValidation is ValidationResult.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = heightValidation.message)
                return
            }
        }

        age?.let { a ->
            val ageValidation = validateAge(a)
            if (ageValidation is ValidationResult.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = ageValidation.message)
                return
            }
        }

        viewModelScope.launch {
            try {
                val prefs = resolveUserPrefs()

                prefs.setDailyCalorieGoal(calorieGoal)
                prefs.setDailyWaterGoal(waterGoal)

                weight?.let { prefs.setUserWeight(it) }
                height?.let { prefs.setUserHeight(it) }
                age?.let { prefs.setUserAge(it) }

                prefs.setActivityLevel(activityLevel)
                prefs.setPreferredUnits(units)
                prefs.setNotificationEnabled(notificationsEnabled)
                prefs.setWaterReminderInterval(waterInterval)

                _uiState.value = _uiState.value.copy(
                    dailyCalorieGoal = calorieGoal,
                    dailyWaterGoal = waterGoal,
                    userWeight = weight ?: 0f,
                    userHeight = height ?: 0f,
                    userAge = age ?: 0,
                    activityLevel = activityLevel,
                    preferredUnits = units,
                    notificationEnabled = notificationsEnabled,
                    waterReminderInterval = waterInterval,
                    successMessage = "Settings saved successfully!"
                )

                calculateAndUpdateBMI()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error saving settings: ${e.message}"
                )
            }
        }
    }

    fun updateUnits(units: String) {
        _uiState.value = _uiState.value.copy(preferredUnits = units)
        calculateAndUpdateBMI()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }


    private fun validateCalorieGoal(calories: Int): ValidationResult {
        return when {
            calories < AppConstants.MIN_CALORIE_GOAL -> {
                ValidationResult.Error("Calorie goal must be at least ${AppConstants.MIN_CALORIE_GOAL}")
            }
            calories > AppConstants.MAX_CALORIE_GOAL -> {
                ValidationResult.Error("Calorie goal cannot exceed ${AppConstants.MAX_CALORIE_GOAL}")
            }
            else -> ValidationResult.Success
        }
    }

    private fun validateWaterGoal(waterMl: Int): ValidationResult {
        return when {
            waterMl < AppConstants.MIN_WATER_GOAL_ML -> {
                ValidationResult.Error("Water goal must be at least ${AppConstants.MIN_WATER_GOAL_ML} ml")
            }
            waterMl > AppConstants.MAX_WATER_GOAL_ML -> {
                ValidationResult.Error("Water goal cannot exceed ${AppConstants.MAX_WATER_GOAL_ML} ml")
            }
            else -> ValidationResult.Success
        }
    }

    private fun validateWeight(weight: Float, units: String): ValidationResult {
        return if (NutritionCalculator.Validator.isValidWeight(weight, units)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Invalid weight for selected units")
        }
    }

    private fun validateHeight(height: Float, units: String): ValidationResult {
        return if (NutritionCalculator.Validator.isValidHeight(height, units)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Invalid height for selected units")
        }
    }

    private fun validateAge(age: Int): ValidationResult {
        return if (NutritionCalculator.Validator.isValidAge(age)) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Age must be between ${AppConstants.MIN_AGE} and ${AppConstants.MAX_AGE}")
        }
    }

    private fun validatePhysicalData(
        weight: Float,
        height: Float,
        age: Int,
        units: String
    ): ValidationResult {
        validateWeight(weight, units).let {
            if (it is ValidationResult.Error) return it
        }
        validateHeight(height, units).let {
            if (it is ValidationResult.Error) return it
        }
        validateAge(age).let {
            if (it is ValidationResult.Error) return it
        }
        return ValidationResult.Success
    }

    data class SettingsUiState(
        val dailyCalorieGoal: Int = AppConstants.DEFAULT_CALORIE_GOAL,
        val dailyWaterGoal: Int = AppConstants.DEFAULT_WATER_GOAL_ML,
        val userWeight: Float = 0f,
        val userHeight: Float = 0f,
        val userAge: Int = 0,
        val activityLevel: String = AppConstants.DEFAULT_ACTIVITY_LEVEL,
        val preferredUnits: String = AppConstants.DEFAULT_UNITS,
        val notificationEnabled: Boolean = true,
        val waterReminderInterval: Int = AppConstants.DEFAULT_WATER_REMINDER_INTERVAL,
        val mealReminderEnabled: Boolean = true,
        val bmiValue: Float = 0f,
        val bmiCategory: String = "",
        val bmiColorResource: Int = android.R.color.tertiary_text_light,
        val bmiDisplayText: String = "BMI: Enter weight and height",
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}