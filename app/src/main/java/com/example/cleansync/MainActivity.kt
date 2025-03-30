package com.example.cleansync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.cleansync.navigation.NavigationItem

import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.auth.LoginScreen
import com.example.cleansync.ui.auth.SignupScreen
import com.example.cleansync.ui.booking.BookingScreen
import com.example.cleansync.ui.home.HomeScreen
import com.example.cleansync.ui.profile.ProfileScreen
import com.example.cleansync.ui.profile.ProfileViewModel

// MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanSyncApp()
        }
    }
}

// Composable for the CleanSync app with conditional bottom navigation for logged-in users
@Composable
fun CleanSyncApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    // Observing login state
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Scaffold structure with bottom navigation
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isLoggedIn) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.HomeScreen.route else Screen.LoginScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.LoginScreen.route) {
                LoginScreen(navController = navController, authViewModel = authViewModel, onLoginSuccess = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.SignupScreen.route) {
                SignupScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.HomeScreen.route) {
                HomeScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.ProfileScreen.route) {
                ProfileScreen(navController = navController, profileViewModel = ProfileViewModel())
            }
            composable(Screen.BookingScreen.route) {
                BookingScreen(navController = navController)
            }
        }
    }
}

// Bottom navigation bar composable
@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home, "home_screen"),
        NavigationItem("Booking", Icons.Default.Home, "booking_screen"),
        NavigationItem("Profile", Icons.Default.Person, "profile_screen")
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}



// Preview of the CleanSync app for debugging
@Preview(showBackground = true)
@Composable
fun CleanSyncAppPreview() {
    CleanSyncApp()
}
