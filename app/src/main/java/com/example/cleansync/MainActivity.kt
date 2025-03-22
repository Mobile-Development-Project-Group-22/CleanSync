package com.example.cleansync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.auth.LoginScreen
import com.example.cleansync.ui.auth.SignupScreen
import com.example.cleansync.ui.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanSyncApp()
        }
    }
}

@Composable
fun CleanSyncApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel() // Create or retrieve AuthViewModel

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.LoginScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.LoginScreen.route) {
                LoginScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.SignupScreen.route) {
                SignupScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.HomeScreen.route) {
                HomeScreen(navController = navController, authViewModel = authViewModel)
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

    CleanSyncApp()
}