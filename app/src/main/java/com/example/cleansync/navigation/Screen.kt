package com.example.cleansync.navigation

sealed class Screen(val route: String) {
    object LoginScreen : Screen("login_screen")
    object SignupScreen : Screen("signup_screen")
    object HomeScreen : Screen("home_screen")
    object BookingScreen : Screen("booking_screen")
    object ProfileScreen : Screen("profile_screen")
}