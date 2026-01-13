package com.example.nutrisense.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.data.repository.RecipeRepository
import com.example.nutrisense.managers.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val globalPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "RecipeViewModel"
    }

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    private val _userRecipes = MutableStateFlow<LiveData<List<Recipe>>?>(null)
    val userRecipes: StateFlow<LiveData<List<Recipe>>?> = _userRecipes.asStateFlow()

    private val _userFavoriteRecipes = MutableStateFlow<LiveData<List<Recipe>>?>(null)
    val userFavoriteRecipes: StateFlow<LiveData<List<Recipe>>?> = _userFavoriteRecipes.asStateFlow()

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
        _userRecipes.value = repository.getAllRecipesForUser(userId).asLiveData()
        _userFavoriteRecipes.value = repository.getFavoriteRecipesForUser(userId).asLiveData()
    }

    fun searchRecipes(ingredients: String) {
        val validation = validateRecipeInput(ingredients)
        if (validation is ValidationResult.Error) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = validation.message
            )
            return
        }

        val userEmail = globalPreferencesManager.getUserEmail()
        if (userEmail == null) {
            _uiState.value = _uiState.value.copy(
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
                val result = repository.searchAndSaveRecipesForUser(
                    userEmail = userEmail,
                    query = ingredients
                )

                if (result.isSuccess) {
                    val recipes = result.getOrNull() ?: emptyList()

                    _uiState.value = if (recipes.isEmpty()) {
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "No recipes found for '$ingredients'",
                            searchResults = emptyList()
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            searchResults = recipes,
                            successMessage = "Found ${recipes.size} recipe(s)! All recipes have been saved to your collection."
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
                Log.e(TAG, "Network error during recipe search", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }
    }

    fun addRecipeToDatabase(recipe: Recipe) {
        viewModelScope.launch {
            try {
                repository.insertRecipe(recipe)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Recipe saved successfully!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error saving recipe", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error saving recipe: ${e.message}"
                )
            }
        }
    }

    fun updateFavoriteStatus(recipe: Recipe) {
        viewModelScope.launch {
            try {
                repository.updateFavoriteStatus(recipe.id, recipe.isFavorite)
                val status = if (recipe.isFavorite) "added to" else "removed from"
                _uiState.value = _uiState.value.copy(
                    successMessage = "${recipe.title} $status favorites!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating favorite status", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating favorite: ${e.message}"
                )
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                repository.deleteRecipe(recipe)
                _uiState.value = _uiState.value.copy(
                    successMessage = "${recipe.title} deleted successfully!"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting recipe", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deleting recipe: ${e.message}"
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

    private fun validateRecipeInput(ingredients: String): ValidationResult {
        return when {
            ingredients.isBlank() -> ValidationResult.Error("Ingredients cannot be empty")
            else -> ValidationResult.Success
        }
    }

    data class RecipeUiState(
        val isLoading: Boolean = false,
        val searchResults: List<Recipe> = emptyList(),
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}