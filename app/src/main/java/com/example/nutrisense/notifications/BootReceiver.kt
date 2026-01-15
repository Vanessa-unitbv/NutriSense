package com.example.nutrisense.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nutrisense.managers.SharedPreferencesManager

/**
 * BroadcastReceiver that restores scheduled notifications after device reboot.
 * This is necessary because AlarmManager alarms are cleared when the device restarts.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = SharedPreferencesManager.getGlobalInstance(context)
            val notificationHelper = NotificationHelper(context)

            if (prefs.isNotificationEnabled()) {
                if (prefs.isWaterReminderEnabled()) {
                    val waterInterval = prefs.getWaterReminderInterval()
                    notificationHelper.scheduleWaterReminders(waterInterval)
                }

                if (prefs.isMealReminderEnabled()) {
                    val breakfastTime = prefs.getBreakfastTime()
                    val lunchTime = prefs.getLunchTime()
                    val dinnerTime = prefs.getDinnerTime()

                    val (breakfastHour, breakfastMinute) = parseTime(breakfastTime)
                    val (lunchHour, lunchMinute) = parseTime(lunchTime)
                    val (dinnerHour, dinnerMinute) = parseTime(dinnerTime)

                    notificationHelper.scheduleMealReminder(MealType.BREAKFAST, breakfastHour, breakfastMinute)
                    notificationHelper.scheduleMealReminder(MealType.LUNCH, lunchHour, lunchMinute)
                    notificationHelper.scheduleMealReminder(MealType.DINNER, dinnerHour, dinnerMinute)
                }
            }
        }
    }

    private fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }
}
