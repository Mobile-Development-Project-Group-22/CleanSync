package com.example.cleansync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA78BFA),
    secondary = Color(0xFF38BDF8),
    tertiary = Color(0xFF818CF8),
    background = Color(0xFF1E1E2E),
    surface = Color(0xFF2D2D3D),
    onPrimary = Color(0xFF1E1E1E),
    onSecondary = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    error = Color(0xFFF87171),
    onError = Color.Black,
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF1E1E2E),
    outline = Color(0xFF94A3B8) // Slate-400
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),         // Indigo-500
    onPrimary = Color.White,

    secondary = Color(0xFF0EA5E9),       // Sky-500
    onSecondary = Color.White,

    tertiary = Color(0xFF7C3AED),        // Violet-600
    onTertiary = Color.White,

    background = Color(0xFFFAFAFF),      // Cool light gray-blue
    onBackground = Color(0xFF1E293B),    // Slate-800

    surface = Color.White,
    onSurface = Color(0xFF334155),       // Slate-700

    error = Color(0xFFDC2626),           // Red-600
    onError = Color.White,

    inverseSurface = Color(0xFF1E293B),  // Dark surface for dialogs/snacks
    inverseOnSurface = Color(0xFFF8FAFC),// Light text on dark surface
    outline = Color(0xFFD1D5DB)          // Light gray outline
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
