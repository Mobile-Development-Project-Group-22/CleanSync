package com.example.cleansync

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.AppNavHost
import com.example.cleansync.navigation.BottomNavBar
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.auth.AuthViewModel.AuthState
import com.example.cleansync.ui.booking.BookingViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.theme.ThemeMode

@Composable
fun CleanSyncApp(
    authViewModel: AuthViewModel,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val bookingViewModel: BookingViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()

    when (authState) {

        // 🔄 Splash / Loading
        is AuthState.Loading -> {
            CleanSyncSplash()
        }

        // 🔓 Not logged in → Login flow
        is AuthState.Idle,
        is AuthState.Error,
        is AuthState.PasswordResetSent -> {
            AppNavHost(
                navController = navController,
                startDestination = Screen.LoginScreen.route,
                authViewModel = authViewModel,
                bookingViewModel = bookingViewModel,
                notificationViewModel = notificationViewModel,
                currentThemeMode = currentThemeMode,
                onThemeSelected = onThemeSelected
            )
        }

        // ✅ Logged in → Main app with bottom nav
        is AuthState.LoginSuccess,
        is AuthState.SignupSuccess -> {

            MainAuthenticatedScreen(
                navController = navController,
                authViewModel = authViewModel,
                bookingViewModel = bookingViewModel,
                notificationViewModel = notificationViewModel,
                currentThemeMode = currentThemeMode,
                onThemeSelected = onThemeSelected
            )
        }

        else -> {
            // fallback for any unexpected state
            CleanSyncSplash()
        }
    }
}

// ------------------------
// Splash Screen Composable
// ------------------------
@Composable
fun CleanSyncSplash() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

// ------------------------
// Main Screen (Authenticated)
// ------------------------
@Composable
fun MainAuthenticatedScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    bookingViewModel: BookingViewModel,
    notificationViewModel: NotificationViewModel,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val unreadCount = notificationViewModel.unreadNotificationsCount()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.HomeScreen.route,
        Screen.MyBookingsScreen.route,
        Screen.NotificationScreen.route,
        Screen.ProfileScreen.route
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
            startDestination = Screen.HomeScreen.route,
            authViewModel = authViewModel,
            bookingViewModel = bookingViewModel,
            notificationViewModel = notificationViewModel,
            currentThemeMode = currentThemeMode,
            onThemeSelected = onThemeSelected,
            modifier = Modifier.padding(innerPadding)
        )
    }
}