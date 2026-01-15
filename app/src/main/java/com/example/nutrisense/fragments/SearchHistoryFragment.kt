package com.example.nutrisense.fragments

import android.app.AlertDialog
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.ui.screens.FoodItem
import com.example.nutrisense.ui.screens.SearchHistoryScreenCompose
import com.example.nutrisense.ui.screens.SearchHistoryState
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.NutritionViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchHistoryFragment : Fragment() {
    private val nutritionViewModel: NutritionViewModel by viewModels()

    private var allFoods = mutableStateOf<List<Food>>(emptyList())
    private var todayConsumedFoods = mutableStateOf<List<Food>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by nutritionViewModel.uiState.collectAsStateWithLifecycle()
                val foods by allFoods
                val todayConsumed by todayConsumedFoods

                val todayCalories = todayConsumed.sumOf { it.calories.toInt() }
                val todayProtein = todayConsumed.sumOf { it.proteinG.toInt() }
                val todayCarbs = todayConsumed.sumOf { it.carbohydratesTotalG.toInt() }
                val todayFat = todayConsumed.sumOf { it.fatTotalG.toInt() }

                val foodItems = foods.map { food ->
                    FoodItem(
                        id = food.id,
                        name = food.name,
                        calories = food.calories,
                        protein = food.proteinG,
                        carbs = food.carbohydratesTotalG,
                        fat = food.fatTotalG,
                        quantity = food.requestedQuantityG,
                        isFavorite = food.isFavorite,
                        isConsumed = food.consumedAt != null
                    )
                }

                // Show messages
                LaunchedEffect(uiState.successMessage) {
                    uiState.successMessage?.let {
                        requireContext().showSuccessToast(it)
                        nutritionViewModel.clearMessages()
                    }
                }

                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let {
                        requireContext().showErrorToast(it)
                        nutritionViewModel.clearMessages()
                    }
                }

                NutriSenseTheme {
                    SearchHistoryScreenCompose(
                        state = SearchHistoryState(
                            foods = foodItems,
                            todayCalories = todayCalories,
                            todayProtein = todayProtein,
                            todayCarbs = todayCarbs,
                            todayFat = todayFat,
                            isLoading = uiState.isLoading
                        ),
                        onFoodClick = { foodItem ->
                            foods.find { it.id == foodItem.id }?.let { showFoodDetails(it) }
                        },
                        onFavoriteClick = { foodItem ->
                            foods.find { it.id == foodItem.id }?.let { nutritionViewModel.updateFavoriteStatus(it) }
                        },
                        onDeleteClick = { foodItem ->
                            foods.find { it.id == foodItem.id }?.let { showDeleteConfirmation(it) }
                        },
                        onConsumeClick = { foodItem ->
                            foods.find { it.id == foodItem.id }?.let { nutritionViewModel.markFoodAsConsumed(it) }
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
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            nutritionViewModel.userFoods.collect { liveData ->
                liveData?.observe(viewLifecycleOwner) { foods ->
                    allFoods.value = foods
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            nutritionViewModel.userTodayConsumedFoods.collect { liveData ->
                liveData?.observe(viewLifecycleOwner) { foods ->
                    todayConsumedFoods.value = foods
                }
            }
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goToDashboard()
        }
    }

    private fun goToDashboard() {
        findNavController().popBackStack()
    }

    private fun showFoodDetails(food: Food) {
        val details = buildString {
            appendLine("📊 ${food.name.uppercase()}")
            appendLine("🔍 Original search: ${food.originalQuery}")
            appendLine("⚖️ Quantity: ${food.requestedQuantityG.toInt()}g")
            appendLine("🔥 Calories: ${food.calories.toInt()} kcal")
            appendLine()
            appendLine("MACRONUTRIENTS:")
            appendLine("🥩 Protein: %.1f g".format(food.proteinG))
            appendLine("🍞 Carbs: %.1f g".format(food.carbohydratesTotalG))
            appendLine("🥑 Fat: %.1f g".format(food.fatTotalG))
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Food Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteConfirmation(food: Food) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Food")
            .setMessage("Are you sure you want to delete '${food.name}'?")
            .setPositiveButton("Delete") { _, _ -> nutritionViewModel.deleteFood(food) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}