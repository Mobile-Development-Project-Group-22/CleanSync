package com.example.cleansync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// DeepSeek Light Mode Palette
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF),       // Vibrant DeepSeek Blue
    onPrimary = Color(0xFFFFFFFF),     // White text on primary
    primaryContainer = Color(0xFF5AC8FA), // Light Blue (gradient accent)
    onPrimaryContainer = Color(0xFF000000),

    secondary = Color(0xFF00D1D1),     // DeepSeek Cyan (accent)
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFFFFFFFF),    // White background
    onBackground = Color(0xFF1A1A1A), // Dark text

    surface = Color(0xFFF5F5F5),       // Light gray surface
    onSurface = Color(0xFF333333),     // Dark text on surface

    // Optional (adjust as needed)
    tertiary = Color(0xFF0040DD),      // Dark Blue (gradient part)
)

// DeepSeek Dark Mode Palette
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5AC8FA),       // Light Blue (gradient start)
    onPrimary = Color(0xFF000000),     // Black text on primary
    primaryContainer = Color(0xFF0040DD), // Dark Blue (gradient end)
    onPrimaryContainer = Color(0xFFFFFFFF),

    secondary = Color(0xFF00D1D1),     // Cyan (accent)
    onSecondary = Color(0xFF000000),

    background = Color(0xFF121212),    // Dark background
    onBackground = Color(0xFFE0E0E0),  // Light text

    surface = Color(0xFF1C1C1C),       // Dark surface
    onSurface = Color(0xFFE0E0E0),     // Light text on surface

    // Optional (adjust as needed)
    tertiary = Color(0xFF007AFF),      // Vibrant Blue
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
        typography = Typography, // Define your Typography elsewhere
        content = content
    )
}