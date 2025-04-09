package com.example.cleansync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA78BFA),  // Softer purple (600 in Tailwind)
    secondary = Color(0xFF38BDF8), // Friendly sky blue
    tertiary = Color(0xFF818CF8),  // Indigo-400
    background = Color(0xFF1E1E2E), // Dark but not pure black
    surface = Color(0xFF2D2D3D),   // Slightly lighter than background
    onPrimary = Color(0xFF1E1E1E), // Dark gray for text on primary
    onSecondary = Color(0xFF1E1E1E), // Dark gray for text on secondary
    onBackground = Color(0xFFE2E8F0), // Light grayish blue (easy on eyes)
    onSurface = Color(0xFFE2E8F0),    // Same as onBackground
    error = Color(0xFFF87171)       // Soft red for errors
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),   // Vibrant but not harsh purple
    secondary = Color(0xFF0EA5E9), // Friendly blue
    tertiary = Color(0xFF6366F1),  // Indigo-500
    background = Color(0xFFF8FAFC), // Very light gray background
    surface = Color.White,          // Pure white surfaces
    onPrimary = Color.White,        // White text on primary
    onSecondary = Color.White,      // White text on secondary
    onBackground = Color(0xFF1E293B), // Dark blue-gray for text
    onSurface = Color(0xFF1E293B),    // Same as onBackground
    error = Color(0xFFDC2626)       // Clear red for errors
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
        typography = Typography,
        content = content
    )
}