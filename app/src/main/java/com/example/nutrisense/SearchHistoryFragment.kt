package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.ui.adapter.FoodAdapter
import com.example.nutrisense.viewmodel.NutritionViewModel

class SearchHistoryFragment : Fragment() {

    private lateinit var nutritionViewModel: NutritionViewModel
    private lateinit var foodAdapter: FoodAdapter

    private lateinit var rvSavedFoods: RecyclerView
    private lateinit var tvNoSavedFoods: TextView
    private lateinit var tvNutritionSummary: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        setupBackPressHandler()
    }

    private fun initializeViews(view: View) {
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
        nutritionViewModel.userFoods.observe(viewLifecycleOwner) { liveDataFoods ->
            liveDataFoods?.observe(viewLifecycleOwner) { foods ->
                foodAdapter.submitList(foods)
                tvNoSavedFoods.visibility = if (foods.isEmpty()) View.VISIBLE else View.GONE
                rvSavedFoods.visibility = if (foods.isEmpty()) View.GONE else View.VISIBLE

                updateFoodSummary(foods)
            }
        }

        nutritionViewModel.userTodayConsumedFoods.observe(viewLifecycleOwner) { liveDataFoods ->
            liveDataFoods?.observe(viewLifecycleOwner) { consumedFoods ->
                updateNutritionSummary(consumedFoods)
            }
        }
    }

    private fun updateFoodSummary(foods: List<Food>) {
        if (foods.isEmpty()) return

        val totalFoods = foods.size
        val favoriteFoods = foods.count { it.isFavorite }
        val consumedToday = foods.count { food ->
            food.consumedAt != null && isToday(food.consumedAt)
        }

        val summaryText = buildString {
            appendLine("📊 Your Food Database:")
            appendLine("🍽️ Total saved foods: $totalFoods")
            appendLine("⭐ Favorite foods: $favoriteFoods")
            appendLine("✅ Consumed today: $consumedToday")
        }
    }

    private fun updateNutritionSummary(consumedFoods: List<Food>) {
        if (consumedFoods.isEmpty()) {
            tvNutritionSummary.text = "No foods consumed today"
            return
        }

        val totalCalories = consumedFoods.sumOf { it.calories }
        val totalProtein = consumedFoods.sumOf { it.proteinG }
        val totalCarbs = consumedFoods.sumOf { it.carbohydratesTotalG }
        val totalFat = consumedFoods.sumOf { it.fatTotalG }

        val summaryText = buildString {
            appendLine("📊 Today's Consumption Summary:")
            appendLine("🔥 Total Calories: ${totalCalories.toInt()} kcal")
            appendLine("🥩 Protein: ${String.format("%.1f", totalProtein)}g")
            appendLine("🍞 Carbs: ${String.format("%.1f", totalCarbs)}g")
            appendLine("🥑 Fat: ${String.format("%.1f", totalFat)}g")
            appendLine("🍽️ Foods consumed: ${consumedFoods.size}")
        }

        tvNutritionSummary.text = summaryText
    }

    private fun isToday(timestamp: Long): Boolean {
        val today = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return (today - timestamp) < oneDayInMillis
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