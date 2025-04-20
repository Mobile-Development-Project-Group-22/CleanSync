package com.example.cleansync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Mode Palette
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007BFF), // Vibrant Blue
    secondary = Color(0xFFFF9900), // Muted Orange
    background = Color(0xFFFFFFFF), // White
    surface = Color(0xFFF7F7F7), // Light Gray
    onPrimary = Color(0xFFFFFFFF), // White text on primary
    onSecondary = Color(0xFFFFFFFF), // White text on secondary
    onBackground = Color(0xFF333333), // Dark Text
    onSurface = Color(0xFF333333), // Dark Text on surface
    // Add more colors if needed
)

// Dark Mode Palette
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4A90E2), // Bright Blue
    secondary = Color(0xFFFF6F00), // Muted Orange
    background = Color(0xFF121212), // Dark Gray Background
    surface = Color(0xFF1C1C1C), // Darker Gray
    onPrimary = Color(0xFFFFFFFF), // White text on primary
    onSecondary = Color(0xFFFFFFFF), // White text on secondary
    onBackground = Color(0xFFE0E0E0), // Light Gray Text
    onSurface = Color(0xFFE0E0E0), // Light Gray Text on surface
    // Add more colors if needed
)

@Composable
fun CleanSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography is defined elsewhere
        content = content
    )
}
