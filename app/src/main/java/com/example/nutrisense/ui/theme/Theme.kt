package com.example.nutrisense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

// Culori NutriSense - tema maro/portocaliu
object NutriSenseColors {
    // Primary Colors
    val Brown = Color(0xFF713B27)
    val BrownLight = Color(0xFF8C5A45)
    val BrownDark = Color(0xFF4A2518)

    // Secondary Colors
    val Orange = Color(0xFFD89555)
    val OrangeLight = Color(0xFFE8B078)
    val OrangeDark = Color(0xFFB87A3D)

    // Background Colors
    val Background = Color(0xFFF5E6D3)
    val BackgroundDark = Color(0xFF2C1810)
    val Surface = Color(0xFFFFF8F0)
    val SurfaceDark = Color(0xFF3D2518)

    // Card Colors
    val CardBrown = Color(0xA0713B27)
    val CardOrange = Color(0xFFD89555)

    // Text Colors
    val TextPrimary = Color(0xFF713B27)
    val TextSecondary = Color(0xFF8C7570)
    val TextOnPrimary = Color.White

    // Status Colors
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFC62828)
    val Warning = Color(0xFFFF9800)

    // Nutrition Colors
    val CaloriesColor = Color(0xFFFF6B6B)
    val ProteinColor = Color(0xFF4ECDC4)
    val CarbsColor = Color(0xFFFFA500)
    val FatColor = Color(0xFF95E1D3)
    val FiberColor = Color(0xFFB19CD9)
    val SugarColor = Color(0xFFFDD835)
    val WaterColor = Color(0xFF4ECDC4)
}

private val LightColorScheme = lightColorScheme(
    primary = NutriSenseColors.Brown,
    onPrimary = Color.White,
    primaryContainer = NutriSenseColors.BrownLight,
    onPrimaryContainer = Color.White,
    secondary = NutriSenseColors.Orange,
    onSecondary = Color.White,
    secondaryContainer = NutriSenseColors.OrangeLight,
    onSecondaryContainer = NutriSenseColors.BrownDark,
    tertiary = NutriSenseColors.OrangeDark,
    onTertiary = Color.White,
    error = NutriSenseColors.Error,
    onError = Color.White,
    background = NutriSenseColors.Background,
    onBackground = NutriSenseColors.TextPrimary,
    surface = NutriSenseColors.Surface,
    onSurface = NutriSenseColors.TextPrimary,
    surfaceVariant = NutriSenseColors.Background,
    onSurfaceVariant = NutriSenseColors.TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = NutriSenseColors.OrangeLight,
    onPrimary = NutriSenseColors.BrownDark,
    primaryContainer = NutriSenseColors.Brown,
    onPrimaryContainer = Color.White,
    secondary = NutriSenseColors.Orange,
    onSecondary = NutriSenseColors.BrownDark,
    secondaryContainer = NutriSenseColors.OrangeDark,
    onSecondaryContainer = Color.White,
    tertiary = NutriSenseColors.OrangeLight,
    onTertiary = NutriSenseColors.BrownDark,
    error = Color(0xFFFF6B6B),
    onError = Color.White,
    background = NutriSenseColors.BackgroundDark,
    onBackground = Color.White,
    surface = NutriSenseColors.SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = NutriSenseColors.BackgroundDark,
    onSurfaceVariant = Color(0xFFCCBBAA)
)

@Composable
fun NutriSenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
