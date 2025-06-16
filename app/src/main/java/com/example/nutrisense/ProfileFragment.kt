package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

class ProfileFragment : Fragment() {

    private val args: ProfileFragmentArgs by navArgs()
    private lateinit var authViewModel: AuthViewModel
    private lateinit var userPreferencesManager: SharedPreferencesManager
    private lateinit var globalPreferencesManager: SharedPreferencesManager

    private lateinit var emailEditText: EditText
    private lateinit var logoutButton: Button
    private lateinit var settingsButton: Button
    private lateinit var tvCalorieGoal: TextView
    private lateinit var tvWaterGoal: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvBMI: TextView
    private lateinit var tvLastUpdate: TextView

    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        initializeViews(view)
        loadUserData()
        setupClickListeners()
        setupBackPressHandler()
        updateProfileDisplay()
    }

    override fun onResume() {
        super.onResume()
        updateProfileDisplay()
    }

    private fun initializeComponents() {
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        globalPreferencesManager = SharedPreferencesManager.getGlobalInstance(requireContext())

        userPreferencesManager = SharedPreferencesManager.getInstance(
            requireContext(),
            args.email
        )

        SharedPreferencesManager.setCurrentUser(args.email)
    }

    private fun initializeViews(view: View) {
        emailEditText = view.findViewById(R.id.et_email)
        logoutButton = view.findViewById(R.id.btn_logout)
        settingsButton = view.findViewById(R.id.btn_settings)
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
        authViewModel.getUserByEmail(
            email = args.email,
            onSuccess = { user ->
                currentUser = user
                updateUI(user)
            },
            onError = { errorMessage ->
                showToast("Error loading profile: $errorMessage", true)
                emailEditText.setText(args.email)
            }
        )
    }

    private fun updateUI(user: User?) {
        if (user != null) {
            emailEditText.setText(user.email)
            val welcomeText = if (!user.firstName.isNullOrEmpty()) {
                "${user.firstName}'s Profile"
            } else {
                "My Profile"
            }
            view?.findViewById<TextView>(R.id.tv_title_profile)?.text = welcomeText
        } else {
            emailEditText.setText(args.email)
        }
    }

    private fun updateProfileDisplay() {
        tvCalorieGoal.text = "Daily Calorie Goal: ${userPreferencesManager.getDailyCalorieGoal()} kcal"
        tvWaterGoal.text = "Daily Water Goal: ${userPreferencesManager.getDailyWaterGoal()} ml"

        tvWeight.text = ProfileUtils.formatWeightDisplay(userPreferencesManager)

        val (bmiText, bmiColor) = ProfileUtils.getBMIDisplay(userPreferencesManager)
        tvBMI.text = bmiText
        tvBMI.setTextColor(resources.getColor(bmiColor, null))

        val lastUpdate = userPreferencesManager.getLastWeightUpdate()
        tvLastUpdate.text = if (lastUpdate > 0) {
            "Last weight update: ${ProfileUtils.formatTimeAgo(lastUpdate)}"
        } else {
            "Last weight update: Never"
        }

        settingsButton.text = ProfileUtils.getSettingsButtonText(userPreferencesManager)
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener { performLogout() }
        settingsButton.setOnClickListener { navigateToSettings() }

        emailEditText.setOnClickListener {
            val weight = userPreferencesManager.getUserWeight()
            val message = if (weight > 0) {
                "👋 Profile looks good! Use Settings to make changes or go back to Dashboard."
            } else {
                "🎯 Complete your profile in Settings to get personalized recommendations!"
            }
            showToast(message, false)
        }
    }

    private fun navigateToSettings() {
        try {
            val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            showToast("Error opening Settings: ${e.message}", true)
        }
    }

    private fun navigateToDashboard() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            showToast("Error navigating to Dashboard: ${e.message}", true)
        }
    }

    private fun performLogout() {
        globalPreferencesManager.setUserLoggedOut()
        showToast("Successfully logged out", false)
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

    private fun showToast(message: String, isLong: Boolean) {
        val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, message, duration).show()
    }
}