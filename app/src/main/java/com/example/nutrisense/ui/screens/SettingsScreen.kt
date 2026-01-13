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
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSaveClick: (settings: SettingsData) -> Unit,
    isLoading: Boolean = false,
    successMessage: String? = null
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var calorieGoal by remember { mutableStateOf("2000") }
    var waterGoal by remember { mutableStateOf("2000") }
    var selectedGender by remember { mutableStateOf("Female") }
    var selectedActivityLevel by remember { mutableStateOf("Moderate") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var waterReminderEnabled by remember { mutableStateOf(true) }
    var waterInterval by remember { mutableStateOf("60") }

    var weightError by remember { mutableStateOf("") }
    var heightError by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf("") }
    var calorieError by remember { mutableStateOf("") }
    var waterError by remember { mutableStateOf("") }

    val genders = listOf("Female", "Male")
    val activityLevels = listOf("Sedentary", "Light", "Moderate", "Active", "Very Active")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        TopAppBar(
            title = { Text("Settings", color = Color.White) },
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
            // Success Message
            if (!successMessage.isNullOrEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xE8F5E9))
                    ) {
                        Text(
                            text = "✅ $successMessage",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF2E7D32),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Personal Information Section
            item {
                SettingsSectionHeader("👤 Personal Information")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it; weightError = "" },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = weightError.isNotEmpty(),
                            supportingText = {
                                if (weightError.isNotEmpty()) {
                                    Text(weightError, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it; heightError = "" },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = heightError.isNotEmpty(),
                            supportingText = {
                                if (heightError.isNotEmpty()) {
                                    Text(heightError, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it; ageError = "" },
                            label = { Text("Age (years)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = ageError.isNotEmpty(),
                            supportingText = {
                                if (ageError.isNotEmpty()) {
                                    Text(ageError, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        // Gender Spinner
                        DropdownMenuField(
                            label = "Gender",
                            options = genders,
                            selectedOption = selectedGender,
                            onOptionSelected = { selectedGender = it }
                        )

                        // Activity Level Spinner
                        DropdownMenuField(
                            label = "Activity Level",
                            options = activityLevels,
                            selectedOption = selectedActivityLevel,
                            onOptionSelected = { selectedActivityLevel = it }
                        )
                    }
                }
            }

            // Daily Goals Section
            item {
                SettingsSectionHeader("🎯 Daily Goals")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = calorieGoal,
                            onValueChange = { calorieGoal = it; calorieError = "" },
                            label = { Text("Daily Calorie Goal (kcal)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = calorieError.isNotEmpty(),
                            supportingText = {
                                if (calorieError.isNotEmpty()) {
                                    Text(calorieError, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        OutlinedTextField(
                            value = waterGoal,
                            onValueChange = { waterGoal = it; waterError = "" },
                            label = { Text("Daily Water Goal (ml)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = waterError.isNotEmpty(),
                            supportingText = {
                                if (waterError.isNotEmpty()) {
                                    Text(waterError, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
            }

            // Notifications Section
            item {
                SettingsSectionHeader("🔔 Notifications")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable Notifications", fontSize = 14.sp)
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Water Reminders", fontSize = 14.sp)
                            Switch(
                                checked = waterReminderEnabled,
                                onCheckedChange = { waterReminderEnabled = it }
                            )
                        }

                        if (waterReminderEnabled) {
                            OutlinedTextField(
                                value = waterInterval,
                                onValueChange = { waterInterval = it },
                                label = { Text("Reminder Interval (minutes)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        var isValid = true

                        if (calorieGoal.isEmpty()) {
                            calorieError = "Calorie goal cannot be empty!"
                            isValid = false
                        } else {
                            val cal = calorieGoal.toIntOrNull()
                            if (cal == null || cal <= 0) {
                                calorieError = "Must be a valid positive number!"
                                isValid = false
                            }
                        }

                        if (waterGoal.isEmpty()) {
                            waterError = "Water goal cannot be empty!"
                            isValid = false
                        } else {
                            val water = waterGoal.toIntOrNull()
                            if (water == null || water <= 0) {
                                waterError = "Must be a valid positive number!"
                                isValid = false
                            }
                        }

                        if (weight.isNotEmpty()) {
                            val w = weight.toFloatOrNull()
                            if (w == null || w <= 0) {
                                weightError = "Must be a valid positive number!"
                                isValid = false
                            }
                        }

                        if (height.isNotEmpty()) {
                            val h = height.toFloatOrNull()
                            if (h == null || h <= 0) {
                                heightError = "Must be a valid positive number!"
                                isValid = false
                            }
                        }

                        if (age.isNotEmpty()) {
                            val a = age.toIntOrNull()
                            if (a == null || a <= 0 || a > 150) {
                                ageError = "Must be between 1 and 150!"
                                isValid = false
                            }
                        }

                        if (waterReminderEnabled && waterInterval.isEmpty()) {
                            // Handle error for water interval
                        }

                        if (isValid) {
                            onSaveClick(
                                SettingsData(
                                    weight = weight.toFloatOrNull(),
                                    height = height.toFloatOrNull(),
                                    age = age.toIntOrNull(),
                                    calorieGoal = calorieGoal.toIntOrNull() ?: 2000,
                                    waterGoal = waterGoal.toIntOrNull() ?: 2000,
                                    gender = selectedGender,
                                    activityLevel = selectedActivityLevel,
                                    notificationsEnabled = notificationsEnabled,
                                    waterReminderEnabled = waterReminderEnabled,
                                    waterInterval = if (waterReminderEnabled) waterInterval.toIntOrNull() ?: 60 else 0
                                )
                            )
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
                        Text("Save Settings", fontSize = 16.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun DropdownMenuField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(4.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selectedOption, color = Color.Black)
                Text("▼", color = Color.Gray)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

data class SettingsData(
    val weight: Float?,
    val height: Float?,
    val age: Int?,
    val calorieGoal: Int,
    val waterGoal: Int,
    val gender: String,
    val activityLevel: String,
    val notificationsEnabled: Boolean,
    val waterReminderEnabled: Boolean,
    val waterInterval: Int
)
