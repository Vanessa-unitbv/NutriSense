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
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.data.preferences.SharedPreferencesManager
import com.example.nutrisense.utils.ProfileUtils
import com.example.nutrisense.viewmodel.AuthViewModel

class DashboardFragment : Fragment() {

    private val args: DashboardFragmentArgs by navArgs()
    private lateinit var authViewModel: AuthViewModel
    private lateinit var preferencesManager: SharedPreferencesManager

    private lateinit var btnCalculateNutrition: Button
    private lateinit var btnViewHistory: Button
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
            val action = DashboardFragmentDirections.actionDashboardFragmentToSearchHistoryFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            showToast("Error opening Search History: ${e.message}", true)
        }
    }

    private fun navigateToCalculateNutrition() {
        try {
            val action = DashboardFragmentDirections.actionDashboardFragmentToCalculateNutritionFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            showToast("Error opening Nutrition Calculator: ${e.message}", true)
        }
    }

    private fun navigateToSettings() {
        try {
            val action = DashboardFragmentDirections.actionDashboardFragmentToSettingsFragment()
            findNavController().navigate(action)
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