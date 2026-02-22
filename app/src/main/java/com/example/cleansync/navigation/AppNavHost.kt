package com.example.cleansync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
    startDestination: String,
    authViewModel: AuthViewModel,
    bookingViewModel: BookingViewModel,
    notificationViewModel: NotificationViewModel,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ------------------------
        // Authentication
        // ------------------------
        composable(Screen.LoginScreen.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
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

        // ------------------------
        // Home
        // ------------------------
        composable(Screen.HomeScreen.route) {
            HomeScreen(
                authViewModel = authViewModel,
                bookingViewModel = bookingViewModel,
                notificationViewModel = notificationViewModel,
                onNavigateToBooking = { navController.navigate(Screen.BookingStartScreen.route) },
                onNavigateToNotifications = { navController.navigate(Screen.NotificationScreen.route) },
                onNavigateToProfile = { navController.navigate(Screen.ProfileScreen.route) },
                onNavigateToMyBookings = { bookingId ->
                    navController.navigate(Screen.MyBookingsScreen.createRoute(bookingId))
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ------------------------
        // Notifications
        // ------------------------
        composable(Screen.NotificationScreen.route) {
            NotificationScreen(viewModel = notificationViewModel)
        }

        // ------------------------
        // Bookings
        // ------------------------
        composable(Screen.MyBookingsScreen.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId")
            MyBookingsScreen(
                viewModel = bookingViewModel,
                scrollToBookingId = if (bookingId != "null") bookingId else null,
                onBookingClick = {
                    navController.navigate(Screen.BookingStartScreen.route)
                }
            )
        }

        composable(Screen.BookingStartScreen.route) {
            BookingStartScreen(
                bookingViewModel = bookingViewModel,
                onBookingConfirmed = {
                    navController.navigate(Screen.BookingFormScreen.route)
                },
                onBookingCancelled = {
                    navController.popBackStack()
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

        // ------------------------
        // Profile & Settings
        // ------------------------
        composable(Screen.ProfileScreen.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            ProfileScreen(
                profileViewModel = profileViewModel,
                preferencesViewModel = NotificationSettingsViewModel(),
                currentThemeMode = currentThemeMode,
                onThemeSelected = onThemeSelected,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToBookings = {
                    navController.navigate(Screen.MyBookingsScreen.createRoute(null))
                },
                onNavigateToPastBookings = {
                    navController.navigate(Screen.PastBookingsScreen.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.SettingScreen.route)
                },
                onNavigateToSupport = {
                    navController.navigate(Screen.SupportScreen.route)
                },
                onNavigateToContact = {
                    navController.navigate(Screen.ContactUs.route)
                },
                onNavigateToLogin = {
                    authViewModel.signOut()
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PastBookingsScreen.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            PastBookingsScreen(
                profileViewModel = profileViewModel,
                bookingViewModel = bookingViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToConfirmation = {
                    navController.navigate(Screen.BookingConfirmationScreen.route)
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
                    authViewModel.signOut()
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ------------------------
        // Contact & Support
        // ------------------------
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