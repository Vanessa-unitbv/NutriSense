package com.example.nutrisense.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nutrisense.data.dao.UserDao
import com.example.nutrisense.data.dao.FoodDao
import com.example.nutrisense.data.dao.RecipeDao
import com.example.nutrisense.data.dao.MealPlanDao
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.data.entity.MealPlan

@Database(
    entities = [User::class, Food::class, Recipe::class, MealPlan::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutrisense_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}