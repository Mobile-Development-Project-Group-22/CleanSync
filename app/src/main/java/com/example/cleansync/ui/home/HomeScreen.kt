package com.example.cleansync.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home") })
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Welcome to CleanSync!", style = MaterialTheme.typography.headlineLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    // Navigate to booking screen or other actions
                    navController.navigate(Screen.BookingScreen.route)
                }) {
                    Text("Go to Booking")
                }

                Spacer(modifier = Modifier.height(16.dp))

//                Button(onClick = {
//                    // Navigate to settings screen or other actions
//                    navController.navigate(Screen.SettingScreen.route)
//                }) {
//                    Text("Go to Settings")
//                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    // Logout action
                    authViewModel.signOut()
                    navController.navigate(Screen.LoginScreen.route) {
                        // Pop up to home screen and make login screen the new start destination
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Logout")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navController = rememberNavController(),
        authViewModel = AuthViewModel()
    )
}
