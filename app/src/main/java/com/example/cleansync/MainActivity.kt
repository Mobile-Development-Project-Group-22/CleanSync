package com.example.cleansync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.cleansync.ui.theme.CleanSyncTheme

// MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanSyncTheme {
                // A surface container using the 'background' color from the theme
                CleanSyncApp()
            }

        }
    }
}




// Preview of the CleanSync app for debugging
@Preview(showBackground = true)
@Composable
fun CleanSyncAppPreview() {
    CleanSyncApp()
}
