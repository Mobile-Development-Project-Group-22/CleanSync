package com.example.cleansync.ui.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel.AuthState
import kotlinx.coroutines.delay

@Composable
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    var showVerificationDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Debounce state
    var isSignupEnabled by remember { mutableStateOf(true) }

    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() &&
                    email.isValidEmail() &&
                    password.length >= 8 &&
                    password == confirmPassword &&
                    password.isNotBlank()
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignupSuccess -> {
                // Only show dialog after successful signup
                showVerificationDialog = true
                authViewModel.currentUser?.sendEmailVerification()
                isSignupEnabled = true
                // Send verification email if not already sent
                }

            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                isSignupEnabled = true
            }
            else -> Unit
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Sign Up",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Fill in your details to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Form fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    isError = email.isNotBlank() && !email.isValidEmail(),
                    supportingText = {
                        if (email.isNotBlank() && !email.isValidEmail()) {
                            Text("Please enter a valid email address")
                        }
                    }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    isError = password.isNotBlank() && password.length < 8,
                    supportingText = {
                        if (password.isNotBlank() && password.length < 8) {
                            Text("Password must be at least 8 characters")
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    isError = confirmPassword.isNotBlank() && password != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isNotBlank() && password != confirmPassword) {
                            Text("Passwords don't match")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        isSignupEnabled = false
                        focusManager.clearFocus()
                        authViewModel.signUp(name, email, password)
                        showVerificationDialog = true
                    } else {
                        Toast.makeText(
                            context,
                            "Please fill all fields correctly",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = isSignupEnabled && isFormValid && authState !is AuthState.Loading,
                shape = MaterialTheme.shapes.large
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Sign Up",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Log in",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(Screen.SignupScreen.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }

    if (showVerificationDialog) {
        EmailVerificationDialog(
            email = email,
            onDismiss = { showVerificationDialog = false },
            onNavigateToLogin = {
                showVerificationDialog = false
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.SignupScreen.route) { inclusive = true }
                }
            },
            autoNavigateAfterSeconds = 10
        )
    }
}

@Composable
fun EmailVerificationDialog(
    email: String,
    onDismiss: () -> Unit,
    onNavigateToLogin: () -> Unit,
    autoNavigateAfterSeconds: Int = 10
) {

    var countdown by remember { mutableStateOf(autoNavigateAfterSeconds) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        if (countdown == 0) {
            onNavigateToLogin()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Account Created Successfully!",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text("Thank you for signing up!")
                Spacer(modifier = Modifier.height(8.dp))
                Text("We've sent a verification email to:")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please verify your email before logging in.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You'll be redirected to login in $countdown seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier.width(150.dp)
            ) {
                Text("Got it (${countdown}s)")
            }
        },
        shape = MaterialTheme.shapes.large
    )
}

