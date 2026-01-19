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
import com.example.nutrisense.R
import com.example.nutrisense.data.dao.MealPlanWithRecipeData
import com.example.nutrisense.data.entity.DayOfWeek
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.ui.screens.MealPlanItem
import com.example.nutrisense.ui.screens.MealPlanScreenCompose
import com.example.nutrisense.ui.screens.MealPlanScreenState
import com.example.nutrisense.ui.screens.RecipeItem
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.MealPlanViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MealPlanFragment : Fragment() {

    private val mealPlanViewModel: MealPlanViewModel by viewModels()

    private var allMealPlans = mutableStateOf<List<MealPlanWithRecipeData>>(emptyList())
    private var allRecipes = mutableStateOf<List<Recipe>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by mealPlanViewModel.uiState.collectAsStateWithLifecycle()
                val selectedDay by mealPlanViewModel.selectedDay.collectAsStateWithLifecycle()
                val mealPlans by allMealPlans
                val recipes by allRecipes

                // Convert MealPlanWithRecipeData to MealPlanItem
                val mealPlanItems = mealPlans.map { data ->
                    MealPlanItem(
                        id = data.id,
                        recipeId = data.recipeId,
                        recipeTitle = data.recipeTitle,
                        recipeIngredients = data.recipeIngredients,
                        dayOfWeek = data.dayOfWeek,
                        mealType = data.mealType,
                        isFavorite = data.recipeIsFavorite
                    )
                }

                // Convert recipes to RecipeItems for the dialog
                val recipeItems = recipes.map { recipe ->
                    RecipeItem(
                        id = recipe.id.toInt(),
                        title = recipe.title,
                        ingredients = recipe.ingredients,
                        instructions = recipe.instructions,
                        servings = recipe.servings.toIntOrNull() ?: 1,
                        isFavorite = recipe.isFavorite
                    )
                }

                LaunchedEffect(uiState.successMessage) {
                    uiState.successMessage?.let {
                        requireContext().showSuccessToast(it)
                        mealPlanViewModel.clearMessages()
                    }
                }

                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let {
                        requireContext().showErrorToast(it)
                        mealPlanViewModel.clearMessages()
                    }
                }

                NutriSenseTheme {
                    MealPlanScreenCompose(
                        state = MealPlanScreenState(
                            mealPlans = mealPlanItems,
                            availableRecipes = recipeItems,
                            selectedDay = selectedDay,
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.errorMessage,
                            successMessage = uiState.successMessage
                        ),
                        onDaySelected = { day ->
                            mealPlanViewModel.selectDay(day)
                        },
                        onAddRecipeToDay = { recipeItem, day, mealType ->
                            recipes.find { it.id.toInt() == recipeItem.id }?.let { recipe ->
                                mealPlanViewModel.addRecipeToDay(recipe, day, mealType)
                            }
                        },
                        onRemoveMealPlan = { mealPlanId ->
                            showRemoveConfirmation(mealPlanId)
                        },
                        onClearDay = { day ->
                            showClearDayConfirmation(day)
                        },
                        onSearchRecipesClick = { navigateToRecipeSearch() },
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
            mealPlanViewModel.allMealPlans.collect { liveData ->
                liveData?.observe(viewLifecycleOwner) { mealPlans ->
                    allMealPlans.value = mealPlans
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mealPlanViewModel.userRecipes.collect { liveData ->
                liveData?.observe(viewLifecycleOwner) { recipes ->
                    allRecipes.value = recipes
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
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }

    private fun navigateToRecipeSearch() {
        try {
            findNavController().navigate(R.id.action_mealPlanFragment_to_recipeSearchFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Recipe Search")
        }
    }

    private fun showRemoveConfirmation(mealPlanId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Meal")
            .setMessage("Are you sure you want to remove this meal from your plan?")
            .setPositiveButton("Remove") { _, _ ->
                mealPlanViewModel.removeMealPlan(mealPlanId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearDayConfirmation(day: DayOfWeek) {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear ${day.displayName}")
            .setMessage("Are you sure you want to remove all meals for ${day.displayName}?")
            .setPositiveButton("Clear") { _, _ ->
                mealPlanViewModel.clearDayMeals(day)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
