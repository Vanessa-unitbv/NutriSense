package com.example.nutrisense.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_plans",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId"), Index("userId")]
)
data class MealPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val recipeId: Long,
    val dayOfWeek: Int, // 0 = Monday, 1 = Tuesday, ..., 6 = Sunday
    val mealType: String, // "breakfast", "lunch", "dinner", "snack"
    val createdAt: Long = System.currentTimeMillis()
)

enum class DayOfWeek(val value: Int, val displayName: String) {
    MONDAY(0, "Monday"),
    TUESDAY(1, "Tuesday"),
    WEDNESDAY(2, "Wednesday"),
    THURSDAY(3, "Thursday"),
    FRIDAY(4, "Friday"),
    SATURDAY(5, "Saturday"),
    SUNDAY(6, "Sunday");

    companion object {
        fun fromValue(value: Int): DayOfWeek = values().first { it.value == value }
    }
}

enum class MealType(val value: String, val displayName: String, val emoji: String) {
    BREAKFAST("breakfast", "Breakfast", "🌅"),
    LUNCH("lunch", "Lunch", "☀️"),
    DINNER("dinner", "Dinner", "🌙"),
    SNACK("snack", "Snack", "🍎");

    companion object {
        fun fromValue(value: String): MealType = values().first { it.value == value }
    }
}

data class MealPlanWithRecipe(
    val mealPlan: MealPlan,
    val recipe: Recipe
)

