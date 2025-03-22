package com.example.cleansync.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthState
import com.example.cleansync.ui.auth.AuthViewModel


@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel // Pass AuthViewModel instead of HomeViewModel
) {
    val loginState by authViewModel.loginState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loginState is AuthState.Success) {
                Text(
                    text = "Welcome to Home Screen!",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    authViewModel.logout() // Call logout
                    navController.navigate(Screen.LoginScreen.route) // Navigate to LoginScreen
                }) {
                    Text("Logout")
                }
            } else {
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
