package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrisense.R
import com.example.nutrisense.data.entity.Food
import com.example.nutrisense.adapters.FoodAdapter
import com.example.nutrisense.viewmodel.NutritionViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SearchHistoryFragment : Fragment() {
    private val nutritionViewModel: NutritionViewModel by viewModels()

    private lateinit var foodAdapter: FoodAdapter
    private lateinit var rvSavedFoods: RecyclerView
    private lateinit var tvNoSavedFoods: TextView
    private lateinit var tvNutritionSummary: TextView
    private lateinit var btnBackToDashboard: Button

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
        setupRecyclerView()
        observeViewModel()
        setupBackPressHandler()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        rvSavedFoods = view.findViewById(R.id.rv_saved_foods)
        tvNoSavedFoods = view.findViewById(R.id.tv_no_saved_foods)
        tvNutritionSummary = view.findViewById(R.id.tv_nutrition_summary)
        btnBackToDashboard = view.findViewById(R.id.btn_back_to_dashboard)
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
            goToDashboard()
        }
    }

    private fun setupClickListeners() {
        btnBackToDashboard.setOnClickListener {
            goToDashboard()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            nutritionViewModel.userFoods.collect { liveDataFoods ->
                liveDataFoods?.observe(viewLifecycleOwner) { foods ->
                    foodAdapter.submitList(foods)
                    tvNoSavedFoods.visibility = if (foods.isEmpty()) View.VISIBLE else View.GONE
                    rvSavedFoods.visibility = if (foods.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            nutritionViewModel.userTodayConsumedFoods.collect { liveDataFoods ->
                liveDataFoods?.observe(viewLifecycleOwner) { consumedFoods ->
                    updateNutritionSummary(consumedFoods)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            nutritionViewModel.uiState.collect { state ->
                state.successMessage?.let {
                    requireContext().showSuccessToast(it)
                    nutritionViewModel.clearMessages()
                }
                state.errorMessage?.let {
                    requireContext().showErrorToast(it)
                    nutritionViewModel.clearMessages()
                }
            }
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
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                appendLine("✅ Consumed: ${dateFormat.format(Date(food.consumedAt))}")
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
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun goToDashboard() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }
}