package com.example.nutrisense.di

import com.example.nutrisense.data.api.NutritionApiService
import com.example.nutrisense.data.api.RecipeApiService
import com.example.nutrisense.data.dao.FoodDao
import com.example.nutrisense.data.dao.MealPlanDao
import com.example.nutrisense.data.dao.RecipeDao
import com.example.nutrisense.data.dao.UserDao
import com.example.nutrisense.data.repository.FoodRepository
import com.example.nutrisense.data.repository.MealPlanRepository
import com.example.nutrisense.data.repository.RecipeRepository
import com.example.nutrisense.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideFoodRepository(
        foodDao: FoodDao,
        userDao: UserDao,
        nutritionApiService: NutritionApiService
    ): FoodRepository {
        return FoodRepository(foodDao, userDao, nutritionApiService)
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipeDao: RecipeDao,
        userDao: UserDao,
        recipeApiService: RecipeApiService
    ): RecipeRepository {
        return RecipeRepository(recipeDao, userDao, recipeApiService)
    }

    @Provides
    @Singleton
    fun provideMealPlanRepository(
        mealPlanDao: MealPlanDao,
        userDao: UserDao
    ): MealPlanRepository {
        return MealPlanRepository(mealPlanDao, userDao)
    }
}