package com.example.nutrisense.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.nutrisense.R
import com.example.nutrisense.activities.MainActivity
import java.util.Calendar

/**
 * Helper class for managing notifications in NutriSense app.
 * Handles water reminders, meal reminders, and general notifications.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        // Channel IDs
        const val WATER_REMINDER_CHANNEL_ID = "water_reminder_channel"
        const val MEAL_REMINDER_CHANNEL_ID = "meal_reminder_channel"
        const val GENERAL_CHANNEL_ID = "general_channel"

        // Notification IDs
        const val WATER_REMINDER_NOTIFICATION_ID = 1001
        const val BREAKFAST_REMINDER_NOTIFICATION_ID = 1002
        const val LUNCH_REMINDER_NOTIFICATION_ID = 1003
        const val DINNER_REMINDER_NOTIFICATION_ID = 1004

        // Request codes for PendingIntents
        const val WATER_REMINDER_REQUEST_CODE = 2001
        const val BREAKFAST_REMINDER_REQUEST_CODE = 2002
        const val LUNCH_REMINDER_REQUEST_CODE = 2003
        const val DINNER_REMINDER_REQUEST_CODE = 2004
    }

    init {
        createNotificationChannels()
    }

    /**
     * Creates all notification channels required by the app.
     * Must be called before showing any notifications.
     */
    private fun createNotificationChannels() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Water Reminder Channel
        val waterChannel = NotificationChannel(
            WATER_REMINDER_CHANNEL_ID,
            "Water Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders to drink water throughout the day"
            enableVibration(true)
        }

        // Meal Reminder Channel
        val mealChannel = NotificationChannel(
            MEAL_REMINDER_CHANNEL_ID,
            "Meal Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for breakfast, lunch, and dinner"
            enableVibration(true)
        }

        // General Notifications Channel
        val generalChannel = NotificationChannel(
            GENERAL_CHANNEL_ID,
            "General Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "General app notifications"
        }

        notificationManager.createNotificationChannels(
            listOf(waterChannel, mealChannel, generalChannel)
        )
    }

    /**
     * Checks if the app has notification permission (required for Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Shows a water reminder notification
     */
    @SuppressLint("MissingPermission")
    fun showWaterReminderNotification() {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "dashboard")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, WATER_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("Don't forget to drink water. Stay hydrated for better health!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            WATER_REMINDER_NOTIFICATION_ID,
            notification
        )
    }

    /**
     * Shows a meal reminder notification
     */
    @SuppressLint("MissingPermission")
    fun showMealReminderNotification(mealType: MealType) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "calculate_nutrition")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, message, notificationId) = when (mealType) {
            MealType.BREAKFAST -> Triple(
                "🍳 Breakfast Time!",
                "Good morning! Don't skip breakfast - it's the most important meal!",
                BREAKFAST_REMINDER_NOTIFICATION_ID
            )
            MealType.LUNCH -> Triple(
                "🥗 Lunch Time!",
                "Time for a healthy lunch. Log your meal to track your nutrition!",
                LUNCH_REMINDER_NOTIFICATION_ID
            )
            MealType.DINNER -> Triple(
                "🍽️ Dinner Time!",
                "Time for dinner! Remember to eat a balanced meal.",
                DINNER_REMINDER_NOTIFICATION_ID
            )
        }

        val notification = NotificationCompat.Builder(context, MEAL_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Shows a general notification with custom title and message
     */
    @SuppressLint("MissingPermission")
    fun showGeneralNotification(title: String, message: String, notificationId: Int = 999) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Schedules water reminder notifications at regular intervals
     * @param intervalMinutes The interval between reminders in minutes
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleWaterReminders(intervalMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WATER_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Cancel any existing water reminders
        alarmManager.cancel(pendingIntent)

        // Schedule repeating alarm
        val intervalMillis = intervalMinutes * 60 * 1000L
        val triggerAtMillis = System.currentTimeMillis() + intervalMillis

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            intervalMillis,
            pendingIntent
        )
    }

    /**
     * Cancels all scheduled water reminders
     */
    fun cancelWaterReminders() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WATER_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * Schedules meal reminder at a specific time
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleMealReminder(mealType: MealType, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val requestCode = when (mealType) {
            MealType.BREAKFAST -> BREAKFAST_REMINDER_REQUEST_CODE
            MealType.LUNCH -> LUNCH_REMINDER_REQUEST_CODE
            MealType.DINNER -> DINNER_REMINDER_REQUEST_CODE
        }

        val intent = Intent(context, MealReminderReceiver::class.java).apply {
            putExtra("meal_type", mealType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Calculate next trigger time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Schedule daily repeating alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    /**
     * Cancels a meal reminder
     */
    fun cancelMealReminder(mealType: MealType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val requestCode = when (mealType) {
            MealType.BREAKFAST -> BREAKFAST_REMINDER_REQUEST_CODE
            MealType.LUNCH -> LUNCH_REMINDER_REQUEST_CODE
            MealType.DINNER -> DINNER_REMINDER_REQUEST_CODE
        }

        val intent = Intent(context, MealReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * Cancels all meal reminders
     */
    fun cancelAllMealReminders() {
        MealType.entries.forEach { cancelMealReminder(it) }
    }

    /**
     * Sets up default meal reminders
     * Breakfast: 8:00 AM, Lunch: 12:30 PM, Dinner: 7:00 PM
     */
    fun setupDefaultMealReminders() {
        scheduleMealReminder(MealType.BREAKFAST, 8, 0)
        scheduleMealReminder(MealType.LUNCH, 12, 30)
        scheduleMealReminder(MealType.DINNER, 19, 0)
    }
}

/**
 * Enum representing different meal types
 */
enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER
}
