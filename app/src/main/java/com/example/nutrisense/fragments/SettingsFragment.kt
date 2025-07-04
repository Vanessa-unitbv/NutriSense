package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.R
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.utils.NutritionCalculator
import com.example.nutrisense.helpers.extensions.*
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment() {

    private lateinit var userPreferencesManager: SharedPreferencesManager
    private lateinit var globalPreferencesManager: SharedPreferencesManager

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializePreferences()
        initializeViews(view)
        setupSpinners()
        loadCurrentSettings()
        setupClickListeners()
    }

    private fun initializePreferences() {
        globalPreferencesManager = SharedPreferencesManager.getGlobalInstance(requireContext())
        val currentUserEmail = globalPreferencesManager.getUserEmail()

        userPreferencesManager = if (currentUserEmail != null) {
            SharedPreferencesManager.getInstance(requireContext(), currentUserEmail)
        } else {
            SharedPreferencesManager.getGlobalInstance(requireContext())
        }
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

    private fun loadCurrentSettings() {
        etCalories.setText(userPreferencesManager.getDailyCalorieGoal().toString())
        etWater.setText(userPreferencesManager.getDailyWaterGoal().toString())

        val weight = userPreferencesManager.getUserWeight()
        if (weight > 0) etWeight.setText(weight.toString())

        val height = userPreferencesManager.getUserHeight()
        if (height > 0) etHeight.setText(height.toString())

        val age = userPreferencesManager.getUserAge()
        if (age > 0) etAge.setText(age.toString())

        val currentActivity = userPreferencesManager.getActivityLevel()
        val activityPosition = when (currentActivity) {
            "sedentary" -> 0
            "light" -> 1
            "moderate" -> 2
            "active" -> 3
            "very_active" -> 4
            else -> 2
        }
        spinnerActivity.setSelection(activityPosition)

        val currentUnits = userPreferencesManager.getPreferredUnits()
        spinnerUnits.setSelection(if (currentUnits == "metric") 0 else 1)

        switchNotifications.isChecked = userPreferencesManager.isNotificationEnabled()
        switchWaterReminder.isChecked = userPreferencesManager.getWaterReminderInterval() > 0

        val waterInterval = userPreferencesManager.getWaterReminderInterval()
        etWaterInterval.setText(if (waterInterval > 0) waterInterval.toString() else "60")

        updateBMI()
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
                updateBMI()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateBMI() {
        val weightText = etWeight.getTextString()
        val heightText = etHeight.getTextString()
        val isImperial = spinnerUnits.selectedItemPosition == 1

        val weight = weightText.toFloatOrNull()
        val height = heightText.toFloatOrNull()

        if (weight != null && height != null && weight > 0 && height > 0) {
            val heightInMeters = if (isImperial) {
                height * 0.0254f
            } else {
                height / 100f
            }

            val weightInKg = if (isImperial) {
                weight * 0.453592f
            } else {
                weight
            }

            val bmi = NutritionCalculator.calculateBMI(weightInKg, heightInMeters)
            val bmiCategory = NutritionCalculator.getBMICategory(bmi)

            val bmiColor = when (bmiCategory) {
                "Normal" -> resources.getColor(android.R.color.holo_green_dark, null)
                "Underweight" -> resources.getColor(android.R.color.holo_blue_dark, null)
                "Overweight" -> resources.getColor(android.R.color.holo_orange_dark, null)
                else -> resources.getColor(android.R.color.holo_red_dark, null)
            }

            tvBMI.text = "BMI: %.1f (%s)".format(bmi, bmiCategory)
            tvBMI.setTextColor(bmiColor)
        } else {
            tvBMI.text = getString(R.string.bmi_display)
            tvBMI.setTextColor(resources.getColor(android.R.color.tertiary_text_light, null))
        }
    }

    private fun calculateRecommendedGoals() {
        val weightText = etWeight.getTextString()
        val heightText = etHeight.getTextString()
        val ageText = etAge.getTextString()

        etWeight.clearErrorAndFocus()
        etHeight.clearErrorAndFocus()
        etAge.clearErrorAndFocus()

        if (!weightText.isValidWeight(getCurrentUnits())) {
            etWeight.setErrorAndFocus("Please enter a valid weight")
            return
        }

        if (!heightText.isValidHeight(getCurrentUnits())) {
            etHeight.setErrorAndFocus("Please enter a valid height")
            return
        }

        if (!ageText.isValidAge()) {
            etAge.setErrorAndFocus("Please enter a valid age (13-120)")
            return
        }

        val weight = weightText.toFloat()
        val height = heightText.toFloat()
        val age = ageText.toInt()

        val gender = if (spinnerGender.selectedItemPosition == 0) "female" else "male"
        val activityLevel = when (spinnerActivity.selectedItemPosition) {
            0 -> "sedentary"
            1 -> "light"
            2 -> "moderate"
            3 -> "active"
            4 -> "very_active"
            else -> "moderate"
        }

        try {
            val isImperial = spinnerUnits.selectedItemPosition == 1
            val weightInKg = if (isImperial) weight * 0.453592f else weight
            val heightInCm = if (isImperial) height * 2.54f else height

            val bmr = NutritionCalculator.calculateBMR(weightInKg, heightInCm, age, gender)
            val recommendedCalories = NutritionCalculator.calculateDailyCalorieNeeds(bmr, activityLevel)
            val recommendedWater = NutritionCalculator.calculateWaterIntake(weightInKg, activityLevel)

            etCalories.setText(recommendedCalories.toString())
            etWater.setText(recommendedWater.toString())

            requireContext().showSuccessToast("Goals calculated successfully!")

        } catch (e: Exception) {
            requireContext().showErrorToast("Error calculating goals: ${e.message}")
        }
    }

    private fun saveAllSettings() {
        try {
            val caloriesText = etCalories.getTextString()
            val waterText = etWater.getTextString()

            if (!caloriesText.isValidCalorieGoal()) {
                etCalories.setErrorAndFocus("Please enter a valid calorie goal (800-5000)")
                return
            }

            if (!waterText.isValidWaterGoal()) {
                etWater.setErrorAndFocus("Please enter a valid water goal (500-5000 ml)")
                return
            }

            val calories = caloriesText.toInt()
            val water = waterText.toInt()

            userPreferencesManager.setDailyCalorieGoal(calories)
            userPreferencesManager.setDailyWaterGoal(water)

            val weightText = etWeight.getTextString()
            val heightText = etHeight.getTextString()
            val ageText = etAge.getTextString()

            if (weightText.isNotEmpty()) {
                if (weightText.isValidWeight(getCurrentUnits())) {
                    userPreferencesManager.setUserWeight(weightText.toFloat())
                } else {
                    etWeight.setErrorAndFocus("Invalid weight")
                    return
                }
            }

            if (heightText.isNotEmpty()) {
                if (heightText.isValidHeight(getCurrentUnits())) {
                    userPreferencesManager.setUserHeight(heightText.toFloat())
                } else {
                    etHeight.setErrorAndFocus("Invalid height")
                    return
                }
            }

            if (ageText.isNotEmpty()) {
                if (ageText.isValidAge()) {
                    userPreferencesManager.setUserAge(ageText.toInt())
                } else {
                    etAge.setErrorAndFocus("Invalid age")
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
            userPreferencesManager.setActivityLevel(activityLevel)

            val units = if (spinnerUnits.selectedItemPosition == 0) "metric" else "imperial"
            userPreferencesManager.setPreferredUnits(units)

            userPreferencesManager.setNotificationEnabled(switchNotifications.isChecked)

            val waterInterval = if (switchWaterReminder.isChecked) {
                val intervalText = etWaterInterval.getTextString()
                if (intervalText.isNotEmpty()) {
                    intervalText.toIntOrNull() ?: 60
                } else {
                    60
                }
            } else {
                0
            }
            userPreferencesManager.setWaterReminderInterval(waterInterval)

            requireContext().showSuccessToast("Settings saved successfully!")
            goBack()

        } catch (e: Exception) {
            requireContext().showErrorToast("Error saving settings: ${e.message}")
        }
    }

    private fun getCurrentUnits(): String {
        return if (spinnerUnits.selectedItemPosition == 0) "metric" else "imperial"
    }

    private fun goBack() {
        try {
            findNavController().popBackStack(R.id.dashboardFragment, false)
        } catch (e: Exception) {
            try {
                findNavController().popBackStack()
            } catch (ex: Exception) {
                requireActivity().finish()
            }
        }
    }
}