package com.example.cleansync.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Login", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null // Reset error on input change
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError != null
            )
            emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(modifier = Modifier.height(8.dp))

            // Password Input Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null // Reset error on input change
                },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                },
                isError = passwordError != null
            )
            passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = {
                    // Field validation before login
                    emailError = if (email.isBlank()) "Email cannot be empty" else null
                    passwordError = if (password.isBlank()) "Password cannot be empty" else null

                    if (emailError == null && passwordError == null) {
                        authViewModel.signIn(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up Navigation
            TextButton(
                onClick = {
                    navController.navigate(Screen.SignupScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                }
            ) {
                Text("Don't have an account? Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Handle Authentication States
            when (authState) {
                is AuthState.Loading -> {} // Show loading indicator inside the button
                is AuthState.Error -> {
                    val errorMessage = (authState as AuthState.Error).message
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )

                    // Specific error handling for wrong password or email not found
                    if (errorMessage.contains("user not found", ignoreCase = true)) {
                        emailError = "No account found with this email"
                    }
                    if (errorMessage.contains("wrong password", ignoreCase = true)) {
                        passwordError = "Incorrect password"
                    }
                }
                is AuthState.Success -> {
                    LaunchedEffect(Unit) {
                        onLoginSuccess()
                    }
                }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(
        navController = rememberNavController(),
        authViewModel = AuthViewModel(),
        onLoginSuccess = {}
    )
}
