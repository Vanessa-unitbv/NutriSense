package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.data.preferences.SharedPreferencesManager
import com.example.nutrisense.ui.adapter.RecipeAdapter
import com.example.nutrisense.viewmodel.RecipeViewModel

class RecipeHistoryFragment : Fragment() {

    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var preferencesManager: SharedPreferencesManager

    private lateinit var rvSavedRecipes: RecyclerView
    private lateinit var tvNoSavedRecipes: TextView
    private lateinit var tvRecipeSummary: TextView
    private lateinit var btnLogout: Button
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
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        setupBackPressHandler()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        preferencesManager = SharedPreferencesManager.getInstance(requireContext())

        rvSavedRecipes = view.findViewById(R.id.rv_saved_recipes)
        tvNoSavedRecipes = view.findViewById(R.id.tv_no_saved_recipes)
        tvRecipeSummary = view.findViewById(R.id.tv_recipe_summary)
        btnLogout = view.findViewById(R.id.btn_logout)
        btnBackToDashboard = view.findViewById(R.id.btn_back_to_dashboard)
    }

    private fun setupViewModel() {
        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]
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
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
                requireActivity().finish()
            }
        }
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            performLogout()
        }

        btnBackToDashboard.setOnClickListener {
            goToDashboard()
        }
    }

    private fun observeViewModel() {
        recipeViewModel.userRecipes.observe(viewLifecycleOwner) { liveDataRecipes ->
            liveDataRecipes?.observe(viewLifecycleOwner) { recipes ->
                recipeAdapter.submitList(recipes)
                tvNoSavedRecipes.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
                rvSavedRecipes.visibility = if (recipes.isEmpty()) View.GONE else View.VISIBLE

                updateRecipeSummary(recipes)
            }
        }

        recipeViewModel.successMessage.observe(viewLifecycleOwner) { successMessage ->
            if (successMessage != null) {
                showToast(successMessage)
                recipeViewModel.clearMessages()
            }
        }

        recipeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                showToast(errorMessage)
                recipeViewModel.clearMessages()
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
            appendLine("📚 Your Recipe Collection:")
            appendLine("🍽️ Total saved recipes: $totalRecipes")
            appendLine("⭐ Favorite recipes: $favoriteRecipes")
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
            .setMessage("Are you sure you want to delete ${recipe.title} from your collection?")
            .setPositiveButton("Delete") { _, _ ->
                recipeViewModel.deleteRecipe(recipe)
                showToast("${recipe.title} deleted from your collection")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        preferencesManager.setUserLoggedOut()
        showToast("Successfully logged out")
        goToLogin()
    }

    private fun goToLogin() {
        try {
            findNavController().navigate(R.id.loginFragment)
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }

    private fun goToDashboard() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}