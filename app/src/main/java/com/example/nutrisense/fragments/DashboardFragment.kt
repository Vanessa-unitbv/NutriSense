package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.R
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.viewmodel.AuthViewModel

class DashboardFragment : Fragment() {

    private val args: DashboardFragmentArgs by navArgs()
    private lateinit var authViewModel: AuthViewModel
    private lateinit var preferencesManager: SharedPreferencesManager

    private lateinit var btnCalculateNutrition: Button
    private lateinit var btnViewHistory: Button
    private lateinit var btnRecipeSearch: Button
    private lateinit var btnRecipeHistory: Button
    private lateinit var btnSettings: Button
    private lateinit var btnProfile: Button
    private lateinit var btnLogout: Button

    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        initializeViews(view)
        loadUserData()
        setupClickListeners()
        setupBackPressHandler()
    }

    private fun initializeComponents() {
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        preferencesManager = SharedPreferencesManager.getInstance(requireContext())
    }

    private fun initializeViews(view: View) {
        btnCalculateNutrition = view.findViewById(R.id.btn_calculate_nutrition)
        btnViewHistory = view.findViewById(R.id.btn_view_history)
        btnRecipeSearch = view.findViewById(R.id.btn_recipe_search)
        btnRecipeHistory = view.findViewById(R.id.btn_recipe_history)
        btnSettings = view.findViewById(R.id.btn_settings)
        btnProfile = view.findViewById(R.id.btn_profile)
        btnLogout = view.findViewById(R.id.btn_logout)
    }

    private fun loadUserData() {
        authViewModel.getUserByEmail(
            email = args.email,
            onSuccess = { user ->
                currentUser = user
            },
            onError = { errorMessage ->
                showToast("Error loading profile: $errorMessage", true)
            }
        )
    }

    private fun setupClickListeners() {
        btnCalculateNutrition.setOnClickListener {
            navigateToCalculateNutrition()
        }

        btnViewHistory.setOnClickListener {
            navigateToSearchHistory()
        }

        btnRecipeSearch.setOnClickListener {
            navigateToRecipeSearch()
        }

        btnRecipeHistory.setOnClickListener {
            navigateToRecipeHistory()
        }

        btnSettings.setOnClickListener {
            navigateToSettings()
        }

        btnProfile.setOnClickListener {
            navigateToProfile()
        }

        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun navigateToSearchHistory() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_searchHistoryFragment)
        } catch (e: Exception) {
            showToast("Error opening Search History: ${e.message}", true)
        }
    }

    private fun navigateToCalculateNutrition() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_calculateNutritionFragment)
        } catch (e: Exception) {
            showToast("Error opening Nutrition Calculator: ${e.message}", true)
        }
    }

    private fun navigateToSettings() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
        } catch (e: Exception) {
            showToast("Error opening Settings: ${e.message}", true)
        }
    }

    private fun navigateToProfile() {
        try {
            val action = DashboardFragmentDirections.actionDashboardFragmentToProfileFragment(args.email)
            findNavController().navigate(action)
        } catch (e: Exception) {
            showToast("Error opening Profile: ${e.message}", true)
        }
    }

    private fun navigateToRecipeSearch() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_recipeSearchFragment)
        } catch (e: Exception) {
            showToast("Error opening Recipe Search: ${e.message}", true)
        }
    }

    private fun navigateToRecipeHistory() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_recipeHistoryFragment)
        } catch (e: Exception) {
            showToast("Error opening Recipe Collection: ${e.message}", true)
        }
    }

    private fun performLogout() {
        preferencesManager.setUserLoggedOut()
        showToast("Successfully logged out", false)
        goToLogin()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun goToLogin() {
        findNavController().navigate(R.id.loginFragment)
    }

    private fun showToast(message: String, isLong: Boolean) {
        val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, message, duration).show()
    }
}