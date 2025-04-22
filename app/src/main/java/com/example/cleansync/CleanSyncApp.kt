package com.example.cleansync

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.BottomNavBar
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.LoginScreen
import com.example.cleansync.ui.auth.SignupScreen
import com.example.cleansync.ui.home.HomeScreen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.auth.PasswordResetScreen
import com.example.cleansync.ui.booking.BookingConfirmationScreen
import com.example.cleansync.ui.booking.BookingFormScreen
import com.example.cleansync.ui.booking.BookingStartScreen
import com.example.cleansync.ui.booking.MyBookingsScreen
import com.example.cleansync.ui.notifications.NotificationScreen
import com.example.cleansync.ui.notifications.NotificationSettingsViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.profile.ProfileScreen
import com.example.cleansync.ui.profile.ProfileViewModel
import com.example.cleansync.ui.booking.BookingViewModel

@Composable
fun CleanSyncApp(
    authViewModel: AuthViewModel,
    onThemeToggle: (Boolean) -> Unit
) {
    val notificationViewModel = NotificationViewModel()
    val navController = rememberNavController()
    val bookingViewModel: BookingViewModel = viewModel()


    MainScreen(
        navController = navController,
        authViewModel = authViewModel,
        notificationViewModel = notificationViewModel,
        bookingViewModel = bookingViewModel,
        onThemeToggle = onThemeToggle
    )
}

@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    bookingViewModel: BookingViewModel,
    onThemeToggle: (Boolean) -> Unit
) {
    val unreadCount = notificationViewModel.unreadNotificationsCount()
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.HomeScreen.route,
        Screen.MyBookingsScreen.route,
        Screen.NotificationScreen.route,
        Screen.ProfileScreen.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    navController = navController,
                    unreadCount = unreadCount
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            authViewModel = authViewModel,
            notificationViewModel = notificationViewModel,
            bookingViewModel = bookingViewModel,
            modifier = Modifier.padding(innerPadding),
            onThemeToggle = onThemeToggle
        )
    }
}




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
                profileViewModel= ProfileViewModel(),
                preferencesViewModel= NotificationSettingsViewModel(),
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
                onThemeToggle = onThemeToggle,
                onNavigateToBookings = {
                    navController.navigate(Screen.MyBookingsScreen.route) {
                        popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                    }
                }

            )
        }

    }
}
