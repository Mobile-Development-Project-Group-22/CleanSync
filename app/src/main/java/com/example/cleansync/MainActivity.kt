package com.example.cleansync

import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.theme.CleanSyncTheme
import com.example.cleansync.ui.theme.ThemeMode
import com.example.cleansync.ui.theme.ThemePreferenceManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        // 🔹 Install splash BEFORE super
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authViewModel = AuthViewModel()
        var isThemeLoaded by mutableStateOf(false)

        // 🔹 Detect system dark mode
        val isDarkTheme = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }

        // 🔹 Set splash background dynamically based on system theme
        window.setBackgroundDrawableResource(
            if (isDarkTheme) R.color.splash_background_dark
            else R.color.splash_background_light
        )

        // 🔹 Keep splash until Firebase + Theme are ready
        splashScreen.setKeepOnScreenCondition {
            !authViewModel.isAppReady.value || !isThemeLoaded
        }

        // 🔹 Smooth fade + scale exit animation
        splashScreen.setOnExitAnimationListener { splashView ->
            splashView.view.animate()
                .alpha(0f)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { splashView.remove() }
                .start()
        }

        // 🔹 Compose content
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }

            // 🔹 Load theme from DataStore
            LaunchedEffect(Unit) {
                ThemePreferenceManager.getThemeMode(this@MainActivity).collect { mode ->
                    themeMode = mode
                    isThemeLoaded = true
                }
            }

            CleanSyncTheme(themeMode = themeMode) {
                CleanSyncApp(
                    authViewModel = authViewModel,
                    currentThemeMode = themeMode,
                    onThemeSelected = { mode ->
                        themeMode = mode
                        lifecycleScope.launch {
                            ThemePreferenceManager.saveThemeMode(this@MainActivity, mode)
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CleanSyncAppPreview() {
    val previewThemeMode = ThemeMode.SYSTEM
    CleanSyncTheme(themeMode = previewThemeMode) {
        CleanSyncApp(
            authViewModel = AuthViewModel(),
            currentThemeMode = previewThemeMode,
            onThemeSelected = {}
        )
    }
}
