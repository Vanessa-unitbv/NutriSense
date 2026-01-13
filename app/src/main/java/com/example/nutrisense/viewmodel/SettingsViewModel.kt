package com.example.nutrisense.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.managers.PreferencesRepository
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.utils.AppConstants
import com.example.nutrisense.utils.NutritionCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
        loadCurrentSettingsAsync()
    }

    private fun initializeUserPreferences() {
        val currentUserEmail = globalPreferencesManager.getUserEmail()
        userPreferencesManager = preferencesRepository.getManagerForUser(currentUserEmail)
    }

    private fun resolveUserPrefs(): SharedPreferencesManager {
        val email = globalPreferencesManager.getUserEmail()
        return preferencesRepository.getManagerForUser(email)
    }

    private fun loadCurrentSettingsAsync() {
        viewModelScope.launch {
            try {
                val prefs = resolveUserPrefs()

                // Use async to load multiple preferences in parallel
                val calorieGoalAsync = async { prefs.getDailyCalorieGoal() }
                val waterGoalAsync = async { prefs.getDailyWaterGoal() }
                val weightAsync = async { prefs.getUserWeight() }
                val heightAsync = async { prefs.getUserHeight() }
                val ageAsync = async { prefs.getUserAge() }
                val activityLevelAsync = async { prefs.getActivityLevel() }
                val unitsAsync = async { prefs.getPreferredUnits() }
                val notificationAsync = async { prefs.isNotificationEnabled() }
                val waterReminderAsync = async { prefs.getWaterReminderInterval() }
                val mealReminderAsync = async { prefs.isMealReminderEnabled() }

                _uiState.value = _uiState.value.copy(
                    dailyCalorieGoal = calorieGoalAsync.await(),
                    dailyWaterGoal = waterGoalAsync.await(),
                    userWeight = weightAsync.await(),
                    userHeight = heightAsync.await(),
                    userAge = ageAsync.await(),
                    activityLevel = activityLevelAsync.await(),
                    preferredUnits = unitsAsync.await(),
                    notificationEnabled = notificationAsync.await(),
                    waterReminderInterval = waterReminderAsync.await(),
                    mealReminderEnabled = mealReminderAsync.await()
                )

                calculateAndUpdateBMI()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading settings", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error loading settings: ${e.message}"
                )
            }
        }
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

                // Use async for parallel calculations
                val bmrAsync = async { NutritionCalculator.calculateBMR(weightKg, heightCm, age, gender) }
                val caloriesAsync = async { NutritionCalculator.calculateDailyCalorieNeeds(bmrAsync.await(), activityLevel) }
                val waterAsync = async { NutritionCalculator.calculateWaterIntake(weightKg, activityLevel) }

                val calories = caloriesAsync.await()
                val water = waterAsync.await()

                _uiState.value = _uiState.value.copy(
                    dailyCalorieGoal = calories,
                    dailyWaterGoal = water,
                    successMessage = "Goals calculated successfully!"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error calculating goals", e)
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

                // Launch all async preference save operations in parallel
                val saveCalorieAsync = async { prefs.setDailyCalorieGoal(calorieGoal) }
                val saveWaterAsync = async { prefs.setDailyWaterGoal(waterGoal) }
                val saveWeightAsync = async { weight?.let { prefs.setUserWeight(it) } }
                val saveHeightAsync = async { height?.let { prefs.setUserHeight(it) } }
                val saveAgeAsync = async { age?.let { prefs.setUserAge(it) } }
                val saveActivityAsync = async { prefs.setActivityLevel(activityLevel) }
                val saveUnitsAsync = async { prefs.setPreferredUnits(units) }
                val saveNotificationAsync = async { prefs.setNotificationEnabled(notificationsEnabled) }
                val saveWaterIntervalAsync = async { prefs.setWaterReminderInterval(waterInterval) }

                // Wait for all save operations to complete
                saveCalorieAsync.await()
                saveWaterAsync.await()
                saveWeightAsync.await()
                saveHeightAsync.await()
                saveAgeAsync.await()
                saveActivityAsync.await()
                saveUnitsAsync.await()
                saveNotificationAsync.await()
                saveWaterIntervalAsync.await()

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
                Log.e(TAG, "Error saving settings", e)
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