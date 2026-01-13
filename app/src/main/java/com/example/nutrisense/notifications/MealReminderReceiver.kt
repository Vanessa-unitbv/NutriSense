package com.example.nutrisense.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver for meal reminder notifications.
 * Triggered by AlarmManager at scheduled times for breakfast, lunch, and dinner.
 */
class MealReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val mealTypeName = intent.getStringExtra("meal_type") ?: return

        val mealType = try {
            MealType.valueOf(mealTypeName)
        } catch (e: IllegalArgumentException) {
            return
        }

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showMealReminderNotification(mealType)

        // Reschedule for tomorrow (since we use setExactAndAllowWhileIdle instead of setRepeating)
        val hour = intent.getIntExtra("hour", getDefaultHour(mealType))
        val minute = intent.getIntExtra("minute", getDefaultMinute(mealType))
        notificationHelper.scheduleMealReminder(mealType, hour, minute)
    }

    private fun getDefaultHour(mealType: MealType): Int = when (mealType) {
        MealType.BREAKFAST -> 8
        MealType.LUNCH -> 12
        MealType.DINNER -> 19
    }

    private fun getDefaultMinute(mealType: MealType): Int = when (mealType) {
        MealType.BREAKFAST -> 0
        MealType.LUNCH -> 30
        MealType.DINNER -> 0
    }
}
