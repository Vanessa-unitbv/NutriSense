package com.example.nutrisense.data.dao

import androidx.room.*
import com.example.nutrisense.data.entity.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM user_recipes WHERE userId = :userId ORDER BY addedAt DESC")
    fun getAllRecipesForUser(userId: Long): Flow<List<Recipe>>

    @Query("SELECT * FROM user_recipes WHERE userId = :userId AND isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteRecipesForUser(userId: Long): Flow<List<Recipe>>

    @Query("SELECT * FROM user_recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM user_recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)

    @Query("UPDATE user_recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM user_recipes WHERE userId = :userId")
    suspend fun getRecipeCountForUser(userId: Long): Int

    @Query("DELETE FROM user_recipes WHERE userId = :userId")
    suspend fun deleteAllRecipesForUser(userId: Long)
}