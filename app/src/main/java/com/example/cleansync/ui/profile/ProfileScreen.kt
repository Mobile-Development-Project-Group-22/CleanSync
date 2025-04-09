package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cleansync.navigation.Screen
import com.google.firebase.auth.GoogleAuthProvider
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    navController: NavController
) {
    val profileState = profileViewModel.profileState.collectAsState().value
    val currentUser = profileViewModel.currentUser
    val context = LocalContext.current

    // States for dialogs and preferences
    var showUpdateProfileDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }  // Store selected image URI

    // Language preference state
    var selectedLanguage by remember { mutableStateOf("en") }
    val languageOptions = listOf("English", "Spanish", "French")
    var expanded by remember { mutableStateOf(false) }

    // Notification preferences state
    var emailNotificationsEnabled by remember { mutableStateOf(true) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (profileState is ProfileState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        TopAppBar(
            title = {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // Profile Image
            val imagePainter = rememberAsyncImagePainter(
                model = currentUser?.photoUrl
                    ?: "https://png.pngtree.com/png-clipart/20231019/original/pngtree-user-profile-avatar-png-image_13369989.png".toUri(),
            )
            Image(
                painter = imagePainter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp)
                    .clickable {
                        // Implement image picker logic here (e.g., using an image picker library)
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = currentUser?.displayName ?: "No Name",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row {
                Text(
                    text = currentUser?.email ?: "No Email",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (profileViewModel.isEmailVerified) "Verified" else "Not Verified",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = if (profileViewModel.isEmailVerified) Color.Green else Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }



            Spacer(modifier = Modifier.height(24.dp))

            // Language Preferences Section
            Text(
                text = "Language Preferences",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            LanguageSelector(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onButtonClicked = {
                    Toast.makeText(context, "Change language button clicked", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notification Preferences Section
            Text(
                text = "Notification Preferences",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            NotificationPreferences(
                emailNotificationsEnabled = emailNotificationsEnabled,
                pushNotificationsEnabled = pushNotificationsEnabled,
                onEmailNotificationsChanged = { emailNotificationsEnabled = it },
                onPushNotificationsChanged = { pushNotificationsEnabled = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons for Profile Actions
            Button(
                onClick = { showUpdateProfileDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text(text = "Update Profile", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text(text = "Delete Account", style = MaterialTheme.typography.labelLarge)
            }
        }

        LaunchedEffect(profileState) {
            when (profileState) {
                is ProfileState.Error -> {
                    Toast.makeText(context, profileState.message, Toast.LENGTH_LONG).show()
                }
                is ProfileState.Success -> {
                    if (profileState.user == null) {
                        navController.navigate(Screen.LoginScreen.route)
                    } else {
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    // Do nothing
                }
            }
        }

        // Profile Update Dialog
        if (showUpdateProfileDialog) {
            AlertDialog(
                onDismissRequest = { showUpdateProfileDialog = false },
                title = { Text(text = "Update Profile") },
                text = {
                    Column {
                        TextField(
                            value = newDisplayName,
                            onValueChange = { newDisplayName = it },
                            label = { Text("New Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newDisplayName.isNotEmpty()) {
                                profileViewModel.updateUserProfile(newDisplayName, selectedImageUri)
                                showUpdateProfileDialog = false
                            } else {
                                errorMessage = "Display Name cannot be empty"
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showUpdateProfileDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Account Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = "Confirm Account Deletion") },
                text = {
                    Column {
                        if (currentUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true) {
                            Text("You are signed in with Google. Please confirm to delete your account.")
                        } else {
                            Text("Please enter your current password to delete your account.")
                            TextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Current Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                isError = errorMessage.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.textFieldColors(
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
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
                            if (currentUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true) {
                                profileViewModel.reAuthenticateAndDeleteUser(
                                    password = null,
                                    context = context,
                                    onSuccess = {
                                        showDeleteDialog = false
                                        navController.navigate(Screen.LoginScreen.route) {
                                            popUpTo(Screen.ProfileScreen.route) {
                                                inclusive = true
                                            } // Ensures the profile screen is removed from the back stack
                                        }
                                        Toast.makeText(
                                            context,
                                            "Account deleted successfully",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    onFailure = { errorMessage ->
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG)
                                            .show()
                                    }


                                )
                            } else {
                                if (currentPassword.isNotEmpty()) {
                                    profileViewModel.reAuthenticateAndDeleteUser(
                                        password = currentPassword,
                                        onSuccess = {
                                            showDeleteDialog = false
                                            navController.navigate(Screen.LoginScreen.route) {
                                                popUpTo(Screen.ProfileScreen.route) {
                                                    inclusive = true
                                                } // Ensures the profile screen is removed from the back stack
                                            }
                                            Toast.makeText(
                                                context,
                                                "Account deleted successfully",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        },
                                        onFailure = { errorMessage ->
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG)
                                                .show()

                                        },
                                        context = context
                                    )
                                } else {
                                    errorMessage = "Password cannot be empty"
                                }
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = false }
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
