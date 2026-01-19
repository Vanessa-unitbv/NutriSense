package com.example.nutrisense.data.repository

import com.example.nutrisense.data.dao.MealPlanDao
import com.example.nutrisense.data.dao.MealPlanWithRecipeData
import com.example.nutrisense.data.dao.UserDao
import com.example.nutrisense.data.entity.MealPlan
import kotlinx.coroutines.flow.Flow

class MealPlanRepository(
    private val mealPlanDao: MealPlanDao,
    private val userDao: UserDao
) {

    fun getAllMealPlansForUser(userId: Long): Flow<List<MealPlanWithRecipeData>> =
        mealPlanDao.getAllMealPlansForUser(userId)

    fun getMealPlansForDay(userId: Long, dayOfWeek: Int): Flow<List<MealPlanWithRecipeData>> =
        mealPlanDao.getMealPlansForDay(userId, dayOfWeek)

    suspend fun getMealPlanById(id: Long): MealPlan? =
        mealPlanDao.getMealPlanById(id)

    suspend fun insertMealPlan(mealPlan: MealPlan): Long {
        val existing = mealPlanDao.findExistingMealPlan(
            mealPlan.userId,
            mealPlan.recipeId,
            mealPlan.dayOfWeek,
            mealPlan.mealType
        )
        return if (existing != null) {
            existing.id
        } else {
            mealPlanDao.insertMealPlan(mealPlan)
        }
    }

    suspend fun updateMealPlan(mealPlan: MealPlan) =
        mealPlanDao.updateMealPlan(mealPlan)

    suspend fun deleteMealPlan(mealPlan: MealPlan) =
        mealPlanDao.deleteMealPlan(mealPlan)

    suspend fun deleteMealPlanById(id: Long) =
        mealPlanDao.deleteMealPlanById(id)

    suspend fun deleteMealPlansForDay(userId: Long, dayOfWeek: Int) =
        mealPlanDao.deleteMealPlansForDay(userId, dayOfWeek)

    suspend fun deleteAllMealPlansForUser(userId: Long) =
        mealPlanDao.deleteAllMealPlansForUser(userId)

    suspend fun getMealPlanCountForUser(userId: Long): Int =
        mealPlanDao.getMealPlanCountForUser(userId)

    suspend fun getMealPlanCountForDay(userId: Long, dayOfWeek: Int): Int =
        mealPlanDao.getMealPlanCountForDay(userId, dayOfWeek)

    suspend fun getUserIdByEmail(email: String): Long? {
        return userDao.getUserByEmail(email)?.id
    }
}

