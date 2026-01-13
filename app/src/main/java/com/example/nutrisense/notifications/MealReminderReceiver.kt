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
    }
}

