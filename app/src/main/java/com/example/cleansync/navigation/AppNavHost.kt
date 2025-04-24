package com.example.cleansync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.auth.LoginScreen
import com.example.cleansync.ui.auth.PasswordResetScreen
import com.example.cleansync.ui.auth.SignupScreen
import com.example.cleansync.ui.booking.BookingConfirmationScreen
import com.example.cleansync.ui.booking.BookingFormScreen
import com.example.cleansync.ui.booking.BookingStartScreen
import com.example.cleansync.ui.booking.BookingViewModel
import com.example.cleansync.ui.booking.MyBookingsScreen
import com.example.cleansync.ui.home.HomeScreen
import com.example.cleansync.ui.notifications.NotificationScreen
import com.example.cleansync.ui.notifications.NotificationSettingsViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.profile.ProfileScreen
import com.example.cleansync.ui.profile.ProfileViewModel
import com.example.cleansync.ui.profile.SettingScreen
import com.example.cleansync.ui.profile.SupportScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    bookingViewModel: BookingViewModel,
    modifier: Modifier = Modifier,
    onThemeToggle: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route,
        modifier = modifier
    ) {
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.SignupScreen.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.PasswordResetScreen.route)
                }
            )
        }

        composable(Screen.SignupScreen.route) {
            SignupScreen(
                authViewModel = authViewModel,
                onSignupSuccess = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.SignupScreen.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.SignupScreen.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.PasswordResetScreen.route) {
            PasswordResetScreen(
                authViewModel = AuthViewModel(),
                onPasswordResetSuccess = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.PasswordResetScreen.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.PasswordResetScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.HomeScreen.route) {
            HomeScreen(
                authViewModel = authViewModel,
                notificationViewModel = notificationViewModel,
                onNavigateToBooking = {
                    navController.navigate(Screen.BookingStartScreen.route)
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.NotificationScreen.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileScreen.route)
                },
                onLogout = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                },
                bookingViewModel = BookingViewModel(),

            )
        }
        composable(Screen.NotificationScreen.route) {
            NotificationScreen(
                viewModel = notificationViewModel,
            )
        }

        composable(Screen.MyBookingsScreen.route) {
            MyBookingsScreen(
                viewModel = bookingViewModel,
                onBookingClick = {
                    navController.navigate(Screen.BookingStartScreen.route)
                },
            )
        }

        composable(Screen.BookingStartScreen.route) {
            BookingStartScreen(
                bookingViewModel = bookingViewModel,
                onBookingConfirmed = {
                    navController.navigate(Screen.BookingFormScreen.route)
                },
                onBookingCancelled = {
                    navController.navigate(Screen.MyBookingsScreen.route)
                }
            )
        }
        composable(Screen.BookingConfirmationScreen.route) {
            BookingConfirmationScreen(
                bookingViewModel = bookingViewModel,
                onReturnHome = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.BookingStartScreen.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.BookingFormScreen.route) {
            BookingFormScreen(
                bookingViewModel = bookingViewModel,
                onBookingDone = {
                    navController.navigate(Screen.BookingConfirmationScreen.route) {
                        popUpTo(Screen.BookingFormScreen.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ProfileScreen.route) {
            ProfileScreen(
                profileViewModel = ProfileViewModel(),
                preferencesViewModel = NotificationSettingsViewModel(),
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                },

                onNavigateToBookings = {
                    navController.navigate(Screen.MyBookingsScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.SettingScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                },
                onNavigateToSupport = {
                    navController.navigate(Screen.SupportScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                },
                onThemeToggle = onThemeToggle,)
        }
        composable(Screen.SettingScreen.route) {
            SettingScreen(
                onBackClick = {
                    navController.navigate(Screen.ProfileScreen.route) {
                        popUpTo(Screen.SettingScreen.route) { inclusive = true }
                    }
                },
                preferencesViewModel = NotificationSettingsViewModel(),
                profileViewModel = ProfileViewModel(),
                onThemeToggle = onThemeToggle,
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.SettingScreen.route) { inclusive = true }
                    }

                },

                )

        }
        composable(Screen.SupportScreen.route) {
            SupportScreen(
                onBackClick = {
                    navController.navigate(Screen.ProfileScreen.route) {
                        popUpTo(Screen.SupportScreen.route) { inclusive = true }
                    }
                },
            )
        }

    }
}

