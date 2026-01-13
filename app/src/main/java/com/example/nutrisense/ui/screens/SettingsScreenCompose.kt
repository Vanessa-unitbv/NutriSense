package com.example.nutrisense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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

data class SettingsScreenState(
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val calorieGoal: String = "2000",
    val waterGoal: String = "2000",
    val selectedGender: String = "Female",
    val selectedActivityLevel: String = "Moderate",
    val notificationsEnabled: Boolean = true,
    val waterReminderEnabled: Boolean = true,
    val waterInterval: String = "60",
    val bmiText: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenCompose(
    state: SettingsScreenState,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onCalorieGoalChange: (String) -> Unit,
    onWaterGoalChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onActivityLevelChange: (String) -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onWaterReminderEnabledChange: (Boolean) -> Unit,
    onWaterIntervalChange: (String) -> Unit,
    onCalculateGoals: () -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit,
    onAdvancedNotificationsClick: () -> Unit
) {
    val genders = listOf("Female", "Male")
    val activityLevels = listOf("Sedentary", "Light", "Moderate", "Active", "Very Active")

    NutriSenseTheme {
        Scaffold(
            topBar = {
                NutriSenseTopBar(
                    title = "⚙️ Settings",
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
                // Success/Error Messages
                state.successMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Success.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "✅ $message",
                            modifier = Modifier.padding(16.dp),
                            color = NutriSenseColors.Success,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                state.errorMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Error.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "❌ $message",
                            modifier = Modifier.padding(16.dp),
                            color = NutriSenseColors.Error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Personal Information Section
                NutriSenseCard(title = "👤 Personal Information") {
                    NutriSenseTextField(
                        value = state.age,
                        onValueChange = onAgeChange,
                        label = "Age (years)",
                        keyboardType = KeyboardType.Number
                    )

                    NutriSenseDropdown(
                        label = "Gender",
                        options = genders,
                        selectedOption = state.selectedGender,
                        onOptionSelected = onGenderChange
                    )
                }

                // Physical Data Section
                NutriSenseCard(title = "📊 Physical Data") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NutriSenseTextField(
                            value = state.weight,
                            onValueChange = onWeightChange,
                            label = "Weight (kg)",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        NutriSenseTextField(
                            value = state.height,
                            onValueChange = onHeightChange,
                            label = "Height (cm)",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (state.bmiText.isNotEmpty()) {
                        Text(
                            text = state.bmiText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    NutriSenseDropdown(
                        label = "Activity Level",
                        options = activityLevels,
                        selectedOption = state.selectedActivityLevel,
                        onOptionSelected = onActivityLevelChange
                    )
                }

                // Nutrition Goals Section
                NutriSenseCard(title = "🎯 Nutrition Goals") {
                    NutriSenseTextField(
                        value = state.calorieGoal,
                        onValueChange = onCalorieGoalChange,
                        label = "Daily Calorie Goal (kcal)",
                        keyboardType = KeyboardType.Number
                    )

                    NutriSenseTextField(
                        value = state.waterGoal,
                        onValueChange = onWaterGoalChange,
                        label = "Daily Water Goal (ml)",
                        keyboardType = KeyboardType.Number
                    )

                    Button(
                        onClick = onCalculateGoals,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NutriSenseColors.Brown
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Calculate Recommended Goals")
                    }
                }

                // Notifications Section
                NutriSenseCard(title = "🔔 Notifications") {
                    NutriSenseSwitchRow(
                        title = "Enable Notifications",
                        checked = state.notificationsEnabled,
                        onCheckedChange = onNotificationsEnabledChange
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onAdvancedNotificationsClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notification Settings")
                    }
                }

                // Save Button
                NutriSenseButton(
                    text = "Save All Settings",
                    onClick = onSave,
                    isLoading = state.isLoading
                )

                // Back to Dashboard Button
                NutriSenseOutlinedButton(
                    text = "← Dashboard",
                    onClick = onBackClick
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
