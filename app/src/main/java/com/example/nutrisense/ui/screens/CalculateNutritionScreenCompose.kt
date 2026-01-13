package com.example.nutrisense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrisense.ui.components.*
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class NutritionResult(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fatTotal: Double = 0.0,
    val fatSaturated: Double = 0.0,
    val sodium: Double = 0.0,
    val potassium: Double = 0.0,
    val cholesterol: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0
)

data class CalculateNutritionState(
    val foodName: String = "",
    val quantity: String = "100",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val result: NutritionResult? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculateNutritionScreenCompose(
    state: CalculateNutritionState,
    onFoodNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onCalculateClick: () -> Unit,
    onBackClick: () -> Unit
) {
    NutriSenseTheme {
        Scaffold(
            topBar = {
                NutriSenseTopBar(
                    title = "🍎 Calculate Nutrition",
                    onBackClick = onBackClick
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
                // Input Card
                NutriSenseCard(title = "🔍 Search Food") {
                    NutriSenseTextField(
                        value = state.foodName,
                        onValueChange = onFoodNameChange,
                        label = "Food name (e.g., apple, chicken breast)",
                        isError = state.errorMessage != null && state.foodName.isBlank()
                    )

                    NutriSenseTextField(
                        value = state.quantity,
                        onValueChange = onQuantityChange,
                        label = "Quantity (grams)",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onCalculateClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NutriSenseColors.Brown
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Calculate Nutrition",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Error Message
                state.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Error.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "❌ $error",
                            modifier = Modifier.padding(16.dp),
                            color = NutriSenseColors.Error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Results
                state.result?.let { result ->
                    NutriSenseCard(title = "📊 Nutrition Facts") {
                        // Main Nutrients
                        NutrientRow("🔥 Calories", "${result.calories.toInt()} kcal", NutriSenseColors.CaloriesColor)
                        NutrientRow("💪 Protein", "${result.protein.format()}g", NutriSenseColors.ProteinColor)
                        NutrientRow("🍞 Carbohydrates", "${result.carbs.format()}g", NutriSenseColors.CarbsColor)
                        NutrientRow("🥑 Total Fat", "${result.fatTotal.format()}g", NutriSenseColors.FatColor)
                    }

                    NutriSenseCard(title = "📋 Detailed Nutrients") {
                        NutrientRow("Saturated Fat", "${result.fatSaturated.format()}g", Color.White)
                        NutrientRow("Fiber", "${result.fiber.format()}g", NutriSenseColors.FiberColor)
                        NutrientRow("Sugar", "${result.sugar.format()}g", NutriSenseColors.SugarColor)
                        NutrientRow("Sodium", "${result.sodium.format()}mg", Color.White)
                        NutrientRow("Potassium", "${result.potassium.format()}mg", Color.White)
                        NutrientRow("Cholesterol", "${result.cholesterol.format()}mg", Color.White)
                    }
                }

                // Back Button
                NutriSenseOutlinedButton(
                    text = "← Back to Dashboard",
                    onClick = onBackClick
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun NutrientRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.3f))
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

private fun Double.format(): String = "%.1f".format(this)

