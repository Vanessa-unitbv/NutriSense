package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.R
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val args: DashboardFragmentArgs by navArgs()

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var btnCalculateNutrition: Button
    private lateinit var btnViewHistory: Button
    private lateinit var btnRecipeSearch: Button
    private lateinit var btnRecipeHistory: Button
    private lateinit var btnSettings: Button
    private lateinit var btnProfile: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        loadUserData()
        setupClickListeners()
        setupBackPressHandler()
    }

    private fun initializeViews(view: View) {
        btnCalculateNutrition = view.findViewById(R.id.btn_calculate_nutrition)
        btnViewHistory = view.findViewById(R.id.btn_view_history)
        btnRecipeSearch = view.findViewById(R.id.btn_recipe_search)
        btnRecipeHistory = view.findViewById(R.id.btn_recipe_history)
        btnSettings = view.findViewById(R.id.btn_settings)
        btnProfile = view.findViewById(R.id.btn_profile)
    }

    private fun loadUserData() {
        // Încarcă datele utilizatorului (opțional, pentru validare)
        authViewModel.getUserByEmail(args.email)
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
    }

    private fun navigateToSearchHistory() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_searchHistoryFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Search History: ${e.message}")
        }
    }

    private fun navigateToCalculateNutrition() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_calculateNutritionFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Nutrition Calculator: ${e.message}")
        }
    }

    private fun navigateToSettings() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Settings: ${e.message}")
        }
    }

    private fun navigateToProfile() {
        try {
            val action = DashboardFragmentDirections.actionDashboardFragmentToProfileFragment(args.email)
            findNavController().navigate(action)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Profile: ${e.message}")
        }
    }

    private fun navigateToRecipeSearch() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_recipeSearchFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Recipe Search: ${e.message}")
        }
    }

    private fun navigateToRecipeHistory() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_recipeHistoryFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Recipe Collection: ${e.message}")
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }
}