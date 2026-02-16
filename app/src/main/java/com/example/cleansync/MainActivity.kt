package com.example.cleansync

import android.os.Bundle
import android.util.Log
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
import com.example.cleansync.ui.theme.ThemePreferenceManager
import com.example.cleansync.ui.theme.CleanSyncTheme
import com.example.cleansync.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth


// MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val context = this

        setContent {

            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }

            LaunchedEffect(Unit) {
                ThemePreferenceManager
                    .getThemeMode(this@MainActivity)
                    .collect { themeMode = it }
            }

            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener {
                    Log.d("AUTH", "Signed in")
                }
                .addOnFailureListener {
                    Log.e("AUTH", "Error signing in")
                }


            CleanSyncTheme(themeMode = themeMode) {

                CleanSyncApp(
                    authViewModel = AuthViewModel(),
                    currentThemeMode = themeMode,
                    onThemeSelected = { mode ->
                        themeMode = mode
                        lifecycleScope.launch {
                            ThemePreferenceManager
                                .saveThemeMode(this@MainActivity, mode)
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


