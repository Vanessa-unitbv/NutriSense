package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.R
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.viewmodel.NutritionViewModel
import com.example.nutrisense.helpers.extensions.*

class CalculateNutritionFragment : Fragment() {

    private lateinit var nutritionViewModel: NutritionViewModel
    private lateinit var preferencesManager: SharedPreferencesManager

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
        setupViewModel()
        setupClickListeners()
        observeViewModel()
        setupBackPressHandler()
    }

    private fun initializeViews(view: View) {
        preferencesManager = SharedPreferencesManager.getInstance(requireContext())

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

    private fun setupViewModel() {
        nutritionViewModel = ViewModelProvider(this)[NutritionViewModel::class.java]
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
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
                requireActivity().finish()
            }
        }
    }

    private fun observeViewModel() {
        nutritionViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar.show()
            } else {
                progressBar.hide()
            }
            btnCalculateNutrition.isEnabled = !isLoading
        }

        nutritionViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                tvErrorMessage.text = errorMessage
                tvErrorMessage.show()
                llNutritionResults.hide()
            } else {
                tvErrorMessage.hide()
            }
        }

        nutritionViewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            if (searchResults.isNotEmpty()) {
                val food = searchResults.first()
                displayNutritionResults(food)

                llNutritionResults.show()

                requireContext().showSuccessToast("Food automatically saved to your database! Check 'Search History' to see all saved foods.")
            }
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
            etQuantity.setErrorAndFocus("Please enter quantity in grams")
            return
        }

        if (!quantityText.isValidQuantity()) {
            etQuantity.setErrorAndFocus("Please enter a valid quantity")
            return
        }

        val quantity = quantityText.toDouble()

        llNutritionResults.hide()
        nutritionViewModel.clearMessages()

        nutritionViewModel.searchFoodNutrition(foodName, quantity)
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

    private fun clearForm() {
        etFoodName.text.clear()
        etQuantity.text.clear()
        llNutritionResults.hide()
    }

    private fun goToDashboard() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }
}