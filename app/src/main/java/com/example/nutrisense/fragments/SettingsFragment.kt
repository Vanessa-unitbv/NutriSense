package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.R
import com.example.nutrisense.viewmodel.SettingsViewModel
import com.example.nutrisense.helpers.extensions.*
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var etWeight: TextInputEditText
    private lateinit var etHeight: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var etCalories: TextInputEditText
    private lateinit var etWater: TextInputEditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerActivity: Spinner
    private lateinit var spinnerUnits: Spinner
    private lateinit var tvBMI: TextView
    private lateinit var switchNotifications: Switch
    private lateinit var switchWaterReminder: Switch
    private lateinit var etWaterInterval: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnCalculateGoals: Button
    private lateinit var btnBackToDashboard: Button

    private var suppressActivitySpinnerCallback: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupSpinners()
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        etWeight = view.findViewById(R.id.et_weight)
        etHeight = view.findViewById(R.id.et_height)
        etAge = view.findViewById(R.id.et_age)
        etCalories = view.findViewById(R.id.et_calorie_goal)
        etWater = view.findViewById(R.id.et_water_goal)
        spinnerGender = view.findViewById(R.id.spinner_gender)
        spinnerActivity = view.findViewById(R.id.spinner_activity_level)
        spinnerUnits = view.findViewById(R.id.spinner_units)
        tvBMI = view.findViewById(R.id.tv_bmi_result)
        switchNotifications = view.findViewById(R.id.switch_notifications)
        switchWaterReminder = view.findViewById(R.id.switch_water_reminder)
        etWaterInterval = view.findViewById(R.id.et_water_interval)
        btnSave = view.findViewById(R.id.btn_save)
        btnCalculateGoals = view.findViewById(R.id.btn_calculate_goals)
        btnBackToDashboard = view.findViewById(R.id.btn_back_to_dashboard)
    }

    private fun setupSpinners() {
        val genders = arrayOf(getString(R.string.female), getString(R.string.male))
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genders)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = genderAdapter

        val activityLevels = arrayOf(
            getString(R.string.sedentary),
            getString(R.string.light_activity),
            getString(R.string.moderate_activity),
            getString(R.string.active),
            getString(R.string.very_active)
        )
        val activityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityLevels)
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerActivity.adapter = activityAdapter

        val units = arrayOf(getString(R.string.metric_units), getString(R.string.imperial_units))
        val unitsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnits.adapter = unitsAdapter
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveAllSettings()
        }

        btnCalculateGoals.setOnClickListener {
            calculateRecommendedGoals()
        }

        btnBackToDashboard.setOnClickListener {
            goBack()
        }

        spinnerUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val units = if (position == 0) "metric" else "imperial"
                settingsViewModel.updateUnits(units)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerActivity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (suppressActivitySpinnerCallback) return
                val activityLevel = when (position) {
                    0 -> "sedentary"
                    1 -> "light"
                    2 -> "moderate"
                    3 -> "active"
                    4 -> "very_active"
                    else -> "moderate"
                }
                settingsViewModel.updateActivityLevel(activityLevel)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            settingsViewModel.uiState.collect { state ->
                updateUIFromState(state)

                state.errorMessage?.let {
                    requireContext().showErrorToast(it)
                    settingsViewModel.clearMessages()
                }

                state.successMessage?.let {
                    requireContext().showSuccessToast(it)
                    settingsViewModel.clearMessages()
                }
            }
        }
    }

    private fun updateUIFromState(state: SettingsViewModel.SettingsUiState) {
        etCalories.setText(state.dailyCalorieGoal.toString())
        etWater.setText(state.dailyWaterGoal.toString())

        if (state.userWeight > 0) etWeight.setText(state.userWeight.toString())
        if (state.userHeight > 0) etHeight.setText(state.userHeight.toString())
        if (state.userAge > 0) etAge.setText(state.userAge.toString())

        tvBMI.text = state.bmiDisplayText
        tvBMI.setTextColor(resources.getColor(state.bmiColorResource, null))

        val activityPosition = when (state.activityLevel) {
            "sedentary" -> 0
            "light" -> 1
            "moderate" -> 2
            "active" -> 3
            "very_active" -> 4
            else -> 2
        }
        if (spinnerActivity.selectedItemPosition != activityPosition) {
            suppressActivitySpinnerCallback = true
            spinnerActivity.setSelection(activityPosition)
            suppressActivitySpinnerCallback = false
        }

        spinnerUnits.setSelection(if (state.preferredUnits == "metric") 0 else 1)

        switchNotifications.isChecked = state.notificationEnabled
        switchWaterReminder.isChecked = state.waterReminderInterval > 0
        etWaterInterval.setText(
            if (state.waterReminderInterval > 0) state.waterReminderInterval.toString() else "60"
        )
    }

    private fun calculateRecommendedGoals() {
        val weightText = etWeight.getTextString()
        val heightText = etHeight.getTextString()
        val ageText = etAge.getTextString()

        if (!etWeight.validateNumberField("Weight", 1f, 500f)) {
            return
        }
        if (!etHeight.validateNumberField("Height", 1f, 300f)) {
            return
        }
        if (!etAge.validateNumberField("Age", 1f, 150f)) {
            return
        }

        val weight = weightText.toFloatOrNull() ?: return
        val height = heightText.toFloatOrNull() ?: return
        val age = ageText.toIntOrNull() ?: return

        val gender = if (spinnerGender.selectedItemPosition == 0) "female" else "male"
        val activityLevel = settingsViewModel.uiState.value.activityLevel
        val units = if (spinnerUnits.selectedItemPosition == 0) "metric" else "imperial"

        settingsViewModel.calculateRecommendedGoals(
            weight, height, age, gender, activityLevel, units
        )
    }

    private fun saveAllSettings() {
        val caloriesText = etCalories.getTextString()
        val waterText = etWater.getTextString()

        if (!etCalories.validateNumberField("Calorie goal", 100f, 10000f)) {
            return
        }
        if (!etWater.validateNumberField("Water goal", 100f, 10000f)) {
            return
        }

        val calories = caloriesText.toIntOrNull() ?: return
        val water = waterText.toIntOrNull() ?: return

        val weight = etWeight.getTextString().toFloatOrNull()
        val height = etHeight.getTextString().toFloatOrNull()
        val age = etAge.getTextString().toIntOrNull()

        // Validare opționale - dacă sunt completate trebuie să fie valide
        if (weight != null && !etWeight.validateNumberField("Weight", 1f, 500f)) {
            return
        }
        if (height != null && !etHeight.validateNumberField("Height", 1f, 300f)) {
            return
        }
        if (age != null && !etAge.validateNumberField("Age", 1f, 150f)) {
            return
        }

        if (switchWaterReminder.isChecked) {
            val waterIntervalText = etWaterInterval.getTextString()
            if (!etWaterInterval.validateNumberField("Water interval", 1f, 1440f)) {
                return
            }
        }

        val activityLevel = when (spinnerActivity.selectedItemPosition) {
            0 -> "sedentary"
            1 -> "light"
            2 -> "moderate"
            3 -> "active"
            4 -> "very_active"
            else -> "moderate"
        }

        val units = if (spinnerUnits.selectedItemPosition == 0) "metric" else "imperial"

        val waterInterval = if (switchWaterReminder.isChecked) {
            etWaterInterval.getTextString().toIntOrNull() ?: 60
        } else {
            0
        }

        settingsViewModel.saveAllSettings(
            calorieGoal = calories,
            waterGoal = water,
            weight = weight,
            height = height,
            age = age,
            activityLevel = activityLevel,
            units = units,
            notificationsEnabled = switchNotifications.isChecked,
            waterInterval = waterInterval
        )
    }

    private fun goBack() {
        try {
            findNavController().popBackStack(R.id.dashboardFragment, false)
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }
}