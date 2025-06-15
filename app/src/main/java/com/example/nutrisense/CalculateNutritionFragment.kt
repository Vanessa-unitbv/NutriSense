package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.ui.adapter.FoodAdapter
import com.example.nutrisense.viewmodel.NutritionViewModel

class CalculateNutritionFragment : Fragment() {

    private lateinit var nutritionViewModel: NutritionViewModel
    private lateinit var foodAdapter: FoodAdapter

    private lateinit var etFoodName: EditText
    private lateinit var etQuantity: EditText
    private lateinit var btnCalculateNutrition: Button

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

    private lateinit var btnMarkConsumed: Button

    private lateinit var rvSavedFoods: RecyclerView
    private lateinit var tvNoSavedFoods: TextView
    private lateinit var tvNutritionSummary: TextView

    private var currentFoodResult: Food? = null

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
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        setupBackPressHandler()
    }

    private fun initializeViews(view: View) {
        etFoodName = view.findViewById(R.id.et_food_name)
        etQuantity = view.findViewById(R.id.et_quantity)
        btnCalculateNutrition = view.findViewById(R.id.btn_calculate_nutrition)

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

        btnMarkConsumed = view.findViewById(R.id.btn_mark_consumed)

        rvSavedFoods = view.findViewById(R.id.rv_saved_foods)
        tvNoSavedFoods = view.findViewById(R.id.tv_no_saved_foods)
        tvNutritionSummary = view.findViewById(R.id.tv_nutrition_summary)
    }

    private fun setupViewModel() {
        nutritionViewModel = ViewModelProvider(this)[NutritionViewModel::class.java]
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            onItemClick = { food -> showFoodDetails(food) },
            onFavoriteClick = { food -> nutritionViewModel.updateFavoriteStatus(food) },
            onDeleteClick = { food -> showDeleteConfirmation(food) },
            onConsumeClick = { food -> nutritionViewModel.markFoodAsConsumed(food) }
        )

        rvSavedFoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = foodAdapter
        }
    }

    private fun setupClickListeners() {
        btnCalculateNutrition.setOnClickListener {
            calculateNutrition()
        }

        btnMarkConsumed.setOnClickListener {
            markCurrentFoodAsConsumed()
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        nutritionViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnCalculateNutrition.isEnabled = !isLoading
        }

        nutritionViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                tvErrorMessage.text = errorMessage
                tvErrorMessage.visibility = View.VISIBLE
                llNutritionResults.visibility = View.GONE
            } else {
                tvErrorMessage.visibility = View.GONE
            }
        }

        nutritionViewModel.searchResults.observe(viewLifecycleOwner) { searchResults ->
            if (searchResults.isNotEmpty()) {
                currentFoodResult = searchResults.first()
                displayNutritionResults(currentFoodResult!!)

                llNutritionResults.visibility = View.VISIBLE
                btnMarkConsumed.visibility = View.VISIBLE

                showToast("Food automatically saved to your database!")
            }
        }

        nutritionViewModel.userFoods.observe(viewLifecycleOwner) { liveDataFoods ->
            liveDataFoods?.observe(viewLifecycleOwner) { foods ->
                foodAdapter.submitList(foods)
                tvNoSavedFoods.visibility = if (foods.isEmpty()) View.VISIBLE else View.GONE
                rvSavedFoods.visibility = if (foods.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        nutritionViewModel.nutritionSummary.observe(viewLifecycleOwner) { summary ->
            val summaryText = buildString {
                appendLine("📊 Today's Nutrition Summary:")
                appendLine("🔥 Calories: ${summary.totalCalories.toInt()} kcal")
                appendLine("🥩 Protein: ${String.format("%.1f", summary.totalProtein)}g")
                appendLine("🍞 Carbs: ${String.format("%.1f", summary.totalCarbs)}g")
                appendLine("🥑 Fat: ${String.format("%.1f", summary.totalFat)}g")
            }
            tvNutritionSummary.text = summaryText
        }

        nutritionViewModel.recentSearches.observe(viewLifecycleOwner) { searches ->
            if (searches.isNotEmpty() && etFoodName.text.isEmpty()) {
                etFoodName.hint = "e.g., ${searches.first()}"
            }
        }
    }

    private fun calculateNutrition() {
        val foodName = etFoodName.text.toString().trim()
        val quantityText = etQuantity.text.toString().trim()

        if (foodName.isEmpty()) {
            showToast("Please enter a food name")
            return
        }

        if (quantityText.isEmpty()) {
            showToast("Please enter quantity in grams")
            return
        }

        val quantity = quantityText.toDoubleOrNull()
        if (quantity == null || quantity <= 0) {
            showToast("Please enter a valid quantity")
            return
        }

        llNutritionResults.visibility = View.GONE
        btnMarkConsumed.visibility = View.GONE
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

    private fun markCurrentFoodAsConsumed() {
        currentFoodResult?.let { food ->
            nutritionViewModel.markFoodAsConsumed(food)
            showToast("Food marked as consumed today!")
            clearForm()
        }
    }

    private fun clearForm() {
        etFoodName.text.clear()
        etQuantity.text.clear()
        llNutritionResults.visibility = View.GONE
        btnMarkConsumed.visibility = View.GONE
        currentFoodResult = null
    }

    private fun showFoodDetails(food: Food) {
        val details = buildString {
            appendLine("📊 ${food.name.uppercase()}")
            appendLine("🔍 Original search: ${food.originalQuery}")
            appendLine("⚖️ Requested quantity: ${food.requestedQuantityG.toInt()}g")
            appendLine("🔥 Calories: ${String.format("%.1f", food.calories)} kcal")
            appendLine()
            appendLine("MACRONUTRIENTS:")
            appendLine("🥩 Protein: ${String.format("%.1f", food.proteinG)}g")
            appendLine("🍞 Carbs: ${String.format("%.1f", food.carbohydratesTotalG)}g")
            appendLine("🥑 Fat: ${String.format("%.1f", food.fatTotalG)}g")
            appendLine("   - Saturated: ${String.format("%.1f", food.fatSaturatedG)}g")
            appendLine()
            appendLine("OTHER NUTRIENTS:")
            appendLine("🧂 Sodium: ${String.format("%.1f", food.sodiumMg)}mg")
            appendLine("🍌 Potassium: ${String.format("%.1f", food.potassiumMg)}mg")
            appendLine("🧡 Cholesterol: ${String.format("%.1f", food.cholesterolMg)}mg")
            appendLine("🌾 Fiber: ${String.format("%.1f", food.fiberG)}g")
            appendLine("🍯 Sugar: ${String.format("%.1f", food.sugarG)}g")
            appendLine()
            if (food.consumedAt != null) {
                appendLine("✅ Consumed: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(food.consumedAt))}")
            } else {
                appendLine("⏳ Not consumed yet")
            }
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Nutrition Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteConfirmation(food: Food) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Food")
            .setMessage("Are you sure you want to delete ${food.name} from your database?")
            .setPositiveButton("Delete") { _, _ ->
                nutritionViewModel.deleteFood(food)
                showToast("${food.name} deleted from your database")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}