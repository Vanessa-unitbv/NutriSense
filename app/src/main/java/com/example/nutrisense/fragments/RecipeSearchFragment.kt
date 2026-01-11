package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
class RecipeSearchFragment : Fragment() {
    private val recipeViewModel: RecipeViewModel by viewModels()

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var etIngredients: EditText
    private lateinit var btnSearchRecipes: Button
    private lateinit var btnBackToDashboard: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    private lateinit var rvRecipes: RecyclerView
    private lateinit var tvNoRecipes: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        setupBackPressHandler()
    }

    private fun initializeViews(view: View) {
        etIngredients = view.findViewById(R.id.et_ingredients)
        btnSearchRecipes = view.findViewById(R.id.btn_search_recipes)
        btnBackToDashboard = view.findViewById(R.id.btn_back_to_dashboard)
        progressBar = view.findViewById(R.id.progress_bar)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)
        rvRecipes = view.findViewById(R.id.rv_recipes)
        tvNoRecipes = view.findViewById(R.id.tv_no_recipes)
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onItemClick = { recipe -> showRecipeDetails(recipe) },
            onFavoriteClick = { recipe -> recipeViewModel.updateFavoriteStatus(recipe) },
            onDeleteClick = { recipe -> showDeleteConfirmation(recipe) }
        )

        rvRecipes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    private fun setupClickListeners() {
        btnSearchRecipes.setOnClickListener {
            searchRecipes()
        }

        btnBackToDashboard.setOnClickListener {
            goToDashboard()
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goToDashboard()
        }
    }

    private fun searchRecipes() {
        val ingredients = etIngredients.getTextString()

        if (ingredients.isEmpty()) {
            requireContext().showErrorToast("Please enter ingredients")
            return
        }

        recipeViewModel.searchRecipes(ingredients)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            recipeViewModel.uiState.collect { state ->

                progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                btnSearchRecipes.isEnabled = !state.isLoading

                if (state.errorMessage != null) {
                    tvErrorMessage.text = state.errorMessage
                    tvErrorMessage.visibility = View.VISIBLE
                    recipeViewModel.clearMessages()
                } else {
                    tvErrorMessage.visibility = View.GONE
                }

                if (state.searchResults.isNotEmpty()) {
                    recipeAdapter.submitList(state.searchResults)
                    rvRecipes.visibility = View.VISIBLE
                    tvNoRecipes.visibility = View.GONE
                } else if (!state.isLoading && state.errorMessage == null) {
                    rvRecipes.visibility = View.GONE
                    tvNoRecipes.visibility = View.VISIBLE
                }

                state.successMessage?.let {
                    requireContext().showSuccessToast(it)
                    recipeViewModel.clearMessages()
                }
            }
        }
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
            appendLine()
            appendLine("🔍 Search query: ${recipe.searchQuery}")
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