package com.example.nutrisense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculateNutritionScreen(
    onBackClick: () -> Unit,
    onSearchClick: (foodName: String, quantity: Double) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    nutritionData: NutritionData? = null
) {
    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var foodNameError by remember { mutableStateOf("") }
    var quantityError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        TopAppBar(
            title = { Text("Calculate Nutrition", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Search Food",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = foodName,
                            onValueChange = {
                                foodName = it
                                foodNameError = ""
                            },
                            label = { Text("Food Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            singleLine = true,
                            isError = foodNameError.isNotEmpty(),
                            supportingText = {
                                if (foodNameError.isNotEmpty()) {
                                    Text(foodNameError, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        OutlinedTextField(
                            value = quantity,
                            onValueChange = {
                                quantity = it
                                quantityError = ""
                            },
                            label = { Text("Quantity (grams)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = quantityError.isNotEmpty(),
                            supportingText = {
                                if (quantityError.isNotEmpty()) {
                                    Text(quantityError, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        Button(
                            onClick = {
                                var isValid = true
                                if (foodName.isEmpty()) {
                                    foodNameError = "Food name cannot be empty!"
                                    isValid = false
                                }

                                if (quantity.isEmpty()) {
                                    quantityError = "Quantity cannot be empty!"
                                    isValid = false
                                } else {
                                    val qty = quantity.toDoubleOrNull()
                                    if (qty == null || qty <= 0) {
                                        quantityError = "Quantity must be a valid positive number!"
                                        isValid = false
                                    }
                                }

                                if (isValid) {
                                    onSearchClick(foodName, quantity.toDouble())
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6200EE)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Search", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            if (!errorMessage.isNullOrEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = "⚠️ $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (nutritionData != null) {
                item {
                    Text(
                        text = "📊 Nutrition Facts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    NutritionInfoCard(
                        icon = "🔥",
                        label = "Calories",
                        value = "${nutritionData.calories.toInt()} kcal",
                        color = Color(0xFFFF6B6B)
                    )
                }

                item {
                    NutritionInfoCard(
                        icon = "🥩",
                        label = "Protein",
                        value = "%.1f g".format(nutritionData.protein),
                        color = Color(0xFF4ECDC4)
                    )
                }

                item {
                    NutritionInfoCard(
                        icon = "🍞",
                        label = "Carbohydrates",
                        value = "%.1f g".format(nutritionData.carbs),
                        color = Color(0xFFFFA500)
                    )
                }

                item {
                    NutritionInfoCard(
                        icon = "🥑",
                        label = "Total Fat",
                        value = "%.1f g".format(nutritionData.fat),
                        color = Color(0xFF95E1D3)
                    )
                }

                item {
                    NutritionInfoCard(
                        icon = "🌾",
                        label = "Fiber",
                        value = "%.1f g".format(nutritionData.fiber),
                        color = Color(0xFFB19CD9)
                    )
                }

                item {
                    NutritionInfoCard(
                        icon = "🍯",
                        label = "Sugar",
                        value = "%.1f g".format(nutritionData.sugar),
                        color = Color(0xFFFDD835)
                    )
                }
            }
        }
    }
}

@Composable
fun NutritionInfoCard(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 28.sp)
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

data class NutritionData(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double
)
