package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.R
import com.example.nutrisense.utils.ProfileUtils
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.viewmodel.ProfileViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val args: ProfileFragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var emailEditText: EditText
    private lateinit var logoutButton: Button
    private lateinit var settingsButton: Button
    private lateinit var dashboardButton: Button
    private lateinit var tvCalorieGoal: TextView
    private lateinit var tvWaterGoal: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvBMI: TextView
    private lateinit var tvLastUpdate: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        profileViewModel.loadProfile(args.email)
        loadUserData()
        setupClickListeners()
        setupBackPressHandler()
        observeProfileState()
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.refresh()
    }

    private fun initializeViews(view: View) {
        emailEditText = view.findViewById(R.id.et_email)
        logoutButton = view.findViewById(R.id.btn_logout)
        settingsButton = view.findViewById(R.id.btn_settings)
        dashboardButton = view.findViewById(R.id.btn_dashboard)
        tvCalorieGoal = view.findViewById(R.id.tv_calorie_goal)
        tvWaterGoal = view.findViewById(R.id.tv_water_goal)
        tvWeight = view.findViewById(R.id.tv_weight)
        tvBMI = view.findViewById(R.id.tv_bmi)
        tvLastUpdate = view.findViewById(R.id.tv_last_update)

        emailEditText.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            clearFocus()
        }
        view.clearFocus()
    }

    private fun loadUserData() {
        authViewModel.getUserByEmail(args.email)

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.userData.collect { user ->
                user?.let {
                    emailEditText.setText(it.email)
                    val welcomeText = if (!it.firstName.isNullOrEmpty()) {
                        "${it.firstName}'s Profile"
                    } else {
                        "My Profile"
                    }
                    view?.findViewById<TextView>(R.id.tv_title_profile)?.text = welcomeText
                } ?: run {
                    emailEditText.setText(args.email)
                }
            }
        }
    }

    private fun observeProfileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.profileUiState.collect { state ->
                tvCalorieGoal.text = state.dailyCalorieGoalText
                tvWaterGoal.text = state.dailyWaterGoalText
                tvWeight.text = state.weightDisplay
                tvBMI.text = state.bmiText
                tvBMI.setTextColor(resources.getColor(state.bmiColor, null))
                tvLastUpdate.text = state.lastUpdateText
                settingsButton.text = state.settingsButtonText

                view?.findViewById<TextView>(R.id.tv_title_profile)?.text = state.welcomeTitle

                if (emailEditText.text.isNullOrEmpty()) {
                    emailEditText.setText(state.email)
                }
            }
        }
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            performLogout()
        }

        settingsButton.setOnClickListener {
            navigateToSettings()
        }

        dashboardButton.setOnClickListener {
            navigateToDashboard()
        }

        emailEditText.setOnClickListener {
            val weight = profileViewModel.profileUiState.value.rawWeight
            val message = if (weight > 0) {
                "Profile looks good! Use Settings to make changes or go back to Dashboard."
            } else {
                "Complete your profile in Settings to get personalized recommendations!"
            }
            requireContext().showToast(message, false)
        }
    }

    private fun navigateToSettings() {
        try {
            val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error opening Settings: ${e.message}")
        }
    }

    private fun navigateToDashboard() {
        try {
            val action = ProfileFragmentDirections.actionProfileFragmentToDashboardFragment(args.email)
            findNavController().navigate(action)
        } catch (e: Exception) {
            requireContext().showErrorToast("Error navigating to Dashboard: ${e.message}")
        }
    }

    private fun performLogout() {
        authViewModel.logoutUser()
        requireContext().showSuccessToast("Successfully logged out")
        goToLogin()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToDashboard()
        }
    }

    private fun goToLogin() {
        findNavController().navigate(R.id.loginFragment)
    }
}