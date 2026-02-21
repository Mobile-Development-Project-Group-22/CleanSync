// CleanSyncApp.kt
package com.example.cleansync

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.AppNavHost
import com.example.cleansync.navigation.BottomNavBar
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.booking.BookingViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.theme.ThemeMode

@Composable
fun CleanSyncApp(
    authViewModel: AuthViewModel,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val notificationViewModel = NotificationViewModel()
    val navController = rememberNavController()
    val bookingViewModel: BookingViewModel = viewModel()

    MainScreen(
        navController = navController,
        authViewModel = authViewModel,
        notificationViewModel = notificationViewModel,
        bookingViewModel = bookingViewModel,
        currentThemeMode = currentThemeMode,
        onThemeSelected = onThemeSelected
    )
}

@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    bookingViewModel: BookingViewModel,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
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
        Screen.BookingFormScreen.route,
        Screen.PastBookingsScreen.route
    ) || currentRoute?.startsWith("my_bookings_screen") == true

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
            currentThemeMode = currentThemeMode,
            onThemeSelected = onThemeSelected,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
