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
import com.example.nutrisense.ui.screens.ProfileScreenCompose
import com.example.nutrisense.ui.screens.ProfileState
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.viewmodel.ProfileViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val args: ProfileFragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val profileState by profileViewModel.profileUiState.collectAsStateWithLifecycle()
                val userData by authViewModel.userData.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    profileViewModel.loadProfile(args.email)
                    authViewModel.getUserByEmail(args.email)
                }

                NutriSenseTheme {
                    ProfileScreenCompose(
                        state = ProfileState(
                            email = args.email,
                            userName = userData?.firstName ?: args.email.substringBefore("@"),
                            calorieGoal = profileState.dailyCalorieGoal,
                            waterGoal = profileState.dailyWaterGoal,
                            weight = profileState.rawWeight,
                            bmi = profileState.bmiText,
                            lastUpdate = profileState.lastUpdateText
                        ),
                        onSettingsClick = { navigateToSettings() },
                        onDashboardClick = { navigateToDashboard() },
                        onLogoutClick = { performLogout() }
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
        profileViewModel.refresh()
    }

    private fun navigateToSettings() {
        try {
            val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Settings")
        }
    }

    private fun navigateToDashboard() {
        try {
            val action = ProfileFragmentDirections.actionProfileFragmentToDashboardFragment(args.email)
            findNavController().navigate(action)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error navigating to Dashboard")
        }
    }

    private fun performLogout() {
        authViewModel.logoutUser()
        requireContext().showSuccessToast("Successfully logged out")
        findNavController().navigate(R.id.loginFragment)
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToDashboard()
        }
    }
}