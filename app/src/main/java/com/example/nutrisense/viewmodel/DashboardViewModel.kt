package com.example.nutrisense.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.data.repository.FoodRepository
import com.example.nutrisense.managers.PreferencesRepository
import com.example.nutrisense.managers.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentEmail: String? = null
    private var currentUserId: Long? = null

    fun loadDashboard(email: String) {
        currentEmail = email
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading dashboard for $email")

                // Get user preferences
                val userPrefs = preferencesRepository.getManagerForUser(email)
                SharedPreferencesManager.setCurrentUser(email)

                // Get user ID for food queries
                val userId = foodRepository.getUserIdByEmail(email)
                currentUserId = userId

                // Load goals from preferences
                val calorieGoal = userPrefs.getDailyCalorieGoal()
                val waterGoal = userPrefs.getDailyWaterGoal()
                val userName = userPrefs.getUserName() ?: email.substringBefore("@")

                Log.d(TAG, "User goals: calories=$calorieGoal, water=$waterGoal")

                // Update UI state with goals
                _uiState.value = DashboardUiState(
                    userName = userName,
                    dailyCalorieGoal = calorieGoal,
                    dailyWaterGoal = waterGoal,
                    isLoading = false
                )

                // Load today's consumed foods
                if (userId != null) {
                    loadTodayConsumedFoods(userId)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading dashboard", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error loading dashboard: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadTodayConsumedFoods(userId: Long) {
        viewModelScope.launch {
            try {
                foodRepository.getTodayConsumedFoodsForUser(userId).collect { foods ->
                    updateConsumedNutrition(foods)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading today's foods", e)
            }
        }
    }

    private fun updateConsumedNutrition(foods: List<Food>) {
        val totalCalories = foods.sumOf { it.calories.toInt() }
        val totalProtein = foods.sumOf { it.proteinG.toInt() }
        val totalCarbs = foods.sumOf { it.carbohydratesTotalG.toInt() }
        val totalFat = foods.sumOf { it.fatTotalG.toInt() }

        Log.d(TAG, "Updating consumed nutrition: calories=$totalCalories, protein=$totalProtein, carbs=$totalCarbs, fat=$totalFat")

        _uiState.value = _uiState.value.copy(
            dailyCaloriesConsumed = totalCalories,
            dailyProtein = totalProtein,
            dailyCarbs = totalCarbs,
            dailyFat = totalFat
        )
    }

    fun refresh() {
        currentEmail?.let { loadDashboard(it) }
    }

    data class DashboardUiState(
        val userName: String = "User",
        val dailyCalorieGoal: Int = 2000,
        val dailyCaloriesConsumed: Int = 0,
        val dailyWaterGoal: Int = 2000,
        val dailyWaterConsumed: Int = 0,
        val dailyProtein: Int = 0,
        val dailyCarbs: Int = 0,
        val dailyFat: Int = 0,
        val isLoading: Boolean = true,
        val errorMessage: String? = null
    )
}
