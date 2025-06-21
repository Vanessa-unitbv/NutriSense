package com.example.nutrisense.data.api

import com.example.nutrisense.data.model.RecipeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface RecipeApiService {

    @GET("recipe")
    suspend fun getRecipes(
        @Query("query") query: String,
        @Header("X-Api-Key") apiKey: String
    ): Response<List<RecipeResponse>>

    companion object {
        const val BASE_URL = "https://api.api-ninjas.com/v1/"
        const val API_KEY = "P7PaMWKnD3FqEmU5N6q+iA==MvB2cPjgfUqgZPqb"
    }
}