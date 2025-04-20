package com.example.cleansync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5B4FB1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0DEFF),
    onPrimaryContainer = Color(0xFF1E1B4B),

    secondary = Color(0xFF8E8DDF),
    onSecondary = Color(0xFFFFFFFF),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1A),

    surface = Color(0xFFF5F5FA),
    onSurface = Color(0xFF333344),

    tertiary = Color(0xFFA688FA)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5B4FB1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3D3783),
    onPrimaryContainer = Color(0xFFE0DEFF),

    secondary = Color(0xFF7D78D1),
    onSecondary = Color(0xFF000000),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1C1B2E),
    onSurface = Color(0xFFE0E0F0),

    tertiary = Color(0xFFC2B8FF)
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
