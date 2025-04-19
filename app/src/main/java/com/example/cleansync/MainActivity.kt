package com.example.cleansync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.profile.ThemePreferenceManager
import com.example.cleansync.ui.theme.CleanSyncTheme
import kotlinx.coroutines.launch

// MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val context = this

        setContent {
            val defaultDarkTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(defaultDarkTheme) }

            // 🔥 Load the saved preference
            LaunchedEffect(Unit) {
                ThemePreferenceManager.getDarkMode(context).collect {
                    isDarkMode = it
                }
            }

            CleanSyncTheme(darkTheme = isDarkMode) {
                CleanSyncApp(
                    authViewModel = AuthViewModel(),
                    onThemeToggle = {
                        isDarkMode = it
                        lifecycleScope.launch {
                            ThemePreferenceManager.saveDarkMode(context, it)
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
    val defaultDarkTheme = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(defaultDarkTheme) }

    CleanSyncTheme(darkTheme = isDarkMode) {
        CleanSyncApp(
            authViewModel = AuthViewModel(),
            onThemeToggle = { newState -> isDarkMode = newState }
        )
    }
}

