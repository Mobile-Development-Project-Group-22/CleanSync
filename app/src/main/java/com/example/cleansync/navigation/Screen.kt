package com.example.cleansync.navigation

sealed class Screen(val route: String) {

    object LoginScreen : Screen("login_screen")
    object SignupScreen : Screen("signup_screen")
    object HomeScreen : Screen("home_screen")
    object BookingStartScreen : Screen("booking_start_screen")
    object BookingFormScreen : Screen("booking_form_screen")
    object ProfileScreen : Screen("profile_screen")
    object PasswordResetScreen : Screen("password_reset_screen")
    object ChangePasswordScreen : Screen("change_password_screen")
    object BookingConfirmationScreen : Screen("booking_confirmation_screen")
    object NotificationScreen : Screen("notification_screen")
    object VerificationScreen : Screen("verification_screen")
    object MyBookingsScreen : Screen("my_bookings_screen")
    object BookingDetailsScreen : Screen("booking_details_screen")
    object SettingScreen : Screen("setting_screen")
    object SupportScreen : Screen("support_screen")
    object ContactUs : Screen("contact_us")
    object PastBookingsScreen : Screen("past_bookings_screen")
}