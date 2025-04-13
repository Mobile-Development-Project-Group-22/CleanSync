package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cleansync.navigation.Screen
import com.google.firebase.auth.GoogleAuthProvider

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
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeletePasswordDialog by remember { mutableStateOf(false) }

    var newDisplayName by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Check if user has password provider
    val hasPasswordProvider = currentUser?.providerData?.any {
        it.providerId == "password"
    } == true

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
                        // Implement image picker logic here
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

            // Show password status only if user has password provider
            if (hasPasswordProvider) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Password: ******",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Password-related buttons (only show if user has password provider)
            if (hasPasswordProvider) {
                Button(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text(text = "Change Password", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showDeletePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = MaterialTheme.shapes.medium,
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text(text = "Delete Password", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Language Selector
            LanguageSelector(
                selectedLanguage = "en",
                        onLanguageSelected = { language ->
//                    profilexViewModel.updateLanguage(language)
                },
                expanded = false,
                onExpandedChange = {
                    // Handle dropdown expansion
                },
                onButtonClicked = {
                    // Handle button click
                    Toast.makeText(
                        context,
                        "Language changed to English",
                        Toast.LENGTH_SHORT
                    ).show()
                },

            )

            Spacer(modifier = Modifier.height(16.dp))

            NotificationPreferences(
                emailNotificationsEnabled = true,
                pushNotificationsEnabled = true,
                onEmailNotificationsChanged = { /* Handle email notifications change */ },
                onPushNotificationsChanged = { /* Handle push notifications change */ }

            )

            // Update Profile Button
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

            // Delete Account Button
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

        // Change Password Dialog
        if (showChangePasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    showChangePasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                    errorMessage = ""
                },
                title = { Text(text = "Change Password") },
                text = {
                    Column {
                        TextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = { Text("Confirm New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = errorMessage.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when {
                                currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty() -> {
                                    errorMessage = "All fields are required"
                                }
                                newPassword != confirmNewPassword -> {
                                    errorMessage = "Passwords don't match"
                                }
                                newPassword.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                }
                                else -> {
                                    profileViewModel.changePassword(
                                        currentPassword = currentPassword,
                                        newPassword = newPassword,
                                        onSuccess = {
                                            showChangePasswordDialog = false
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmNewPassword = ""
                                            errorMessage = ""
                                            Toast.makeText(
                                                context,
                                                "Password changed successfully",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        },
                                        onFailure = { message ->
                                            errorMessage = message
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        Text("Change Password")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showChangePasswordDialog = false
                            currentPassword = ""
                            newPassword = ""
                            confirmNewPassword = ""
                            errorMessage = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Password Dialog
        if (showDeletePasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeletePasswordDialog = false
                    currentPassword = ""
                    errorMessage = ""
                },
                title = { Text(text = "Delete Password") },
                text = {
                    Column {
                        Text("Are you sure you want to delete your password?")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Note: You won't be able to sign in with email/password after this.")
                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentUser?.providerData?.size == 1) {
                            Text(
                                text = "Warning: This is your only sign-in method. You must add another sign-in method first.",
                                color = Color.Red
                            )
                        } else {
                            TextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Current Password (for verification)") },
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
                            if (currentUser?.providerData?.size == 1) {
                                errorMessage = "You must have another sign-in method before deleting your password"
                                return@Button
                            }

                            if (currentPassword.isEmpty()) {
                                errorMessage = "Please enter your current password"
                                return@Button
                            }

                            profileViewModel.deletePassword(
                                currentPassword = currentPassword,
                                onSuccess = {
                                    showDeletePasswordDialog = false
                                    currentPassword = ""
                                    errorMessage = ""
                                    Toast.makeText(
                                        context,
                                        "Password deleted successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                onFailure = { message ->
                                    errorMessage = message
                                }
                            )
                        },
                        enabled = currentUser?.providerData?.size ?: 0 > 1
                    ) {
                        Text("Delete Password")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showDeletePasswordDialog = false
                            currentPassword = ""
                            errorMessage = ""
                        }
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
                                            }
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
                                                }
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