package com.example.cleansync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your custom colors
val GreenColor = Color(0xFF60C580)
val LightBlueColor = Color(0xFF5FADCF)
val PaleBlueColor = Color(0xFF82BDDB)

private val LightColorScheme = lightColorScheme(
    primary = GreenColor,  // Set your custom green as the primary color
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0DEFF),
    onPrimaryContainer = Color(0xFF1E1B4B),

    secondary = LightBlueColor,  // Set your custom light blue as secondary
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1A),

    surface = Color(0xFFF5F5FA),
    onSurface = Color(0xFF333344),

    tertiary = PaleBlueColor  // Set your custom pale blue as tertiary color
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF82BDDB),  // Use the same custom green for dark theme
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3D3783),
    onPrimaryContainer = Color(0xFFE0DEFF),

    secondary = LightBlueColor,  // Use the custom light blue for dark theme
    onSecondary = Color(0xFF000000),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1C1B2E),
    onSurface = Color(0xFFE0E0F0),

    tertiary = PaleBlueColor  // Use the custom pale blue for dark theme
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
