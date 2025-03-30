package com.example.cleansync

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.BottomNavBar
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.auth.LoginScreen
import com.example.cleansync.ui.auth.PasswordResetScreen
import com.example.cleansync.ui.auth.SignupScreen
import com.example.cleansync.ui.booking.BookingScreen
import com.example.cleansync.ui.home.HomeScreen
import com.example.cleansync.ui.profile.ProfileScreen
import com.example.cleansync.ui.profile.ProfileViewModel

@Composable
fun CleanSyncApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    // Observing login state
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Scaffold structure with bottom navigation
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isLoggedIn) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.HomeScreen.route else Screen.LoginScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.LoginScreen.route) {
                LoginScreen(navController = navController, authViewModel = authViewModel, onLoginSuccess = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.SignupScreen.route) {
                SignupScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.HomeScreen.route) {
                HomeScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.ProfileScreen.route) {
                ProfileScreen(navController = navController, profileViewModel = ProfileViewModel())
            }
            composable(Screen.BookingScreen.route) {
                BookingScreen(navController = navController)
            }
            composable(Screen.PasswordResetScreen.route) {
                PasswordResetScreen(navController = navController)
            }
        }
    }
}
