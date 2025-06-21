package com.example.nutrisense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.database.AppDatabase
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.data.repository.RecipeRepository
import com.example.nutrisense.data.preferences.SharedPreferencesManager
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecipeRepository
    private val globalPreferencesManager: SharedPreferencesManager

    init {
        val database = AppDatabase.getDatabase(application)
        val recipeDao = database.recipeDao()
        val userDao = database.userDao()
        repository = RecipeRepository(recipeDao, userDao)
        globalPreferencesManager = SharedPreferencesManager.getGlobalInstance(application)
    }

    private val currentUserEmail: String?
        get() = globalPreferencesManager.getUserEmail()

    private val _currentUserId = MutableLiveData<Long?>()
    val currentUserId: LiveData<Long?> = _currentUserId

    private val _userRecipes = MutableLiveData<LiveData<List<Recipe>>>()
    val userRecipes: LiveData<LiveData<List<Recipe>>> = _userRecipes

    private val _userFavoriteRecipes = MutableLiveData<LiveData<List<Recipe>>>()
    val userFavoriteRecipes: LiveData<LiveData<List<Recipe>>> = _userFavoriteRecipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _searchResults = MutableLiveData<List<Recipe>>()
    val searchResults: LiveData<List<Recipe>> = _searchResults

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
        _userRecipes.value = repository.getAllRecipesForUser(userId).asLiveData()
        _userFavoriteRecipes.value = repository.getFavoriteRecipesForUser(userId).asLiveData()
    }

    fun searchRecipes(ingredients: String) {
        if (ingredients.isBlank()) {
            _errorMessage.value = "Please enter ingredients"
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
                val result = repository.searchAndSaveRecipesForUser(
                    userEmail = userEmail,
                    query = ingredients
                )

                if (result.isSuccess) {
                    val recipes = result.getOrNull() ?: emptyList()
                    _searchResults.value = recipes

                    if (recipes.isEmpty()) {
                        _errorMessage.value = "No recipes found for '$ingredients'"
                    } else {
                        _errorMessage.value = null
                        _successMessage.value = "Found ${recipes.size} recipe(s)! All recipes have been saved to your collection."
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

    fun addRecipeToDatabase(recipe: Recipe) {
        viewModelScope.launch {
            try {
                repository.insertRecipe(recipe)
                _successMessage.value = "Recipe saved successfully!"
            } catch (e: Exception) {
                _errorMessage.value = "Error saving recipe: ${e.message}"
            }
        }
    }

    fun updateFavoriteStatus(recipe: Recipe) {
        viewModelScope.launch {
            try {
                repository.updateFavoriteStatus(recipe.id, recipe.isFavorite)
                val status = if (recipe.isFavorite) "added to" else "removed from"
                _successMessage.value = "${recipe.title} $status favorites!"
            } catch (e: Exception) {
                _errorMessage.value = "Error updating favorite: ${e.message}"
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                repository.deleteRecipe(recipe)
                _successMessage.value = "${recipe.title} deleted successfully!"
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting recipe: ${e.message}"
            }
        }
    }

    fun clearAllUserRecipes() {
        viewModelScope.launch {
            try {
                currentUserId.value?.let { userId ->
                    repository.deleteAllRecipesForUser(userId)
                    _successMessage.value = "All recipes cleared successfully!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error clearing recipes: ${e.message}"
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