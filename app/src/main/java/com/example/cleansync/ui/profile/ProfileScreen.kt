package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.cleansync.R
import com.example.cleansync.navigation.Screen

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    navController: NavController
) {
    val profileState = profileViewModel.profileState.collectAsState().value
    val currentUser = profileViewModel.currentUser
    val context = LocalContext.current

    // State for showing the password confirmation dialog
    val showDeleteDialog = remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (profileState is ProfileState.Loading) {
            CircularProgressIndicator()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            val imagePainter = rememberAsyncImagePainter(
                model = currentUser?.photoUrl ?: Uri.parse("https://example.com/default_profile.png"),
            )
            Image(
                painter = imagePainter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .padding(16.dp)
                    .clip(CircleShape)
            )

            Text(
                text = currentUser?.displayName ?: "No Name",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = currentUser?.email ?: "No Email",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Button(
                onClick = {
                    profileViewModel.updateUserProfile("New Name", Uri.parse("https://example.com/photo.jpg"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Update Profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    profileViewModel.updateEmail("newemail@example.com")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Update Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { profileViewModel.sendVerificationEmail() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Send Verification Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Account Button - triggers dialog
            Button(
                onClick = { showDeleteDialog.value = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(text = "Delete Account")
            }
        }

        // Handle Success/Error States
         LaunchedEffect(profileState) {
            when (profileState) {
                is ProfileState.Error -> {
                    Toast.makeText(context, profileState.message, Toast.LENGTH_LONG).show()
                }
                is ProfileState.Success -> {
                    if (profileState.user == null) {
                        // User deleted, navigate to login screen
                        navController.navigate(Screen.LoginScreen.route)

                    } else {
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_LONG).show()
                    }
                }
                else -> {}
            }
        }

        // Delete Account Dialog
        if (showDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text(text = "Confirm Account Deletion") },
                text = {
                    Column {
                        Text("Please enter your current password to delete your account.")
                        TextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = errorMessage.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (currentPassword.isNotEmpty()) {
                                profileViewModel.deleteUser(currentPassword)  // Delete account with the entered password
                                showDeleteDialog.value = false  // Close dialog
                            } else {
                                errorMessage = "Password cannot be empty"
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog.value = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}





@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen(
        profileViewModel = ProfileViewModel(),
        navController = NavController(LocalContext.current)
    )
}
