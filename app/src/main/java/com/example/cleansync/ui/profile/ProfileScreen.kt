package com.example.cleansync.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthState
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Collecting the necessary states from the ViewModel
    val loginState by authViewModel.loginState.collectAsState()
    val userName by profileViewModel.userName.collectAsState()
    val userEmail by profileViewModel.userEmail.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    // Fetch user data if it's not already loaded
    LaunchedEffect(Unit) {
        profileViewModel.getUserName()
        profileViewModel.getUserEmail()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = loginState) {
                    is AuthState.Success -> {
                        if (isLoading) {
                            CircularProgressIndicator()  // Show spinner when loading
                        } else {
                            // Display the user's name and email
                            Text(
                                text = "Welcome, ${userName ?: "User"}!",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Email: ${userEmail ?: "No email"}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Button to navigate to Profile Update
                            Button(
                                onClick = { navController.navigate(Screen.ProfileUpdateScreen.route) }
                            ) {
                                Text("Edit Profile")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Logout Button
                            Button(onClick = {
                                authViewModel.logout()
                                navController.navigate(Screen.LoginScreen.route) {
                                    popUpTo(0) // Clears entire backstack to prevent app closure issues
                                }
                            }) {
                                Text("Logout")
                            }
                        }
                    }
                    else -> {
                        CircularProgressIndicator() // Show loading indicator while waiting for login state
                    }
                }
            }
        }
    }
}
