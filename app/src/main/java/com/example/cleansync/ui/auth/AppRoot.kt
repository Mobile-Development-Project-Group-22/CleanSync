package com.example.cleansync.ui.auth

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.CleanSyncSplash
import com.example.cleansync.navigation.AppNavHost
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.booking.BookingViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.theme.ThemeMode

@Composable
fun AppRoot(
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    bookingViewModel: BookingViewModel,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    /*
        Entire app navigation is driven by AuthState.
        No manual navigation from LoginScreen.
        No popUpTo hacks.
        No backstack glitches.
     */
    when (authState) {

        // 🔄 Checking current user (auto login)
        is AuthViewModel.AuthState.Loading -> {
            CleanSyncSplash()
        }

        // 🔓 Not logged in
        is AuthViewModel.AuthState.Idle,
        is AuthViewModel.AuthState.Error,
        is AuthViewModel.AuthState.PasswordResetSent -> {
            AppNavHost(
                navController = navController,
                startDestination = Screen.LoginScreen.route,
                authViewModel = authViewModel,
                notificationViewModel = notificationViewModel,
                bookingViewModel = bookingViewModel,
                currentThemeMode = currentThemeMode,
                onThemeSelected = onThemeSelected
            )
        }

        // ✅ Logged in
        is AuthViewModel.AuthState.LoginSuccess,
        is AuthViewModel.AuthState.SignupSuccess -> {
            AppNavHost(
                navController = navController,
                startDestination = Screen.HomeScreen.route,
                authViewModel = authViewModel,
                notificationViewModel = notificationViewModel,
                bookingViewModel = bookingViewModel,
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