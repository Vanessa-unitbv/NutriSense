package com.example.nutrisense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class FoodItem(
    val id: Long = 0,
    val name: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val quantity: Double = 100.0,
    val isFavorite: Boolean = false,
    val isConsumed: Boolean = false,
    val dateAdded: String = ""
)

data class SearchHistoryState(
    val foods: List<FoodItem> = emptyList(),
    val todayCalories: Int = 0,
    val todayProtein: Int = 0,
    val todayCarbs: Int = 0,
    val todayFat: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHistoryScreenCompose(
    state: SearchHistoryState,
    onFoodClick: (FoodItem) -> Unit,
    onFavoriteClick: (FoodItem) -> Unit,
    onDeleteClick: (FoodItem) -> Unit,
    onConsumeClick: (FoodItem) -> Unit,
    onBackClick: () -> Unit
) {
    NutriSenseTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "📜 Food History",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        Text(
                            text = "📊 Today's Nutrition",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NutritionSummaryItem(
                                emoji = "🔥",
                                value = "${state.todayCalories}",
                                label = "kcal"
                            )
                            NutritionSummaryItem(
                                emoji = "🥩",
                                value = "${state.todayProtein}g",
                                label = "Protein"
                            )
                            NutritionSummaryItem(
                                emoji = "🍞",
                                value = "${state.todayCarbs}g",
                                label = "Carbs"
                            )
                            NutritionSummaryItem(
                                emoji = "🧈",
                                value = "${state.todayFat}g",
                                label = "Fat"
                            )
                        }
                    }
                }

                state.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Error.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "❌ $error",
                            modifier = Modifier.padding(12.dp),
                            color = NutriSenseColors.Error,
                            fontSize = 14.sp
                        )
                    }
                }

                state.successMessage?.let { success ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Success.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "✅ $success",
                            modifier = Modifier.padding(12.dp),
                            color = NutriSenseColors.Success,
                            fontSize = 14.sp
                        )
                    }
                }

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NutriSenseColors.Brown)
                    }
                } else if (state.foods.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🍽️",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No food history yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = NutriSenseColors.Brown
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Calculate nutrition for foods to see them here!",
                                fontSize = 14.sp,
                                color = NutriSenseColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.foods) { food ->
                            FoodHistoryCard(
                                food = food,
                                onClick = { onFoodClick(food) },
                                onFavoriteClick = { onFavoriteClick(food) },
                                onDeleteClick = { onDeleteClick(food) },
                                onConsumeClick = { onConsumeClick(food) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionSummaryItem(
    emoji: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodHistoryCard(
    food: FoodItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onConsumeClick: () -> Unit
) {
    var isFavorite by remember(food) { mutableStateOf(food.isFavorite) }
    LaunchedEffect(key1 = food.isFavorite) {
        isFavorite = food.isFavorite
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (food.isConsumed)
                NutriSenseColors.Success.copy(alpha = 0.3f)
            else
                NutriSenseColors.CardOrange
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (food.isConsumed) "✅" else "🍎",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = food.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${food.quantity.toInt()}g",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Text(
                    text = "🔥 ${food.calories.toInt()} kcal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroChip(label = "P", value = "${food.protein.toInt()}g", color = NutriSenseColors.ProteinColor)
                MacroChip(label = "C", value = "${food.carbs.toInt()}g", color = NutriSenseColors.CarbsColor)
                MacroChip(label = "F", value = "${food.fat.toInt()}g", color = NutriSenseColors.FatColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = {
                    isFavorite = !isFavorite
                    onFavoriteClick()
                }) {
                    Text(
                        text = if (isFavorite) "❤️ Favorite" else "🤍 Favorite",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                if (!food.isConsumed) {
                    TextButton(onClick = onConsumeClick) {
                        Text(
                            text = "✅ Consume",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
                TextButton(onClick = onDeleteClick) {
                    Text(
                        text = "🗑️ Delete",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroChip(
    label: String,
    value: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}
