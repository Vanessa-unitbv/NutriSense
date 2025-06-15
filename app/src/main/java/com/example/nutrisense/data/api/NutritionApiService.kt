package com.example.nutrisense.data.api

import com.example.nutrisense.data.model.NutritionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NutritionApiService {

    @GET("nutrition")
    suspend fun getNutritionInfo(
        @Query("query") query: String,
        @Header("X-Api-Key") apiKey: String
    ): Response<CalorieNinjasWrapper>

    companion object {
        const val BASE_URL = "https://api.calorieninjas.com/v1/"

        const val API_KEY = "ZeMJNIaz7JLI2rDpSuL3jQ==nScJs7HfYucaDzUY"
    }
}

data class CalorieNinjasWrapper(
    val items: List<NutritionResponse>
)