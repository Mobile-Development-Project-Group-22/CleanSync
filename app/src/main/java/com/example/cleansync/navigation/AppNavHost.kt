// AppNavHost.kt
package com.example.cleansync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cleansync.ui.auth.*
import com.example.cleansync.ui.booking.*
import com.example.cleansync.ui.home.HomeScreen
import com.example.cleansync.ui.notifications.*
import com.example.cleansync.ui.profile.*
import com.example.cleansync.ui.theme.ThemeMode

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    bookingViewModel: BookingViewModel,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route,
        modifier = modifier
    ) {
        // --- Auth ---
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate(Screen.SignupScreen.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.PasswordResetScreen.route) }
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
                authViewModel = authViewModel,
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

        // --- Home ---
        composable(Screen.HomeScreen.route) {
            HomeScreen(
                authViewModel = authViewModel,
                notificationViewModel = notificationViewModel,
                bookingViewModel = bookingViewModel,
                onNavigateToBooking = { navController.navigate(Screen.BookingStartScreen.route) },
                onNavigateToNotifications = { navController.navigate(Screen.NotificationScreen.route) },
                onNavigateToProfile = { navController.navigate(Screen.ProfileScreen.route) },
                onNavigateToMyBookings = { navController.navigate(Screen.MyBookingsScreen.route) },
                onLogout = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Notifications ---
        composable(Screen.NotificationScreen.route) {
            NotificationScreen(viewModel = notificationViewModel)
        }

        // --- Bookings ---
        composable(Screen.MyBookingsScreen.route) {
            MyBookingsScreen(viewModel = bookingViewModel, onBookingClick = {
                navController.navigate(Screen.BookingStartScreen.route)
            })
        }

        composable(Screen.BookingConfirmationScreen.route) {
            BookingConfirmationScreen(bookingViewModel = bookingViewModel, onReturnHome = {
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.BookingStartScreen.route) { inclusive = true }
                }
            })
        }

        composable(Screen.BookingFormScreen.route) {
            BookingFormScreen(bookingViewModel = bookingViewModel, onBookingDone = {
                navController.navigate(Screen.BookingConfirmationScreen.route) {
                    popUpTo(Screen.BookingFormScreen.route) { inclusive = true }
                }
            })
        }

        // --- Profile & Settings ---
        composable(Screen.ProfileScreen.route) {
            ProfileScreen(
                profileViewModel = ProfileViewModel(),
                preferencesViewModel = NotificationSettingsViewModel(),
                currentThemeMode = currentThemeMode,
                onThemeSelected = onThemeSelected,
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
                onNavigateToContact = {
                    navController.navigate(Screen.ContactUs.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SettingScreen.route) {
            SettingScreen(
                profileViewModel = ProfileViewModel(),
                preferencesViewModel = NotificationSettingsViewModel(),
                currentThemeMode = currentThemeMode,
                onThemeSelected = onThemeSelected,
                onBackClick = { navController.navigate(Screen.ProfileScreen.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.SettingScreen.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Contact & Support ---
        composable(Screen.ContactUs.route) {
            ContactUs(
                onBackClick = { navController.navigate(Screen.ProfileScreen.route) },
                onNavigateToSupport = { navController.navigate(Screen.SupportScreen.route) }
            )
        }

        composable(Screen.SupportScreen.route) {
            SupportScreen(
                onBackClick = { navController.navigate(Screen.ContactUs.route) }
            )
        }

    }
}
