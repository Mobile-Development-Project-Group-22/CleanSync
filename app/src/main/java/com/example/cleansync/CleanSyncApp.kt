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
import com.example.cleansync.ui.auth.*
import com.example.cleansync.ui.booking.*
import com.example.cleansync.ui.home.HomeScreen
import com.example.cleansync.ui.notifications.NotificationScreen
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.profile.ProfileScreen
import com.example.cleansync.ui.profile.ProfileViewModel

@Composable
fun CleanSyncApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

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
                if (isLoggedIn) {
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

            composable(Screen.SignupScreen.route) {
                if (isLoggedIn) {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.SignupScreen.route) { inclusive = true }
                    }
                } else {
                    SignupScreen(navController = navController, authViewModel = authViewModel)
                }
            }

            composable(Screen.HomeScreen.route) {
                if (!isLoggedIn) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                } else {
                    HomeScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        notificationViewModel = NotificationViewModel(),
                        profileViewModel = ProfileViewModel(),
                    )
                }
            }

            composable(Screen.NotificationScreen.route) {
                if (!isLoggedIn) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.NotificationScreen.route) { inclusive = true }
                    }
                } else {
                    NotificationScreen(
                        navController = navController,
                        notificationViewModel = NotificationViewModel()
                    )
                }
            }

            composable(Screen.ProfileScreen.route) {
                if (!isLoggedIn) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                } else {
                    ProfileScreen(navController = navController, profileViewModel = ProfileViewModel())
                }
            }

            composable(Screen.BookingScreen.route) {
                if (!isLoggedIn) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.BookingScreen.route) { inclusive = true }
                    }
                } else {
                    BookingScreen(
                        bookingViewModel = BookingViewModel(),
                        onBookingConfirmed = {
                            navController.navigate(Screen.BookingFormScreen.route)
                        },
                        onBookingCancelled = {
                            navController.navigate(Screen.HomeScreen.route)
                        }
                    )
                }
            }

            // ✅ BookingFormScreen composable
            composable(Screen.BookingFormScreen.route) {
                BookingFormScreen(
                    bookingViewModel = BookingViewModel(),
                    onBookingDone = {
                        navController.navigate(Screen.BookingConfirmationScreen.route)
                    }
                )
            }

            // ✅ BookingConfirmationScreen composable
            composable(Screen.BookingConfirmationScreen.route) {
                BookingConfirmationScreen(
                    bookingViewModel = BookingViewModel(), // Pass the same instance if possible
                    onReturnHome = {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.BookingConfirmationScreen.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.PasswordResetScreen.route) {
                if (isLoggedIn) {
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
