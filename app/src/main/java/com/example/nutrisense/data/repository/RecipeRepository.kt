package com.example.nutrisense.data.repository

import com.example.nutrisense.data.api.ApiClient
import com.example.nutrisense.data.api.RecipeApiService
import com.example.nutrisense.data.dao.RecipeDao
import com.example.nutrisense.data.dao.UserDao
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.data.entity.toUserRecipe
import com.example.nutrisense.data.model.RecipeResponse
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val userDao: UserDao,
    private val apiService: RecipeApiService = ApiClient.recipeApiService
) {

    fun getAllRecipesForUser(userId: Long): Flow<List<Recipe>> =
        recipeDao.getAllRecipesForUser(userId)

    fun getFavoriteRecipesForUser(userId: Long): Flow<List<Recipe>> =
        recipeDao.getFavoriteRecipesForUser(userId)

    suspend fun getRecipeById(id: Long): Recipe? = recipeDao.getRecipeById(id)

    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)

    suspend fun insertRecipes(recipes: List<Recipe>) = recipeDao.insertRecipes(recipes)

    suspend fun updateRecipe(recipe: Recipe) = recipeDao.updateRecipe(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    suspend fun deleteRecipeById(id: Long) = recipeDao.deleteRecipeById(id)

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) =
        recipeDao.updateFavoriteStatus(id, isFavorite)

    suspend fun getRecipeCountForUser(userId: Long): Int =
        recipeDao.getRecipeCountForUser(userId)

    suspend fun deleteAllRecipesForUser(userId: Long) =
        recipeDao.deleteAllRecipesForUser(userId)

    suspend fun getUserIdByEmail(email: String): Long? {
        return userDao.getUserByEmail(email)?.id
    }

    suspend fun searchRecipes(query: String): Result<List<RecipeResponse>> {
        return try {
            val response = apiService.getRecipes(query, RecipeApiService.API_KEY)

            if (response.isSuccessful) {
                val recipes = response.body() ?: emptyList()
                Result.success(recipes)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request - Check your ingredient format"
                    401 -> "Unauthorized - Invalid API key for API Ninjas"
                    403 -> "Forbidden - API key limits exceeded"
                    429 -> "Too Many Requests - Try again later"
                    else -> "API Error: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchAndSaveRecipesForUser(
        userEmail: String,
        query: String
    ): Result<List<Recipe>> {
        return try {
            val userId = getUserIdByEmail(userEmail)
                ?: return Result.failure(Exception("User not found"))

            val recipeResult = searchRecipes(query)

            if (recipeResult.isSuccess) {
                val recipeResponses = recipeResult.getOrNull() ?: emptyList()
                val recipes = recipeResponses.map { response ->
                    response.toUserRecipe(
                        userId = userId,
                        searchQuery = query
                    )
                }

                insertRecipes(recipes)
                Result.success(recipes)
            } else {
                Result.failure(recipeResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}