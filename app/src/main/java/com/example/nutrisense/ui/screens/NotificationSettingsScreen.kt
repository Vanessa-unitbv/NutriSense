package com.example.nutrisense.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.notifications.MealType
import com.example.nutrisense.notifications.NotificationHelper
import com.example.nutrisense.ui.components.*
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    onSaveClick: (NotificationSettingsData) -> Unit
) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    val prefsManager = remember { SharedPreferencesManager.getGlobalInstance(context) }

    var hasNotificationPermission by remember {
        mutableStateOf(notificationHelper.hasNotificationPermission())
    }

    var notificationsEnabled by remember { mutableStateOf(prefsManager.isNotificationEnabled()) }
    var waterReminderEnabled by remember { mutableStateOf(prefsManager.isWaterReminderEnabled()) }
    var mealRemindersEnabled by remember { mutableStateOf(prefsManager.isMealReminderEnabled()) }
    var waterInterval by remember { mutableStateOf(prefsManager.getWaterReminderInterval().toString()) }

    var breakfastTime by remember { mutableStateOf(prefsManager.getBreakfastTime()) }
    var lunchTime by remember { mutableStateOf(prefsManager.getLunchTime()) }
    var dinnerTime by remember { mutableStateOf(prefsManager.getDinnerTime()) }

    var showSuccessMessage by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            notificationsEnabled = true
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    hasNotificationPermission = true
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            hasNotificationPermission = true
        }
    }

    fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }

    NutriSenseTheme {
        Scaffold(
            topBar = {
                NutriSenseTopBar(
                    title = "🔔 Notification Settings",
                    onBackClick = onBackClick
                )
            },
            containerColor = NutriSenseColors.Background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showSuccessMessage) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Success.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "✅ Settings saved successfully!",
                            modifier = Modifier.padding(16.dp),
                            color = NutriSenseColors.Success,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (!hasNotificationPermission) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NutriSenseColors.Warning.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "⚠️ Permission Required",
                                fontWeight = FontWeight.Bold,
                                color = NutriSenseColors.Warning,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Grant notification permission to receive reminders.",
                                color = NutriSenseColors.TextSecondary,
                                fontSize = 14.sp
                            )
                            NutriSenseButton(
                                text = "Grant Permission",
                                onClick = { requestNotificationPermission() }
                            )
                        }
                    }
                }

                NutriSenseCard(title = "🔔 General") {
                    NutriSenseSwitchRow(
                        title = "Enable Notifications",
                        subtitle = "Master switch for all notifications",
                        checked = notificationsEnabled && hasNotificationPermission,
                        onCheckedChange = {
                            if (hasNotificationPermission) {
                                notificationsEnabled = it
                            } else {
                                requestNotificationPermission()
                            }
                        }
                    )
                }

                if (notificationsEnabled && hasNotificationPermission) {
                    NutriSenseCard(title = "💧 Water Reminders") {
                        NutriSenseSwitchRow(
                            title = "Water Reminders",
                            subtitle = "Get reminded to drink water",
                            checked = waterReminderEnabled,
                            onCheckedChange = { waterReminderEnabled = it }
                        )

                        if (waterReminderEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = waterInterval,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                        waterInterval = it
                                    }
                                },
                                label = { Text("Interval (minutes)", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                if (notificationsEnabled && hasNotificationPermission) {
                    NutriSenseCard(title = "🍽️ Meal Reminders") {
                        NutriSenseSwitchRow(
                            title = "Meal Reminders",
                            subtitle = "Breakfast, lunch & dinner reminders",
                            checked = mealRemindersEnabled,
                            onCheckedChange = { mealRemindersEnabled = it }
                        )

                        if (mealRemindersEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))

                            MealTimeField("🍳 Breakfast", breakfastTime) { breakfastTime = it }
                            Spacer(modifier = Modifier.height(8.dp))
                            MealTimeField("🥗 Lunch", lunchTime) { lunchTime = it }
                            Spacer(modifier = Modifier.height(8.dp))
                            MealTimeField("🍝 Dinner", dinnerTime) { dinnerTime = it }
                        }
                    }
                }

                if (notificationsEnabled && hasNotificationPermission) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NutriSenseOutlinedButton(
                            text = "🔔 Test",
                            onClick = {
                                notificationHelper.showGeneralNotification(
                                    "Test Notification",
                                    "Notifications are working! 🎉"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                NutriSenseButton(
                    text = "Save Settings",
                    onClick = {
                        val intervalMinutes = waterInterval.toIntOrNull() ?: 60

                        prefsManager.setNotificationEnabled(notificationsEnabled)
                        prefsManager.setWaterReminderEnabled(waterReminderEnabled)
                        prefsManager.setWaterReminderInterval(intervalMinutes)
                        prefsManager.setMealReminderEnabled(mealRemindersEnabled)
                        prefsManager.setBreakfastTime(breakfastTime)
                        prefsManager.setLunchTime(lunchTime)
                        prefsManager.setDinnerTime(dinnerTime)

                        if (notificationsEnabled && hasNotificationPermission) {
                            if (waterReminderEnabled) {
                                notificationHelper.scheduleWaterReminders(intervalMinutes)
                            } else {
                                notificationHelper.cancelWaterReminders()
                            }

                            if (mealRemindersEnabled) {
                                val (breakfastHour, breakfastMinute) = parseTime(breakfastTime)
                                val (lunchHour, lunchMinute) = parseTime(lunchTime)
                                val (dinnerHour, dinnerMinute) = parseTime(dinnerTime)

                                notificationHelper.scheduleMealReminder(MealType.BREAKFAST, breakfastHour, breakfastMinute)
                                notificationHelper.scheduleMealReminder(MealType.LUNCH, lunchHour, lunchMinute)
                                notificationHelper.scheduleMealReminder(MealType.DINNER, dinnerHour, dinnerMinute)
                            } else {
                                notificationHelper.cancelAllMealReminders()
                            }
                        } else {
                            notificationHelper.cancelWaterReminders()
                            notificationHelper.cancelAllMealReminders()
                        }

                        showSuccessMessage = true

                        onSaveClick(
                            NotificationSettingsData(
                                notificationsEnabled = notificationsEnabled,
                                waterReminderEnabled = waterReminderEnabled,
                                mealRemindersEnabled = mealRemindersEnabled,
                                waterIntervalMinutes = intervalMinutes,
                                breakfastTime = breakfastTime,
                                lunchTime = lunchTime,
                                dinnerTime = dinnerTime
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MealTimeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Allow only valid time format characters
                val filtered = newValue.filter { it.isDigit() || it == ':' }
                if (filtered.length <= 5) {
                    onValueChange(filtered)
                }
            },
            modifier = Modifier.width(120.dp),
            placeholder = { Text("HH:MM", color = Color.White.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

data class NotificationSettingsData(
    val notificationsEnabled: Boolean,
    val waterReminderEnabled: Boolean,
    val mealRemindersEnabled: Boolean,
    val waterIntervalMinutes: Int,
    val breakfastTime: String,
    val lunchTime: String,
    val dinnerTime: String
)
