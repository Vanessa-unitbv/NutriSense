package com.example.nutrisense.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutrisense.ui.screens.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriSenseComposeApp()
        }
    }
}

@Composable
fun NutriSenseComposeApp() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NutritionSenseNavigation(navController)
    }
}

@Composable
fun NutritionSenseNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    // Handle login logic
                    navController.navigate("dashboard")
                },
                onRegisterClick = { email, password ->
                    // Handle register navigation
                    navController.navigate("dashboard")
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                userName = "John",
                dailyCalorieGoal = 2000,
                dailyCaloriesConsumed = 1450,
                waterGoal = 2000,
                waterConsumed = 1200,
                onSettingsClick = {
                    navController.navigate("settings")
                },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onCalculateNutritionClick = {
                    navController.navigate("calculate_nutrition")
                },
                onSearchRecipesClick = {
                    navController.navigate("search_recipes")
                }
            )
        }

        composable("calculate_nutrition") {
            CalculateNutritionScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSearchClick = { foodName, quantity ->
                    // Handle nutrition search
                },
                nutritionData = NutritionData(
                    calories = 250.0,
                    protein = 15.5,
                    carbs = 35.2,
                    fat = 8.3,
                    fiber = 2.1,
                    sugar = 5.0
                )
            )
        }

        composable("search_recipes") {
            RecipeSearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSearchClick = { ingredients ->
                    // Handle recipe search
                },
                recipes = listOf(
                    RecipeItem(
                        id = 1,
                        title = "Grilled Chicken Salad",
                        ingredients = "Chicken breast, lettuce, tomato, cucumber, olive oil, lemon juice",
                        instructions = "1. Grill chicken\n2. Chop vegetables\n3. Mix and serve",
                        servings = 2
                    ),
                    RecipeItem(
                        id = 2,
                        title = "Vegetable Stir Fry",
                        ingredients = "Broccoli, bell pepper, carrot, soy sauce, garlic",
                        instructions = "1. Cut vegetables\n2. Heat wok\n3. Stir fry ingredients",
                        servings = 3
                    )
                ),
                onRecipeClick = { recipe ->
                    // Handle recipe click
                },
                onFavoriteClick = { recipe ->
                    // Handle favorite toggle
                },
                onDeleteClick = { recipe ->
                    // Handle delete
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveClick = { settings ->
                    // Handle save settings
                }
            )
        }
    }
}

