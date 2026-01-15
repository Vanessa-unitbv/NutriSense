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
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.ui.screens.RecipeItem
import com.example.nutrisense.ui.screens.RecipeSearchScreenCompose
import com.example.nutrisense.ui.screens.RecipeSearchState
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.RecipeViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipeSearchFragment : Fragment() {
    private val recipeViewModel: RecipeViewModel by viewModels()

    private var allRecipes = mutableStateOf<List<Recipe>>(emptyList())
    private var ingredients = mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by recipeViewModel.uiState.collectAsStateWithLifecycle()
                val recipes by allRecipes
                val ingredientsText by ingredients

                // Convert to RecipeItems
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
                        recipeViewModel.clearMessages()
                    }
                }

                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let {
                        requireContext().showErrorToast(it)
                        recipeViewModel.clearMessages()
                    }
                }

                NutriSenseTheme {
                    RecipeSearchScreenCompose(
                        state = RecipeSearchState(
                            ingredients = ingredientsText,
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.errorMessage,
                            recipes = recipeItems
                        ),
                        onIngredientsChange = { ingredients.value = it },
                        onSearchClick = {
                            if (ingredientsText.isNotBlank()) {
                                recipeViewModel.searchRecipes(ingredientsText)
                            }
                        },
                        onRecipeClick = { recipeItem ->
                            recipes.find { it.id.toInt() == recipeItem.id }?.let { showRecipeDetails(it) }
                        },
                        onFavoriteClick = { recipeItem ->
                            recipes.find { it.id.toInt() == recipeItem.id }?.let { recipeViewModel.updateFavoriteStatus(it) }
                        },
                        onDeleteClick = { recipeItem ->
                            recipes.find { it.id.toInt() == recipeItem.id }?.let { showDeleteConfirmation(it) }
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
            recipeViewModel.userRecipes.collect { liveData ->
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
        findNavController().popBackStack()
    }

    private fun showRecipeDetails(recipe: Recipe) {
        val details = buildString {
            appendLine("🍽️ ${recipe.title.uppercase()}")
            appendLine("👥 Servings: ${recipe.servings}")
            appendLine()
            appendLine("📝 INGREDIENTS:")
            appendLine(recipe.ingredients)
            appendLine()
            appendLine("👨‍🍳 INSTRUCTIONS:")
            appendLine(recipe.instructions)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Recipe Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteConfirmation(recipe: Recipe) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete '${recipe.title}'?")
            .setPositiveButton("Delete") { _, _ -> recipeViewModel.deleteRecipe(recipe) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}