package com.example.cleansync.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
