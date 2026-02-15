package com.example.cleansync.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.notifications.NotificationSettingsViewModel
import com.example.cleansync.ui.profile.dialogs.ChangePasswordDialog
import com.example.cleansync.ui.profile.dialogs.DeleteAccountDialog
import com.google.firebase.auth.GoogleAuthProvider
import com.example.cleansync.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    preferencesViewModel: NotificationSettingsViewModel,
    profileViewModel: ProfileViewModel,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val preferencesState by preferencesViewModel.preferences.collectAsState()
    val currentUser = profileViewModel.currentUser
    val isGoogleSignIn = currentUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true
    val hasPasswordProvider = currentUser?.providerData?.any { it.providerId == "password" } == true

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val verticalScroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            // Password Management
            if (hasPasswordProvider) {
                SettingSection {
                    Button(onClick = { showChangePasswordDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Change Password")
                    }
                }
            }

            // Theme Selection
            SettingSection(title = "Appearance") {
                Column {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onThemeSelected(mode) }
                        ) {
                            RadioButton(
                                selected = currentThemeMode == mode,
                                onClick = { onThemeSelected(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = when (mode) {
                                ThemeMode.SYSTEM -> "System Default"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            })
                        }
                    }
                }
            }

            // Language Settings (example)
            SettingSection(title = "Language") {
                Text("English (default)") // Replace with a full language selector if needed
            }

            // Notification Settings
            SettingSection(title = "Notifications") {
                NotificationPreferencesScreen(
                    preferences = preferencesState,
                    onEmailChange = {
                        preferencesViewModel.updatePreference(email = it)
                        Toast.makeText(context, if (it) "Email notifications enabled" else "disabled", Toast.LENGTH_SHORT).show()
                    },
                    onPushChange = {
                        preferencesViewModel.updatePreference(push = it)
                        Toast.makeText(context, if (it) "Push notifications enabled" else "disabled", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Danger Zone
            SettingSection {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account")
                }
            }
        }
    }

    // Dialogs
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmNewPassword = confirmNewPassword,
            onCurrentPasswordChange = { currentPassword = it },
            onNewPasswordChange = { newPassword = it },
            onConfirmNewPasswordChange = { confirmNewPassword = it },
            errorMessage = errorMessage,
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = {
                if (newPassword == confirmNewPassword && newPassword.isNotBlank()) {
                    profileViewModel.changePassword(
                        currentPassword, newPassword,
                        onSuccess = {
                            Toast.makeText(context, "Password changed", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error -> errorMessage = error }
                    )
                    showChangePasswordDialog = false
                } else {
                    errorMessage = "Passwords do not match"
                }
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            isGoogleProvider = isGoogleSignIn,
            currentPassword = currentPassword,
            onPasswordChange = { currentPassword = it },
            errorMessage = errorMessage,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                profileViewModel.reAuthenticateAndDeleteUser(
                    password = currentPassword,
                    onSuccess = {
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                        onNavigateToLogin()
                    },
                    context = context,
                    onFailure = { error -> errorMessage = error }
                )
            },
            isLoading = CircularProgressIndicator()
        )
    }
}

@Composable
fun SettingSection(
    title: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
    ) {
        title?.let {
            Text(text = it, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        }
        content()
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
    }
}
