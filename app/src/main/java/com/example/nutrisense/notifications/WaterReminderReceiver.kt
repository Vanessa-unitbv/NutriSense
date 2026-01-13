package com.example.nutrisense.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver for water reminder notifications.
 * Triggered by AlarmManager at scheduled intervals.
 */
class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showWaterReminderNotification()

        // Reschedule next water reminder (since we use setExactAndAllowWhileIdle instead of setRepeating)
        val intervalMinutes = intent.getIntExtra("interval_minutes", 60)
        notificationHelper.scheduleWaterReminders(intervalMinutes)
    }
}
