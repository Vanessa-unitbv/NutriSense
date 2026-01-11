package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.R
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.viewmodel.NutritionViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalculateNutritionFragment : Fragment() {
    private val nutritionViewModel: NutritionViewModel by viewModels()

    private lateinit var etFoodName: EditText
    private lateinit var etQuantity: EditText
    private lateinit var btnCalculateNutrition: Button
    private lateinit var btnBackToDashboard: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    private lateinit var llNutritionResults: LinearLayout
    private lateinit var tvCalories: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvFatTotal: TextView
    private lateinit var tvFatSaturated: TextView
    private lateinit var tvSodium: TextView
    private lateinit var tvPotassium: TextView
    private lateinit var tvCholesterol: TextView
    private lateinit var tvFiber: TextView
    private lateinit var tvSugar: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calculate_nutrition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        observeViewModel()
        setupBackPressHandler()
    }

    private fun initializeViews(view: View) {
        etFoodName = view.findViewById(R.id.et_food_name)
        etQuantity = view.findViewById(R.id.et_quantity)
        btnCalculateNutrition = view.findViewById(R.id.btn_calculate_nutrition)
        btnBackToDashboard = view.findViewById(R.id.btn_back_to_dashboard)
        progressBar = view.findViewById(R.id.progress_bar)
        tvErrorMessage = view.findViewById(R.id.tv_error_message)
        llNutritionResults = view.findViewById(R.id.ll_nutrition_results)
        tvCalories = view.findViewById(R.id.tv_calories)
        tvProtein = view.findViewById(R.id.tv_protein)
        tvCarbs = view.findViewById(R.id.tv_carbs)
        tvFatTotal = view.findViewById(R.id.tv_fat_total)
        tvFatSaturated = view.findViewById(R.id.tv_fat_saturated)
        tvSodium = view.findViewById(R.id.tv_sodium)
        tvPotassium = view.findViewById(R.id.tv_potassium)
        tvCholesterol = view.findViewById(R.id.tv_cholesterol)
        tvFiber = view.findViewById(R.id.tv_fiber)
        tvSugar = view.findViewById(R.id.tv_sugar)
    }

    private fun setupClickListeners() {
        btnCalculateNutrition.setOnClickListener {
            calculateNutrition()
        }

        btnBackToDashboard.setOnClickListener {
            goToDashboard()
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goToDashboard()
        }
    }


    private fun calculateNutrition() {
        val foodName = etFoodName.getTextString()
        val quantityText = etQuantity.getTextString()

        etFoodName.clearErrorAndFocus()
        etQuantity.clearErrorAndFocus()

        if (foodName.isEmpty()) {
            etFoodName.setErrorAndFocus("Please enter a food name")
            return
        }

        if (quantityText.isEmpty()) {
            etQuantity.setErrorAndFocus("Please enter quantity")
            return
        }

        val quantity = quantityText.toDoubleOrNull()
        if (quantity == null) {
            etQuantity.setErrorAndFocus("Please enter a valid number")
            return
        }

        llNutritionResults.hide()

        nutritionViewModel.searchFoodNutrition(foodName, quantity)
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            nutritionViewModel.uiState.collect { state ->
                // Loading state
                if (state.isLoading) {
                    progressBar.show()
                    btnCalculateNutrition.isEnabled = false
                } else {
                    progressBar.hide()
                    btnCalculateNutrition.isEnabled = true
                }

                // Error message
                if (state.errorMessage != null) {
                    tvErrorMessage.text = state.errorMessage
                    tvErrorMessage.show()
                    llNutritionResults.hide()
                    nutritionViewModel.clearMessages()
                } else {
                    tvErrorMessage.hide()
                }

                // Search results
                if (state.searchResults.isNotEmpty()) {
                    val food = state.searchResults.first()
                    displayNutritionResults(food)
                    llNutritionResults.show()
                }

                // Success message
                state.successMessage?.let { message ->
                    requireContext().showSuccessToast(message)
                    nutritionViewModel.clearMessages()
                }
            }
        }
    }

    private fun displayNutritionResults(food: Food) {
        tvCalories.text = "🔥 Calories: ${food.calories.toInt()} kcal"
        tvProtein.text = "🥩 Protein: ${String.format("%.1f", food.proteinG)} g"
        tvCarbs.text = "🍞 Carbohydrates: ${String.format("%.1f", food.carbohydratesTotalG)} g"
        tvFatTotal.text = "🥑 Total Fat: ${String.format("%.1f", food.fatTotalG)} g"
        tvFatSaturated.text = "   - Saturated Fat: ${String.format("%.1f", food.fatSaturatedG)} g"
        tvSodium.text = "🧂 Sodium: ${String.format("%.1f", food.sodiumMg)} mg"
        tvPotassium.text = "🍌 Potassium: ${String.format("%.1f", food.potassiumMg)} mg"
        tvCholesterol.text = "🧡 Cholesterol: ${String.format("%.1f", food.cholesterolMg)} mg"
        tvFiber.text = "🌾 Fiber: ${String.format("%.1f", food.fiberG)} g"
        tvSugar.text = "🍯 Sugar: ${String.format("%.1f", food.sugarG)} g"
    }

    private fun goToDashboard() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }
}