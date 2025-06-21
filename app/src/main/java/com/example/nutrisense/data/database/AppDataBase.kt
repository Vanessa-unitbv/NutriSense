package com.example.nutrisense.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nutrisense.data.dao.UserDao
import com.example.nutrisense.data.dao.FoodDao
import com.example.nutrisense.data.dao.RecipeDao
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.data.entity.Recipe

@Database(
    entities = [User::class, Food::class, Recipe::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun recipeDao(): RecipeDao

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