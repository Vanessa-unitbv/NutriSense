package com.example.nutrisense.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrofit pentru CalorieNinjas (nutrition)
    private val nutritionRetrofit = Retrofit.Builder()
        .baseUrl(NutritionApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Retrofit pentru API Ninjas (recipes)
    private val recipeRetrofit = Retrofit.Builder()
        .baseUrl(RecipeApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val nutritionApiService: NutritionApiService = nutritionRetrofit.create(NutritionApiService::class.java)
    val recipeApiService: RecipeApiService = recipeRetrofit.create(RecipeApiService::class.java)
}