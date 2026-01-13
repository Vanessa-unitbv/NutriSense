package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.R
import com.example.nutrisense.ui.screens.CalculateNutritionScreenCompose
import com.example.nutrisense.ui.screens.CalculateNutritionState
import com.example.nutrisense.ui.screens.NutritionResult
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.NutritionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalculateNutritionFragment : Fragment() {
    private val nutritionViewModel: NutritionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                var foodName by remember { mutableStateOf("") }
                var quantity by remember { mutableStateOf("100") }

                val uiState by nutritionViewModel.uiState.collectAsStateWithLifecycle()

                // Convert Food to NutritionResult
                val nutritionResult = if (uiState.searchResults.isNotEmpty()) {
                    val food = uiState.searchResults.first()
                    NutritionResult(
                        calories = food.calories,
                        protein = food.proteinG,
                        carbs = food.carbohydratesTotalG,
                        fatTotal = food.fatTotalG,
                        fatSaturated = food.fatSaturatedG,
                        sodium = food.sodiumMg,
                        potassium = food.potassiumMg,
                        cholesterol = food.cholesterolMg,
                        fiber = food.fiberG,
                        sugar = food.sugarG
                    )
                } else null

                NutriSenseTheme {
                    CalculateNutritionScreenCompose(
                        state = CalculateNutritionState(
                            foodName = foodName,
                            quantity = quantity,
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.errorMessage,
                            result = nutritionResult
                        ),
                        onFoodNameChange = {
                            foodName = it
                            nutritionViewModel.clearMessages()
                        },
                        onQuantityChange = {
                            quantity = it
                        },
                        onCalculateClick = {
                            if (foodName.isNotBlank()) {
                                val qty = quantity.toDoubleOrNull() ?: 100.0
                                nutritionViewModel.searchFoodNutrition(foodName, qty)
                            }
                        },
                        onBackClick = { goToDashboard() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goToDashboard()
        }
    }


    private fun goToDashboard() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }
}