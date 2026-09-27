package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),           // Glowing Teal-400
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF115E59),  // Deep Teal-800
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFF38BDF8),         // Sky-400
    onSecondary = Color(0xFF0C4A6E),
    background = DarkSurface,
    surface = DarkCard,
    surfaceVariant = Color(0xFF1E293B),
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = ExpenseRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBrand,
    onPrimary = Color.White,
    primaryContainer = PrimaryBrandLight,
    onPrimaryContainer = Color(0xFF042F2E),
    secondary = DarkNavy,
    onSecondary = Color.White,
    background = LightBg,
    surface = CardWhite,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = ExpenseRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
