package com.example.nutrisense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String = "User",
    dailyCalorieGoal: Int = 2000,
    dailyCaloriesConsumed: Int = 1450,
    waterGoal: Int = 2000,
    waterConsumed: Int = 1200,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onCalculateNutritionClick: () -> Unit,
    onSearchRecipesClick: () -> Unit
) {
    val calorieProgress = dailyCaloriesConsumed.toFloat() / dailyCalorieGoal.toFloat()
    val waterProgress = waterConsumed.toFloat() / waterGoal.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "Welcome, $userName! 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                }
                IconButton(onClick = onLogoutClick) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6200EE)
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calorie Card
            item {
                NutritionCard(
                    title = "Daily Calories",
                    current = dailyCaloriesConsumed,
                    goal = dailyCalorieGoal,
                    unit = "kcal",
                    progress = calorieProgress,
                    icon = "🔥",
                    color = Color(0xFFFF6B6B)
                )
            }

            // Water Card
            item {
                NutritionCard(
                    title = "Daily Water Intake",
                    current = waterConsumed,
                    goal = waterGoal,
                    unit = "ml",
                    progress = waterProgress,
                    icon = "💧",
                    color = Color(0xFF4ECDC4)
                )
            }

            // Action Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "📊 Calculate\nNutrition",
                        modifier = Modifier.weight(1f),
                        onClick = onCalculateNutritionClick
                    )
                    ActionCard(
                        title = "🍳 Search\nRecipes",
                        modifier = Modifier.weight(1f),
                        onClick = onSearchRecipesClick
                    )
                }
            }

            // Tips Section
            item {
                TipsCard()
            }
        }
    }
}

@Composable
fun NutritionCard(
    title: String,
    current: Int,
    goal: Int,
    unit: String,
    progress: Float,
    icon: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = icon,
                    fontSize = 28.sp
                )
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(bottom = 12.dp),
                color = color,
                trackColor = Color.LightGray,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$current / $goal $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun TipsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8DC))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💡 Nutrition Tips",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "• Drink water before meals to help digestion\n" +
                        "• Include proteins in every meal\n" +
                        "• Eat colorful vegetables for variety\n" +
                        "• Keep a balanced diet throughout the day",
                fontSize = 12.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )
        }
    }
}
