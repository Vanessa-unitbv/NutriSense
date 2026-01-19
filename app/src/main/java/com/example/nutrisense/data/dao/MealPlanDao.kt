package com.example.nutrisense.data.dao

import androidx.room.*
import com.example.nutrisense.data.entity.MealPlan
import kotlinx.coroutines.flow.Flow

data class MealPlanWithRecipeData(
    val id: Long,
    val userId: Long,
    val recipeId: Long,
    val dayOfWeek: Int,
    val mealType: String,
    val createdAt: Long,
    val recipeTitle: String,
    val recipeIngredients: String,
    val recipeServings: String,
    val recipeInstructions: String,
    val recipeIsFavorite: Boolean
)

@Dao
interface MealPlanDao {

    @Query("""
        SELECT mp.id, mp.userId, mp.recipeId, mp.dayOfWeek, mp.mealType, mp.createdAt,
               r.title as recipeTitle, r.ingredients as recipeIngredients, 
               r.servings as recipeServings, r.instructions as recipeInstructions,
               r.isFavorite as recipeIsFavorite
        FROM meal_plans mp
        INNER JOIN user_recipes r ON mp.recipeId = r.id
        WHERE mp.userId = :userId
        ORDER BY mp.dayOfWeek, 
            CASE mp.mealType 
                WHEN 'breakfast' THEN 0 
                WHEN 'lunch' THEN 1 
                WHEN 'dinner' THEN 2 
                WHEN 'snack' THEN 3 
            END
    """)
    fun getAllMealPlansForUser(userId: Long): Flow<List<MealPlanWithRecipeData>>

    @Query("""
        SELECT mp.id, mp.userId, mp.recipeId, mp.dayOfWeek, mp.mealType, mp.createdAt,
               r.title as recipeTitle, r.ingredients as recipeIngredients, 
               r.servings as recipeServings, r.instructions as recipeInstructions,
               r.isFavorite as recipeIsFavorite
        FROM meal_plans mp
        INNER JOIN user_recipes r ON mp.recipeId = r.id
        WHERE mp.userId = :userId AND mp.dayOfWeek = :dayOfWeek
        ORDER BY CASE mp.mealType 
            WHEN 'breakfast' THEN 0 
            WHEN 'lunch' THEN 1 
            WHEN 'dinner' THEN 2 
            WHEN 'snack' THEN 3 
        END
    """)
    fun getMealPlansForDay(userId: Long, dayOfWeek: Int): Flow<List<MealPlanWithRecipeData>>

    @Query("SELECT * FROM meal_plans WHERE id = :id")
    suspend fun getMealPlanById(id: Long): MealPlan?

    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND recipeId = :recipeId AND dayOfWeek = :dayOfWeek AND mealType = :mealType LIMIT 1")
    suspend fun findExistingMealPlan(userId: Long, recipeId: Long, dayOfWeek: Int, mealType: String): MealPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan): Long

    @Update
    suspend fun updateMealPlan(mealPlan: MealPlan)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlan)

    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteMealPlanById(id: Long)

    @Query("DELETE FROM meal_plans WHERE userId = :userId AND dayOfWeek = :dayOfWeek")
    suspend fun deleteMealPlansForDay(userId: Long, dayOfWeek: Int)

    @Query("DELETE FROM meal_plans WHERE userId = :userId")
    suspend fun deleteAllMealPlansForUser(userId: Long)

    @Query("SELECT COUNT(*) FROM meal_plans WHERE userId = :userId")
    suspend fun getMealPlanCountForUser(userId: Long): Int

    @Query("SELECT COUNT(*) FROM meal_plans WHERE userId = :userId AND dayOfWeek = :dayOfWeek")
    suspend fun getMealPlanCountForDay(userId: Long, dayOfWeek: Int): Int
}
