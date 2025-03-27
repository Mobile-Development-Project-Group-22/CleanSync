package com.example.cleansync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cleansync.navigation.BottomNavigationBar
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.auth.LoginScreen
import com.example.cleansync.ui.auth.SignupScreen
import com.example.cleansync.ui.booking.BookingScreen
import com.example.cleansync.ui.home.HomeScreen
import com.example.cleansync.ui.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanSyncApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanSyncApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel() // Create or retrieve AuthViewModel

    // Get the current backstack entry to check the selected tab
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Function to get the title based on the current screen
    val currentScreenTitle = when (backStackEntry?.destination?.route) {
        Screen.LoginScreen.route -> "Login"
        Screen.SignupScreen.route -> "Sign Up"
        Screen.HomeScreen.route -> "Home"
        Screen.BookingScreen.route -> "Booking"
        Screen.ProfileScreen.route -> "Profile"
        else -> "CleanSync" // Default title if no match
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreenTitle) } // Dynamically set the title
            )
        },
        bottomBar = {
            BottomNavigationBar(navController) // Use BottomNavigationBar here
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.LoginScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.LoginScreen.route) {
                LoginScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.SignupScreen.route) {
                SignupScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.HomeScreen.route) {
                HomeScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.BookingScreen.route) {
                BookingScreen(navController = navController)
            }
            composable(Screen.ProfileScreen.route) {
                ProfileScreen(navController = navController, authViewModel = authViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CleanSyncApp()
}
