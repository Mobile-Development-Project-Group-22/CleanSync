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
import com.example.cleansync.ui.booking.BookingViewModel
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
            // Navigation for login screen
            composable(Screen.LoginScreen.route) {
                if (isLoggedIn) {
                    // Redirect to HomeScreen if already logged in
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                } else {
                    LoginScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.HomeScreen.route) {
                                popUpTo(Screen.LoginScreen.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Navigation for signup screen
            composable(Screen.SignupScreen.route) {
                if (isLoggedIn) {
                    // Redirect to HomeScreen if already logged in
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.SignupScreen.route) { inclusive = true }
                    }
                } else {
                    SignupScreen(navController = navController, authViewModel = authViewModel)
                }
            }

            // Navigation for HomeScreen (logged-in user only)
            composable(Screen.HomeScreen.route) {
                if (!isLoggedIn) {
                    // Redirect to LoginScreen if not logged in
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                } else {
                    HomeScreen(navController = navController, authViewModel = authViewModel)
                }
            }

            // Navigation for ProfileScreen
            composable(Screen.ProfileScreen.route) {
                if (!isLoggedIn) {
                    // Redirect to LoginScreen if not logged in
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                } else {
                    ProfileScreen(navController = navController, profileViewModel = ProfileViewModel())
                }
            }

            // Navigation for BookingScreen
            composable(Screen.BookingScreen.route) {
                if (!isLoggedIn) {
                    // Redirect to LoginScreen if not logged in
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.BookingScreen.route) { inclusive = true }
                    }
                } else {
                    BookingScreen(
                        bookingViewModel = BookingViewModel(),
                        onBookingConfirmed = {
                            // Handle booking confirmation
                            navController.navigate(Screen.BookingConfirmationScreen.route)
                        },
                        onBookingCancelled = {
                            // Handle booking cancellation
                            navController.navigate(Screen.HomeScreen.route)
                        }
                    )
                }
            }

            // Navigation for PasswordResetScreen
            composable(Screen.PasswordResetScreen.route) {
                if (isLoggedIn) {
                    // Redirect to HomeScreen if already logged in
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.PasswordResetScreen.route) { inclusive = true }
                    }
                } else {
                    PasswordResetScreen(navController = navController)
                }
            }
        }
    }
}
