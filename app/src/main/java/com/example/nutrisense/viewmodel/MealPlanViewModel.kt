package com.example.nutrisense.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.dao.MealPlanWithRecipeData
import com.example.nutrisense.data.entity.DayOfWeek
import com.example.nutrisense.data.entity.MealPlan
import com.example.nutrisense.data.entity.MealType
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.data.repository.MealPlanRepository
import com.example.nutrisense.data.repository.RecipeRepository
import com.example.nutrisense.managers.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val globalPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "MealPlanViewModel"
    }

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _allMealPlans = MutableStateFlow<LiveData<List<MealPlanWithRecipeData>>?>(null)
    val allMealPlans: StateFlow<LiveData<List<MealPlanWithRecipeData>>?> = _allMealPlans.asStateFlow()

    private val _userRecipes = MutableStateFlow<LiveData<List<Recipe>>?>(null)
    val userRecipes: StateFlow<LiveData<List<Recipe>>?> = _userRecipes.asStateFlow()

    private val _selectedDay = MutableStateFlow(DayOfWeek.MONDAY)
    val selectedDay: StateFlow<DayOfWeek> = _selectedDay.asStateFlow()

    init {
        loadCurrentUserAsync()
    }

    private fun loadCurrentUserAsync() {
        viewModelScope.launch {
            try {
                val email = globalPreferencesManager.getUserEmail()
                email?.let {
                    val userId = mealPlanRepository.getUserIdByEmail(it)
                    if (userId != null) {
                        _currentUserId.value = userId
                        setupUserSpecificData(userId)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "User not found"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error loading user: ${e.message}"
                )
            }
        }
    }

    private fun setupUserSpecificData(userId: Long) {
        _allMealPlans.value = mealPlanRepository.getAllMealPlansForUser(userId).asLiveData()
        _userRecipes.value = recipeRepository.getAllRecipesForUser(userId).asLiveData()
    }

    fun selectDay(day: DayOfWeek) {
        _selectedDay.value = day
    }

    fun addRecipeToDay(recipe: Recipe, dayOfWeek: DayOfWeek, mealType: MealType) {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val mealPlan = MealPlan(
                    userId = userId,
                    recipeId = recipe.id,
                    dayOfWeek = dayOfWeek.value,
                    mealType = mealType.value
                )

                mealPlanRepository.insertMealPlan(mealPlan)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "${recipe.title} added to ${dayOfWeek.displayName} as ${mealType.displayName}!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error adding recipe to meal plan", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error adding recipe: ${e.message}"
                )
            }
        }
    }

    fun removeMealPlan(mealPlanId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                mealPlanRepository.deleteMealPlanById(mealPlanId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Meal removed from plan!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error removing meal plan", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error removing meal: ${e.message}"
                )
            }
        }
    }

    fun clearDayMeals(dayOfWeek: DayOfWeek) {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                mealPlanRepository.deleteMealPlansForDay(userId, dayOfWeek.value)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "All meals cleared for ${dayOfWeek.displayName}!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing day meals", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error clearing meals: ${e.message}"
                )
            }
        }
    }

    fun clearAllMealPlans() {
        val userId = _currentUserId.value
        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                mealPlanRepository.deleteAllMealPlansForUser(userId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "All meal plans cleared!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing all meal plans", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error clearing meal plans: ${e.message}"
                )
            }
        }
    }

    fun moveMealPlan(mealPlanId: Long, newDayOfWeek: DayOfWeek, newMealType: MealType) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val existingMealPlan = mealPlanRepository.getMealPlanById(mealPlanId)
                if (existingMealPlan != null) {
                    val updatedMealPlan = existingMealPlan.copy(
                        dayOfWeek = newDayOfWeek.value,
                        mealType = newMealType.value
                    )
                    mealPlanRepository.updateMealPlan(updatedMealPlan)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Meal moved to ${newDayOfWeek.displayName} - ${newMealType.displayName}!"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error moving meal plan", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error moving meal: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    data class MealPlanUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )
}

