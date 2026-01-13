package com.example.nutrisense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrisense.ui.components.*
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class ProfileState(
    val email: String = "",
    val userName: String = "",
    val calorieGoal: Int = 2000,
    val waterGoal: Int = 2000,
    val weight: Float = 0f,
    val bmi: String = "",
    val lastUpdate: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenCompose(
    state: ProfileState,
    onSettingsClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    NutriSenseTheme {
        Scaffold(
            topBar = {
                NutriSenseTopBar(
                    title = "👤 My Profile",
                    onBackClick = onDashboardClick,
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Avatar
                Card(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = NutriSenseColors.CardOrange),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.userName.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = state.userName.ifBlank { "User" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NutriSenseColors.Brown
                )

                Text(
                    text = state.email,
                    fontSize = 14.sp,
                    color = NutriSenseColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats Cards
                NutriSenseCard(title = "📊 My Goals") {
                    ProfileStatRow(
                        icon = "🔥",
                        label = "Daily Calorie Goal",
                        value = "${state.calorieGoal} kcal"
                    )
                    ProfileStatRow(
                        icon = "💧",
                        label = "Daily Water Goal",
                        value = "${state.waterGoal} ml"
                    )
                }

                NutriSenseCard(title = "⚖️ Body Stats") {
                    ProfileStatRow(
                        icon = "⚖️",
                        label = "Weight",
                        value = if (state.weight > 0) "${state.weight} kg" else "Not set"
                    )
                    ProfileStatRow(
                        icon = "📏",
                        label = "BMI",
                        value = state.bmi.ifBlank { "Not calculated" }
                    )
                    if (state.lastUpdate.isNotBlank()) {
                        ProfileStatRow(
                            icon = "📅",
                            label = "Last Update",
                            value = state.lastUpdate
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                NutriSenseButton(
                    text = "⚙️ Settings",
                    onClick = onSettingsClick
                )

                NutriSenseOutlinedButton(
                    text = "← Dashboard",
                    onClick = onDashboardClick
                )

                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NutriSenseColors.Error
                    )
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileStatRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp
            )
        }
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

