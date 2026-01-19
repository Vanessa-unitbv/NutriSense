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
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.R
import com.example.nutrisense.ui.screens.DashboardScreenCompose
import com.example.nutrisense.ui.screens.DashboardState
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.viewmodel.DashboardViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val args: DashboardFragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    dashboardViewModel.loadDashboard(args.email)
                }

                NutriSenseTheme {
                    DashboardScreenCompose(
                        state = DashboardState(
                            userName = uiState.userName,
                            dailyCalorieGoal = uiState.dailyCalorieGoal,
                            dailyCaloriesConsumed = uiState.dailyCaloriesConsumed,
                            dailyWaterGoal = uiState.dailyWaterGoal,
                            dailyWaterConsumed = uiState.dailyWaterConsumed,
                            dailyProtein = uiState.dailyProtein,
                            dailyCarbs = uiState.dailyCarbs,
                            dailyFat = uiState.dailyFat
                        ),
                        onCalculateNutritionClick = { navigateToCalculateNutrition() },
                        onSearchHistoryClick = { navigateToSearchHistory() },
                        onRecipeSearchClick = { navigateToRecipeSearch() },
                        onMealPlanClick = { navigateToMealPlan() },
                        onSettingsClick = { navigateToSettings() },
                        onProfileClick = { navigateToProfile() },
                        onLogoutClick = {
                            authViewModel.logoutUser()
                            requireActivity().finish()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.refresh()
    }

    private fun navigateToSearchHistory() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_searchHistoryFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Search History")
        }
    }

    private fun navigateToCalculateNutrition() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_calculateNutritionFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Nutrition Calculator")
        }
    }

    private fun navigateToSettings() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Settings")
        }
    }

    private fun navigateToProfile() {
        try {
            val action = DashboardFragmentDirections.actionDashboardFragmentToProfileFragment(args.email)
            findNavController().navigate(action)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Profile")
        }
    }

    private fun navigateToRecipeSearch() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_recipeSearchFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Recipe Search")
        }
    }


    private fun navigateToMealPlan() {
        try {
            findNavController().navigate(R.id.action_dashboardFragment_to_mealPlanFragment)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Meal Plan")
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }
}