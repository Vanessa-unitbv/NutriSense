package com.example.nutrisense.data.dao

import androidx.room.*
import com.example.nutrisense.data.entity.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM user_foods WHERE userId = :userId ORDER BY addedAt DESC")
    fun getAllFoodsForUser(userId: Long): Flow<List<Food>>

    @Query("SELECT * FROM user_foods WHERE userId = :userId AND isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteFoodsForUser(userId: Long): Flow<List<Food>>

    @Query("SELECT * FROM user_foods WHERE userId = :userId AND (name LIKE '%' || :query || '%' OR originalQuery LIKE '%' || :query || '%') ORDER BY addedAt DESC")
    fun searchFoodsForUser(userId: Long, query: String): Flow<List<Food>>

    @Query("SELECT * FROM user_foods WHERE userId = :userId AND DATE(addedAt/1000, 'unixepoch') = DATE('now') ORDER BY addedAt DESC")
    fun getTodayFoodsForUser(userId: Long): Flow<List<Food>>

    @Query("SELECT * FROM user_foods WHERE userId = :userId AND consumedAt IS NOT NULL AND DATE(consumedAt/1000, 'unixepoch') = DATE('now') ORDER BY consumedAt DESC")
    fun getTodayConsumedFoodsForUser(userId: Long): Flow<List<Food>>

    @Query("SELECT * FROM user_foods WHERE id = :id")
    suspend fun getFoodById(id: Long): Food?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<Food>)

    @Update
    suspend fun updateFood(food: Food)

    @Delete
    suspend fun deleteFood(food: Food)

    @Query("DELETE FROM user_foods WHERE id = :id")
    suspend fun deleteFoodById(id: Long)

    @Query("UPDATE user_foods SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE user_foods SET consumedAt = :consumedAt WHERE id = :id")
    suspend fun markAsConsumed(id: Long, consumedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM user_foods WHERE userId = :userId")
    suspend fun getFoodCountForUser(userId: Long): Int

    @Query("DELETE FROM user_foods WHERE userId = :userId")
    suspend fun deleteAllFoodsForUser(userId: Long)

    @Query("SELECT SUM(calories) FROM user_foods WHERE userId = :userId AND consumedAt IS NOT NULL AND DATE(consumedAt/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayTotalCaloriesForUser(userId: Long): Double?

    @Query("SELECT SUM(proteinG) FROM user_foods WHERE userId = :userId AND consumedAt IS NOT NULL AND DATE(consumedAt/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayTotalProteinForUser(userId: Long): Double?

    @Query("SELECT SUM(carbohydratesTotalG) FROM user_foods WHERE userId = :userId AND consumedAt IS NOT NULL AND DATE(consumedAt/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayTotalCarbsForUser(userId: Long): Double?

    @Query("SELECT SUM(fatTotalG) FROM user_foods WHERE userId = :userId AND consumedAt IS NOT NULL AND DATE(consumedAt/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayTotalFatForUser(userId: Long): Double?

    @Query("SELECT DISTINCT originalQuery FROM user_foods WHERE userId = :userId ORDER BY addedAt DESC LIMIT 10")
    suspend fun getRecentSearchesForUser(userId: Long): List<String>

    @Query("SELECT *, COUNT(*) as consumeCount FROM user_foods WHERE userId = :userId AND consumedAt IS NOT NULL GROUP BY originalQuery ORDER BY consumeCount DESC LIMIT 10")
    suspend fun getMostConsumedFoodsForUser(userId: Long): List<Food>
}