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

            // Restore water reminders if enabled
            if (prefs.isNotificationEnabled()) {
                val waterInterval = prefs.getWaterReminderInterval()
                notificationHelper.scheduleWaterReminders(waterInterval)

                // Restore meal reminders if enabled
                if (prefs.isMealReminderEnabled()) {
                    notificationHelper.setupDefaultMealReminders()
                }
            }
        }
    }
}
