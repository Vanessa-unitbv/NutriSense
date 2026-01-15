package com.example.nutrisense.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.data.repository.FoodRepository
import com.example.nutrisense.data.repository.NutritionSummary
import com.example.nutrisense.managers.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repository: FoodRepository,
    private val globalPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "NutritionViewModel"
    }

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _userFoods = MutableStateFlow<LiveData<List<Food>>?>(null)
    val userFoods: StateFlow<LiveData<List<Food>>?> = _userFoods.asStateFlow()

    private val _userFavoriteFoods = MutableStateFlow<LiveData<List<Food>>?>(null)
    val userFavoriteFoods: StateFlow<LiveData<List<Food>>?> = _userFavoriteFoods.asStateFlow()

    private val _userTodayFoods = MutableStateFlow<LiveData<List<Food>>?>(null)
    val userTodayFoods: StateFlow<LiveData<List<Food>>?> = _userTodayFoods.asStateFlow()

    private val _userTodayConsumedFoods = MutableStateFlow<LiveData<List<Food>>?>(null)
    val userTodayConsumedFoods: StateFlow<LiveData<List<Food>>?> = _userTodayConsumedFoods.asStateFlow()

    private val _nutritionSummary = MutableStateFlow<NutritionSummary?>(null)
    val nutritionSummary: StateFlow<NutritionSummary?> = _nutritionSummary.asStateFlow()

    init {
        loadCurrentUserAsync()
    }

    private fun loadCurrentUserAsync() {
        viewModelScope.launch {
            try {
                val email = globalPreferencesManager.getUserEmail()
                email?.let {
                    val userId = repository.getUserIdByEmail(it)
                    if (userId != null) {
                        _currentUserId.value = userId
                        setupUserSpecificData(userId)
                        loadNutritionSummaryAsync(userId)
                        SharedPreferencesManager.setCurrentUser(it)
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
        _userFoods.value = repository.getAllFoodsForUser(userId).asLiveData()
        _userFavoriteFoods.value = repository.getFavoriteFoodsForUser(userId).asLiveData()
        _userTodayFoods.value = repository.getTodayFoodsForUser(userId).asLiveData()
        _userTodayConsumedFoods.value = repository.getTodayConsumedFoodsForUser(userId).asLiveData()
    }

    fun searchFoodNutrition(foodName: String, quantity: Double) {
        val validation = validateFoodInput(foodName, quantity)
        if (validation is ValidationResult.Error) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = validation.message,
                searchResults = emptyList()
            )
            return
        }

        val userEmail = globalPreferencesManager.getUserEmail()
        if (userEmail == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "User not logged in"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val result = repository.searchAndSaveNutritionInfoForUser(
                    userEmail = userEmail,
                    query = foodName,
                    requestedQuantity = quantity
                )

                if (result.isSuccess) {
                    val foods = result.getOrNull() ?: emptyList()

                    _uiState.value = if (foods.isEmpty()) {
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "No nutrition information found for '$foodName'",
                            searchResults = emptyList()
                        )
                    } else {
                        _currentUserId.value?.let { loadNutritionSummaryAsync(it) }

                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            searchResults = foods,
                            successMessage = "Found nutrition info for ${foods.size} food(s)!"
                        )
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception?.message ?: "Unknown error occurred"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error during food search", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }
    }

    fun addFoodToDatabase(food: Food) {
        viewModelScope.launch {
            try {
                repository.insertFood(food)
                _currentUserId.value?.let { loadNutritionSummaryAsync(it) }
                _uiState.value = _uiState.value.copy(
                    successMessage = "Food saved successfully!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error saving food", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error saving food: ${e.message}"
                )
            }
        }
    }

    fun markFoodAsConsumed(food: Food) {
        viewModelScope.launch {
            try {
                repository.markAsConsumed(food.id)
                _currentUserId.value?.let { loadNutritionSummaryAsync(it) }
                _uiState.value = _uiState.value.copy(
                    successMessage = "${food.name} marked as consumed!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error marking food as consumed", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error marking food as consumed: ${e.message}"
                )
            }
        }
    }

    fun updateFavoriteStatus(food: Food) {
        viewModelScope.launch {
            try {
                val newFavoriteStatus = !food.isFavorite
                repository.updateFavoriteStatus(food.id, newFavoriteStatus)
                val status = if (newFavoriteStatus) "added to" else "removed from"
                _uiState.value = _uiState.value.copy(
                    successMessage = "${food.name} $status favorites!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating favorite status", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating favorite: ${e.message}"
                )
            }
        }
    }

    fun deleteFood(food: Food) {
        viewModelScope.launch {
            try {
                repository.deleteFood(food)
                _currentUserId.value?.let { loadNutritionSummaryAsync(it) }
                _uiState.value = _uiState.value.copy(
                    successMessage = "${food.name} deleted successfully!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting food", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deleting food: ${e.message}"
                )
            }
        }
    }

    private fun loadNutritionSummaryAsync(userId: Long) {
        viewModelScope.launch {
            try {
                val caloriesAsync = async { repository.getTodayTotalCaloriesForUser(userId) }
                val proteinAsync = async { repository.getTodayTotalProteinForUser(userId) }
                val carbsAsync = async { repository.getTodayTotalCarbsForUser(userId) }
                val fatAsync = async { repository.getTodayTotalFatForUser(userId) }
                val foodCountAsync = async {
                    repository.getTodayConsumedFoodsForUser(userId).first().size
                }

                val summary = NutritionSummary(
                    totalCalories = caloriesAsync.await(),
                    totalProtein = proteinAsync.await(),
                    totalCarbs = carbsAsync.await(),
                    totalFat = fatAsync.await(),
                    foodCount = foodCountAsync.await()
                )

                _nutritionSummary.value = summary
            } catch (e: Exception) {
                Log.e(TAG, "Error loading nutrition summary", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error loading nutrition summary: ${e.message}"
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

    private fun validateFoodInput(foodName: String, quantity: Double): ValidationResult {
        return when {
            foodName.isBlank() -> ValidationResult.Error("Food name cannot be empty")
            quantity <= 0 -> ValidationResult.Error("Quantity must be greater than 0")
            quantity > 10000 -> ValidationResult.Error("Quantity too large (max 10kg)")
            else -> ValidationResult.Success
        }
    }


    data class NutritionUiState(
        val isLoading: Boolean = false,
        val searchResults: List<Food> = emptyList(),
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}