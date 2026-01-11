package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisense.R
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.adapters.RecipeAdapter
import com.example.nutrisense.viewmodel.RecipeViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipeHistoryFragment : Fragment() {

    private val recipeViewModel: RecipeViewModel by viewModels()

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var rvSavedRecipes: RecyclerView
    private lateinit var tvNoSavedRecipes: TextView
    private lateinit var tvRecipeSummary: TextView
    private lateinit var btnBackToDashboard: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        observeViewModel()
        setupBackPressHandler()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        rvSavedRecipes = view.findViewById(R.id.rv_saved_recipes)
        tvNoSavedRecipes = view.findViewById(R.id.tv_no_saved_recipes)
        tvRecipeSummary = view.findViewById(R.id.tv_recipe_summary)
        btnBackToDashboard = view.findViewById(R.id.btn_back_to_dashboard)
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onItemClick = { recipe -> showRecipeDetails(recipe) },
            onFavoriteClick = { recipe -> recipeViewModel.updateFavoriteStatus(recipe) },
            onDeleteClick = { recipe -> showDeleteConfirmation(recipe) }
        )

        rvSavedRecipes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goToDashboard()
        }
    }

    private fun setupClickListeners() {
        btnBackToDashboard.setOnClickListener {
            goToDashboard()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            recipeViewModel.userRecipes.collect { liveDataRecipes ->
                liveDataRecipes?.observe(viewLifecycleOwner) { recipes ->
                    recipeAdapter.submitList(recipes)
                    tvNoSavedRecipes.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
                    rvSavedRecipes.visibility = if (recipes.isEmpty()) View.GONE else View.VISIBLE
                    updateRecipeSummary(recipes)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            recipeViewModel.uiState.collect { state ->
                state.successMessage?.let {
                    requireContext().showSuccessToast(it)
                    recipeViewModel.clearMessages()
                }
                state.errorMessage?.let {
                    requireContext().showErrorToast(it)
                    recipeViewModel.clearMessages()
                }
            }
        }
    }

    private fun updateRecipeSummary(recipes: List<Recipe>) {
        if (recipes.isEmpty()) {
            tvRecipeSummary.text = "No recipes saved yet"
            return
        }

        val totalRecipes = recipes.size
        val favoriteRecipes = recipes.count { it.isFavorite }

        val summaryText = buildString {
            appendLine("Your Recipe Collection:")
            appendLine("Total saved recipes: $totalRecipes")
            appendLine("Favorite recipes: $favoriteRecipes")
        }

        tvRecipeSummary.text = summaryText
    }

    private fun showRecipeDetails(recipe: Recipe) {
        val details = buildString {
            appendLine("🍽️ ${recipe.title.uppercase()}")
            appendLine("👥 Servings: ${recipe.servings}")
            appendLine("🔍 Search query: ${recipe.searchQuery}")
            appendLine()
            appendLine("📝 INGREDIENTS:")
            appendLine(recipe.ingredients)
            appendLine()
            appendLine("👨‍🍳 INSTRUCTIONS:")
            appendLine(recipe.instructions)
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Recipe Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteConfirmation(recipe: Recipe) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete ${recipe.title}?")
            .setPositiveButton("Delete") { _, _ ->
                recipeViewModel.deleteRecipe(recipe)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun goToDashboard() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }
}