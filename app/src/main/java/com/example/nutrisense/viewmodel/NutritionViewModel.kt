package com.example.nutrisense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.database.AppDatabase
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.data.repository.FoodRepository
import com.example.nutrisense.data.repository.NutritionSummary
import com.example.nutrisense.data.preferences.SharedPreferencesManager
import kotlinx.coroutines.launch

class NutritionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    private val globalPreferencesManager: SharedPreferencesManager

    init {
        val database = AppDatabase.getDatabase(application)
        val foodDao = database.foodDao()
        val userDao = database.userDao()
        repository = FoodRepository(foodDao, userDao)
        globalPreferencesManager = SharedPreferencesManager.getGlobalInstance(application)
    }

    private val currentUserEmail: String?
        get() = globalPreferencesManager.getUserEmail()

    private val _currentUserId = MutableLiveData<Long?>()
    val currentUserId: LiveData<Long?> = _currentUserId

    private val _userFoods = MutableLiveData<LiveData<List<Food>>>()
    val userFoods: LiveData<LiveData<List<Food>>> = _userFoods

    private val _userFavoriteFoods = MutableLiveData<LiveData<List<Food>>>()
    val userFavoriteFoods: LiveData<LiveData<List<Food>>> = _userFavoriteFoods

    private val _userTodayFoods = MutableLiveData<LiveData<List<Food>>>()
    val userTodayFoods: LiveData<LiveData<List<Food>>> = _userTodayFoods

    private val _userTodayConsumedFoods = MutableLiveData<LiveData<List<Food>>>()
    val userTodayConsumedFoods: LiveData<LiveData<List<Food>>> = _userTodayConsumedFoods

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _searchResults = MutableLiveData<List<Food>>()
    val searchResults: LiveData<List<Food>> = _searchResults

    private val _nutritionSummary = MutableLiveData<NutritionSummary>()
    val nutritionSummary: LiveData<NutritionSummary> = _nutritionSummary

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            currentUserEmail?.let { email ->
                try {
                    val userId = repository.getUserIdByEmail(email)
                    if (userId != null) {
                        _currentUserId.value = userId
                        setupUserSpecificData(userId)
                        loadNutritionSummary(userId)

                        SharedPreferencesManager.setCurrentUser(email)
                    } else {
                        _errorMessage.value = "User not found"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error loading user: ${e.message}"
                }
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
        if (foodName.isBlank()) {
            _errorMessage.value = "Please enter a food name"
            return
        }

        if (quantity <= 0) {
            _errorMessage.value = "Please enter a valid quantity"
            return
        }

        val userEmail = currentUserEmail
        if (userEmail == null) {
            _errorMessage.value = "User not logged in"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.searchAndSaveNutritionInfoForUser(
                    userEmail = userEmail,
                    query = foodName,
                    requestedQuantity = quantity
                )

                if (result.isSuccess) {
                    val foods = result.getOrNull() ?: emptyList()
                    _searchResults.value = foods

                    if (foods.isEmpty()) {
                        _errorMessage.value = "No nutrition information found for '$foodName'"
                    } else {
                        _errorMessage.value = null
                        _successMessage.value = "Found nutrition info for ${foods.size} food(s)!"

                        currentUserId.value?.let { userId ->
                            loadNutritionSummary(userId)
                        }
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    _errorMessage.value = "Error: ${exception?.message ?: "Unknown error occurred"}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFoodToDatabase(food: Food) {
        viewModelScope.launch {
            try {
                repository.insertFood(food)
                currentUserId.value?.let { userId ->
                    loadNutritionSummary(userId)
                }
                _successMessage.value = "Food saved successfully!"
            } catch (e: Exception) {
                _errorMessage.value = "Error saving food: ${e.message}"
            }
        }
    }

    fun markFoodAsConsumed(food: Food) {
        viewModelScope.launch {
            try {
                repository.markAsConsumed(food.id)
                currentUserId.value?.let { userId ->
                    loadNutritionSummary(userId)
                }
                _successMessage.value = "${food.name} marked as consumed!"
            } catch (e: Exception) {
                _errorMessage.value = "Error marking food as consumed: ${e.message}"
            }
        }
    }

    fun updateFavoriteStatus(food: Food) {
        viewModelScope.launch {
            try {
                repository.updateFavoriteStatus(food.id, food.isFavorite)
                val status = if (food.isFavorite) "added to" else "removed from"
                _successMessage.value = "${food.name} $status favorites!"
            } catch (e: Exception) {
                _errorMessage.value = "Error updating favorite: ${e.message}"
            }
        }
    }

    fun deleteFood(food: Food) {
        viewModelScope.launch {
            try {
                repository.deleteFood(food)
                currentUserId.value?.let { userId ->
                    loadNutritionSummary(userId)
                }
                _successMessage.value = "${food.name} deleted successfully!"
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting food: ${e.message}"
            }
        }
    }

    fun clearAllUserFoods() {
        viewModelScope.launch {
            try {
                currentUserId.value?.let { userId ->
                    repository.deleteAllFoodsForUser(userId)
                    loadNutritionSummary(userId)
                    _successMessage.value = "All foods cleared successfully!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error clearing foods: ${e.message}"
            }
        }
    }

    private fun loadNutritionSummary(userId: Long) {
        viewModelScope.launch {
            try {
                val summary = repository.getTodayNutritionSummaryForUser(userId)
                _nutritionSummary.value = summary
            } catch (e: Exception) {
                _errorMessage.value = "Error loading nutrition summary: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun refreshUserData() {
        loadCurrentUser()
    }
}