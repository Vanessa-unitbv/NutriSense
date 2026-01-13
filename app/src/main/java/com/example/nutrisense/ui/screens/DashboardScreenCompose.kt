package com.example.nutrisense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class DashboardState(
    val userName: String = "User",
    val dailyCalorieGoal: Int = 2000,
    val dailyCaloriesConsumed: Int = 0,
    val dailyWaterGoal: Int = 2000,
    val dailyWaterConsumed: Int = 0,
    val dailyProtein: Int = 0,
    val dailyCarbs: Int = 0,
    val dailyFat: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreenCompose(
    state: DashboardState,
    onCalculateNutritionClick: () -> Unit,
    onSearchHistoryClick: () -> Unit,
    onRecipeSearchClick: () -> Unit,
    onRecipeHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val calorieProgress = if (state.dailyCalorieGoal > 0)
        state.dailyCaloriesConsumed.toFloat() / state.dailyCalorieGoal.toFloat()
    else 0f
    val waterProgress = if (state.dailyWaterGoal > 0)
        state.dailyWaterConsumed.toFloat() / state.dailyWaterGoal.toFloat()
    else 0f

    NutriSenseTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Welcome, ${state.userName}! 👋",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                        IconButton(onClick = onLogoutClick) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NutriSenseColors.Brown
                    )
                )
            },
            containerColor = NutriSenseColors.Background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Daily Progress Cards
                Text(
                    text = "📊 Today's Progress",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NutriSenseColors.Brown
                )

                // Calories Card
                ProgressCard(
                    title = "🔥 Calories",
                    current = state.dailyCaloriesConsumed,
                    goal = state.dailyCalorieGoal,
                    unit = "kcal",
                    progress = calorieProgress,
                    color = NutriSenseColors.CaloriesColor
                )

                // Water Card
                ProgressCard(
                    title = "💧 Water",
                    current = state.dailyWaterConsumed,
                    goal = state.dailyWaterGoal,
                    unit = "ml",
                    progress = waterProgress,
                    color = NutriSenseColors.WaterColor
                )

                // Macros Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MacroCard(
                        title = "Protein",
                        value = "${state.dailyProtein}g",
                        color = NutriSenseColors.ProteinColor,
                        modifier = Modifier.weight(1f)
                    )
                    MacroCard(
                        title = "Carbs",
                        value = "${state.dailyCarbs}g",
                        color = NutriSenseColors.CarbsColor,
                        modifier = Modifier.weight(1f)
                    )
                    MacroCard(
                        title = "Fat",
                        value = "${state.dailyFat}g",
                        color = NutriSenseColors.FatColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Actions
                Text(
                    text = "⚡ Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NutriSenseColors.Brown
                )

                // Action Buttons Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionButton(
                        title = "Calculate\nNutrition",
                        emoji = "🧮",
                        onClick = onCalculateNutritionClick,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardActionButton(
                        title = "Food\nHistory",
                        emoji = "📜",
                        onClick = onSearchHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionButton(
                        title = "Search\nRecipes",
                        emoji = "🔍",
                        onClick = onRecipeSearchClick,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardActionButton(
                        title = "Recipe\nHistory",
                        emoji = "❤️",
                        onClick = onRecipeHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardActionButton(
                        title = "Settings",
                        emoji = "⚙️",
                        onClick = onSettingsClick,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardActionButton(
                        title = "Profile",
                        emoji = "👤",
                        onClick = onProfileClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProgressCard(
    title: String,
    current: Int,
    goal: Int,
    unit: String,
    progress: Float,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NutriSenseColors.CardOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$current / $goal $unit",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = color,
                trackColor = Color.White.copy(alpha = 0.3f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(progress * 100).toInt().coerceIn(0, 100)}% of daily goal",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MacroCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardActionButton(
    title: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NutriSenseColors.CardOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
