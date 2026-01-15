package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.R
import com.example.nutrisense.ui.screens.SettingsScreenCompose
import com.example.nutrisense.ui.screens.SettingsScreenState
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

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

                LaunchedEffect(uiState) {
                    if (uiState.userWeight > 0) weight = uiState.userWeight.toString()
                    if (uiState.userHeight > 0) height = uiState.userHeight.toString()
                    if (uiState.userAge > 0) age = uiState.userAge.toString()
                    calorieGoal = uiState.dailyCalorieGoal.toString()
                    waterGoal = uiState.dailyWaterGoal.toString()
                    notificationsEnabled = uiState.notificationEnabled
                    waterReminderEnabled = uiState.waterReminderInterval > 0
                    if (uiState.waterReminderInterval > 0) {
                        waterInterval = uiState.waterReminderInterval.toString()
                    }
                    selectedActivityLevel = when (uiState.activityLevel) {
                        "sedentary" -> "Sedentary"
                        "light" -> "Light"
                        "moderate" -> "Moderate"
                        "active" -> "Active"
                        "very_active" -> "Very Active"
                        else -> "Moderate"
                    }
                }

                NutriSenseTheme {
                    SettingsScreenCompose(
                        state = SettingsScreenState(
                            weight = weight,
                            height = height,
                            age = age,
                            calorieGoal = calorieGoal,
                            waterGoal = waterGoal,
                            selectedGender = selectedGender,
                            selectedActivityLevel = selectedActivityLevel,
                            notificationsEnabled = notificationsEnabled,
                            waterReminderEnabled = waterReminderEnabled,
                            waterInterval = waterInterval,
                            bmiText = uiState.bmiDisplayText,
                            isLoading = false,
                            successMessage = uiState.successMessage,
                            errorMessage = uiState.errorMessage
                        ),
                        onWeightChange = { weight = it },
                        onHeightChange = { height = it },
                        onAgeChange = { age = it },
                        onCalorieGoalChange = { calorieGoal = it },
                        onWaterGoalChange = { waterGoal = it },
                        onGenderChange = { selectedGender = it },
                        onActivityLevelChange = { selectedActivityLevel = it },
                        onNotificationsEnabledChange = { notificationsEnabled = it },
                        onWaterReminderEnabledChange = { waterReminderEnabled = it },
                        onWaterIntervalChange = { waterInterval = it },
                        onCalculateGoals = {
                            val w = weight.toFloatOrNull() ?: return@SettingsScreenCompose
                            val h = height.toFloatOrNull() ?: return@SettingsScreenCompose
                            val a = age.toIntOrNull() ?: return@SettingsScreenCompose
                            val gender = if (selectedGender == "Female") "female" else "male"
                            val activity = when (selectedActivityLevel) {
                                "Sedentary" -> "sedentary"
                                "Light" -> "light"
                                "Moderate" -> "moderate"
                                "Active" -> "active"
                                "Very Active" -> "very_active"
                                else -> "moderate"
                            }
                            settingsViewModel.calculateRecommendedGoals(w, h, a, gender, activity, "metric")
                        },
                        onSave = {
                            val calories = calorieGoal.toIntOrNull() ?: 2000
                            val water = waterGoal.toIntOrNull() ?: 2000
                            val w = weight.toFloatOrNull()
                            val h = height.toFloatOrNull()
                            val a = age.toIntOrNull()
                            val activity = when (selectedActivityLevel) {
                                "Sedentary" -> "sedentary"
                                "Light" -> "light"
                                "Moderate" -> "moderate"
                                "Active" -> "active"
                                "Very Active" -> "very_active"
                                else -> "moderate"
                            }
                            val interval = if (waterReminderEnabled) waterInterval.toIntOrNull() ?: 60 else 0

                            settingsViewModel.saveAllSettings(
                                calorieGoal = calories,
                                waterGoal = water,
                                weight = w,
                                height = h,
                                age = a,
                                activityLevel = activity,
                                units = "metric",
                                notificationsEnabled = notificationsEnabled,
                                waterInterval = interval
                            )
                        },
                        onBackClick = {
                            try {
                                findNavController().popBackStack(R.id.dashboardFragment, false)
                            } catch (e: Exception) {
                                requireActivity().finish()
                            }
                        },
                        onAdvancedNotificationsClick = {
                            findNavController().navigate(R.id.action_settingsFragment_to_notificationSettingsFragment)
                        }
                    )
                }
            }
        }
    }
}