package com.example.cleansync.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthState
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.profile.ProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUpdateScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val loginState by authViewModel.loginState.collectAsState()
    val userName by profileViewModel.userName.collectAsState()
    val userEmail by profileViewModel.userEmail.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    // Initialize the updatedName and updatedEmail with the current values or empty strings if null
    var updatedName by remember { mutableStateOf(userName ?: "") }
    var updatedEmail by remember { mutableStateOf(userEmail ?: "") }
    var currentPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Top bar with back navigation
            TopAppBar(
                title = { Text("Update Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),  // Make the Column scrollable
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = loginState) {
                    is AuthState.Success -> {
                        if (isLoading) {
                            CircularProgressIndicator()  // Show spinner when loading
                        } else {
                            // Name TextField with pre-filled value
                            TextField(
                                value = updatedName,
                                onValueChange = { updatedName = it },  // Update the name value
                                label = { Text("Update Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Email TextField with pre-filled value
                            TextField(
                                value = updatedEmail,
                                onValueChange = { updatedEmail = it },  // Update the email value
                                label = { Text("Update Email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Current Password TextField (for re-authentication)
                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Current Password") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onDone = { keyboardController?.hide() }
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Update Button
                            Button(
                                onClick = {
                                    // Ensure current password is provided and name/email are not empty
                                    if (currentPassword.isNotEmpty()) {
                                        if (updatedName.isNotBlank() && updatedEmail.isNotBlank()) {
                                            // Proceed with the update
                                            profileViewModel.updateProfile(updatedName, updatedEmail, currentPassword)
                                        } else {
                                            // Set error message for blank name/email fields
                                            profileViewModel.setErrorMessage("Name and Email cannot be empty.")
                                        }
                                    } else {
                                        // Set error message for empty password field
                                        profileViewModel.setErrorMessage("Please enter your current password.")
                                    }
                                    keyboardController?.hide()  // Hide keyboard after pressing the button
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Update Profile")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Display the error message if it exists
                            errorMessage?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Back to Profile View
                            Button(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Back to Profile")
                            }
                        }
                    }
                    else -> {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}


