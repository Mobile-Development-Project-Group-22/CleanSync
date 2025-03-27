package com.example.cleansync.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.auth.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val loginState by authViewModel.loginState.collectAsState()

    // Get current screen title dynamically
    val screenTitle = "Profile" // You can hardcode or use logic to make it dynamic if needed

    Scaffold(

        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
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
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = loginState) {
                    is AuthState.Success -> {
                        // Directly access user data here
                        Text(
                            text = "Welcome, ${state.user?.displayName ?: "User"}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Email: ${state.user?.email}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.LoginScreen.route) {
                                popUpTo(Screen.ProfileScreen.route) { inclusive = true }
                            }
                        }) {
                            Text("Logout")
                        }
                    }
                    is AuthState.Error -> {
                        Text(
                            text = "Authentication failed. Please try again.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is AuthState.Loading -> {
                        Text("Loading...", style = MaterialTheme.typography.bodyLarge)
                    }
                    else -> {
                        Text(
                            text = "You are not logged in!",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            navController.navigate(Screen.LoginScreen.route)
                        }) {
                            Text("Login")
                        }
                    }
                }
            }
        }
    }
}
