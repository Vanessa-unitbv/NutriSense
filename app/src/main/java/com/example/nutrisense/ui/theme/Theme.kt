package com.example.nutrisense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Culori definite pentru tema aplicației
private val PrimaryColor = Color(0xFF6200EE)
private val PrimaryVariant = Color(0xFF3700B3)
private val SecondaryColor = Color(0xFF03DAC6)
private val SecondaryVariant = Color(0xFF018786)
private val BackgroundColor = Color(0xFFF5F5F5)
private val SurfaceColor = Color.White
private val ErrorColor = Color(0xFFB00020)

// Color scheme pentru tema light
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = SecondaryColor,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    error = ErrorColor,
    onError = Color.White,
    background = BackgroundColor,
    onBackground = Color.Black,
    surface = SurfaceColor,
    onSurface = Color.Black
)

@Composable
fun NutriSenseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

// Culori suplimentare pentru carduri
object NutritionColors {
    val CaloriesColor = Color(0xFFFF6B6B)
    val ProteinColor = Color(0xFF4ECDC4)
    val CarbsColor = Color(0xFFFFA500)
    val FatColor = Color(0xFF95E1D3)
    val FiberColor = Color(0xFFB19CD9)
    val SugarColor = Color(0xFFFDD835)
    val WaterColor = Color(0xFF4ECDC4)
}

