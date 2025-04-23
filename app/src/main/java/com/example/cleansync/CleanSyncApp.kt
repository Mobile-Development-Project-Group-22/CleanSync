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
import com.example.cleansync.navigation.AppNavHost
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
        Screen.ProfileScreen.route,
        Screen.BookingStartScreen.route,
        Screen.BookingFormScreen.route
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




