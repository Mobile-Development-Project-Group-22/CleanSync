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
import com.example.cleansync.ui.booking.MyBookingsScreen
import com.example.cleansync.ui.notifications.NotificationScreen
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.profile.ProfileScreen
import com.example.cleansync.ui.profile.ProfileViewModel

@Composable
fun CleanSyncApp(
    authViewModel: AuthViewModel
) {
    val notificationViewModel = NotificationViewModel()
    val navController = rememberNavController()

    MainScreen(
        navController = navController,
        authViewModel = authViewModel,
        notificationViewModel = notificationViewModel
    )
}

@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel
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
            modifier = Modifier.padding(innerPadding)
        )
    }
}




@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    modifier: Modifier = Modifier
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
            )
        }
        composable(Screen.NotificationScreen.route) {
            NotificationScreen(
                viewModel = notificationViewModel,
            )
        }
        composable(Screen.BookingStartScreen.route) {
            MyBookingsScreen(
                bookingViewModel = viewModel(),

            )
        }
        composable(Screen.MyBookingsScreen.route) {
            MyBookingsScreen(
                bookingViewModel = viewModel(),
            )
        }
        composable(Screen.ProfileScreen.route) {
            ProfileScreen(
                profileViewModel= ProfileViewModel(),
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
            )
        }

    }
}
