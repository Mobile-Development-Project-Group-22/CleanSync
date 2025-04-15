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
    val notificationViewModel: NotificationViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()

    val isLoggedIn = authViewModel.authState.collectAsState().value.let { state ->
        state is AuthViewModel.AuthState.LoginSuccess || state is AuthViewModel.AuthState.SignupSuccess
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isLoggedIn) {
                BottomNavBar(
                    navController = navController,
                    unreadCount = notificationViewModel.unreadNotificationsCount()
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.HomeScreen.route else Screen.LoginScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // Auth Screens
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

            composable(Screen.PasswordResetScreen.route) {
                if (isLoggedIn) {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.PasswordResetScreen.route) { inclusive = true }
                    }
                } else {
                    PasswordResetScreen(navController = navController)
                }
            }

            // Main Screens
            composable(Screen.HomeScreen.route) {
                if (!isLoggedIn) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                } else {
                    HomeScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        notificationViewModel = notificationViewModel,
                        profileViewModel = profileViewModel
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
                        viewModel = notificationViewModel
                    )
                }
            }

            composable(Screen.ProfileScreen.route) {
                if (!isLoggedIn) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                } else {
                    ProfileScreen(
                        navController = navController,
                        profileViewModel = profileViewModel
                    )
                }
            }

            // Booking Flow Screens
            composable(Screen.BookingStartScreen.route) {
                if (!isLoggedIn) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.BookingStartScreen.route) { inclusive = true }
                    }
                } else {
                    BookingStartScreen(
                        bookingViewModel = bookingViewModel,
                        onBookingConfirmed = {
                            navController.navigate(Screen.BookingFormScreen.route)
                        },
                        onBookingCancelled = {
                            navController.navigate(Screen.HomeScreen.route)
                        }
                    )
                }
            }

            composable(Screen.BookingFormScreen.route) {
                BookingFormScreen(
                    bookingViewModel = bookingViewModel,
                    onBookingDone = {
                        navController.navigate(Screen.BookingConfirmationScreen.route)
                    }
                )
            }

            composable(Screen.BookingConfirmationScreen.route) {
                BookingConfirmationScreen(
                    bookingViewModel = bookingViewModel,
                    onReturnHome = {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.BookingConfirmationScreen.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.MyBookingsScreen.route) {
                MyBookingsScreen(
                    navController = navController,
                    bookingViewModel = bookingViewModel
                )
            }

        }
    }
}
