package com.example.nutrisense.data.repository

import com.example.nutrisense.data.api.ApiClient
import com.example.nutrisense.data.api.NutritionApiService
import com.example.nutrisense.data.dao.FoodDao
import com.example.nutrisense.data.dao.UserDao
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.data.entity.toUserFood
import com.example.nutrisense.data.model.NutritionResponse
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val foodDao: FoodDao,
    private val userDao: UserDao,
    private val apiService: NutritionApiService = ApiClient.nutritionApiService
) {

    fun getAllFoodsForUser(userId: Long): Flow<List<Food>> =
        foodDao.getAllFoodsForUser(userId)

    fun getFavoriteFoodsForUser(userId: Long): Flow<List<Food>> =
        foodDao.getFavoriteFoodsForUser(userId)

    fun getTodayFoodsForUser(userId: Long): Flow<List<Food>> =
        foodDao.getTodayFoodsForUser(userId)

    fun getTodayConsumedFoodsForUser(userId: Long): Flow<List<Food>> =
        foodDao.getTodayConsumedFoodsForUser(userId)

    suspend fun getFoodById(id: Long): Food? = foodDao.getFoodById(id)

    suspend fun insertFood(food: Food): Long = foodDao.insertFood(food)

    suspend fun insertFoods(foods: List<Food>) = foodDao.insertFoods(foods)

    suspend fun updateFood(food: Food) = foodDao.updateFood(food)

    suspend fun deleteFood(food: Food) = foodDao.deleteFood(food)

    suspend fun deleteFoodById(id: Long) = foodDao.deleteFoodById(id)

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) =
        foodDao.updateFavoriteStatus(id, isFavorite)

    suspend fun markAsConsumed(id: Long) =
        foodDao.markAsConsumed(id, System.currentTimeMillis())

    suspend fun getFoodCountForUser(userId: Long): Int =
        foodDao.getFoodCountForUser(userId)

    suspend fun deleteAllFoodsForUser(userId: Long) =
        foodDao.deleteAllFoodsForUser(userId)

    suspend fun getTodayTotalCaloriesForUser(userId: Long): Double =
        foodDao.getTodayTotalCaloriesForUser(userId) ?: 0.0

    suspend fun getTodayTotalProteinForUser(userId: Long): Double =
        foodDao.getTodayTotalProteinForUser(userId) ?: 0.0

    suspend fun getTodayTotalCarbsForUser(userId: Long): Double =
        foodDao.getTodayTotalCarbsForUser(userId) ?: 0.0

    suspend fun getTodayTotalFatForUser(userId: Long): Double =
        foodDao.getTodayTotalFatForUser(userId) ?: 0.0

    suspend fun getUserIdByEmail(email: String): Long? {
        return userDao.getUserByEmail(email)?.id
    }

    suspend fun searchNutritionInfo(query: String): Result<List<NutritionResponse>> {
        return try {
            val response = apiService.getNutritionInfo(query, NutritionApiService.API_KEY)

            if (response.isSuccessful) {
                val body = response.body()
                val nutritionList = body?.items ?: emptyList()
                Result.success(nutritionList)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request - Check your food name format"
                    401 -> "Unauthorized - Invalid API key for CalorieNinjas"
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

    suspend fun searchAndSaveNutritionInfoForUser(
        userEmail: String,
        query: String,
        requestedQuantity: Double
    ): Result<List<Food>> {
        return try {
            val userId = getUserIdByEmail(userEmail)
                ?: return Result.failure(Exception("User not found"))

            val apiQuery = "${requestedQuantity.toInt()}g $query"

            val nutritionResult = searchNutritionInfo(apiQuery)

            if (nutritionResult.isSuccess) {
                val nutritionResponses = nutritionResult.getOrNull() ?: emptyList()
                val foods = nutritionResponses.map { response ->
                    response.toUserFood(
                        userId = userId,
                        originalQuery = query,
                        requestedQuantity = requestedQuantity
                    )
                }

                insertFoods(foods)
                Result.success(foods)
            } else {
                Result.failure(nutritionResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodayNutritionSummaryForUser(userId: Long): NutritionSummary {
        val todayConsumedFoods = foodDao.getTodayConsumedFoodsForUser(userId)
        var foodCount = 0

        todayConsumedFoods.collect { foods ->
            foodCount = foods.size
        }

        return NutritionSummary(
            totalCalories = getTodayTotalCaloriesForUser(userId),
            totalProtein = getTodayTotalProteinForUser(userId),
            totalCarbs = getTodayTotalCarbsForUser(userId),
            totalFat = getTodayTotalFatForUser(userId),
            foodCount = foodCount
        )
    }
}

data class NutritionSummary(
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val foodCount: Int
)