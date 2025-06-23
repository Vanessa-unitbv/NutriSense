package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisense.R
import com.example.nutrisense.data.entity.Recipe
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.adapters.RecipeAdapter
import com.example.nutrisense.viewmodel.RecipeViewModel

class RecipeSearchFragment : Fragment() {

    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var preferencesManager: SharedPreferencesManager

    private lateinit var etIngredients: EditText
    private lateinit var btnSearchRecipes: Button
    private lateinit var btnLogout: Button
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
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        setupBackPressHandler()
    }

    private fun initializeViews(view: View) {
        preferencesManager = SharedPreferencesManager.getInstance(requireContext())

        etIngredients = view.findViewById(R.id.et_ingredients)
        btnSearchRecipes = view.findViewById(R.id.btn_search_recipes)
        btnLogout = view.findViewById(R.id.btn_logout)
        btnBackToDashboard = view.findViewById(R.id.btn_back_to_dashboard)

        progressBar = view.findViewById(R.id.progress_bar)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)
        rvRecipes = view.findViewById(R.id.rv_recipes)
        tvNoRecipes = view.findViewById(R.id.tv_no_recipes)
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

        rvRecipes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    private fun setupClickListeners() {
        btnSearchRecipes.setOnClickListener {
            searchRecipes()
        }

        btnLogout.setOnClickListener {
            performLogout()
        }

        btnBackToDashboard.setOnClickListener {
            goToDashboard()
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

    private fun observeViewModel() {
        recipeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading: Boolean ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSearchRecipes.isEnabled = !isLoading
        }

        recipeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage: String? ->
            if (errorMessage != null) {
                tvErrorMessage.text = errorMessage
                tvErrorMessage.visibility = View.VISIBLE
            } else {
                tvErrorMessage.visibility = View.GONE
            }
        }

        recipeViewModel.searchResults.observe(viewLifecycleOwner) { searchResults: List<Recipe> ->
            if (searchResults.isNotEmpty()) {
                recipeAdapter.submitList(searchResults)
                rvRecipes.visibility = View.VISIBLE
                tvNoRecipes.visibility = View.GONE
                showToast("Found ${searchResults.size} recipe(s)!")
            } else {
                rvRecipes.visibility = View.GONE
                tvNoRecipes.visibility = View.VISIBLE
            }
        }

        recipeViewModel.successMessage.observe(viewLifecycleOwner) { successMessage: String? ->
            if (successMessage != null) {
                showToast(successMessage)
                recipeViewModel.clearMessages()
            }
        }
    }

    private fun searchRecipes() {
        val ingredients = etIngredients.text.toString().trim()

        if (ingredients.isEmpty()) {
            showToast("Please enter ingredients")
            return
        }

        recipeViewModel.searchRecipes(ingredients)
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
            .setMessage("Are you sure you want to delete ${recipe.title} from your saved recipes?")
            .setPositiveButton("Delete") { _, _ ->
                recipeViewModel.deleteRecipe(recipe)
                showToast("${recipe.title} deleted from your recipes")
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