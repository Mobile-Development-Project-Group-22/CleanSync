package com.example.cleansync.ui.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.notifications.NotificationSettingsViewModel
import com.example.cleansync.ui.profile.dialogs.ChangePasswordDialog
import com.example.cleansync.ui.profile.dialogs.DeleteAccountDialog
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    preferencesViewModel: NotificationSettingsViewModel,
    profileViewModel: ProfileViewModel,
    onThemeToggle: (Boolean) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val preferencesState = preferencesViewModel.preferences.collectAsState().value
    val currentUser = profileViewModel.currentUser
    val isGoogleSignIn = currentUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true
    val hasPasswordProvider = currentUser?.providerData?.any { it.providerId == "password" } == true

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }
    var shouldSaveTheme by remember { mutableStateOf(false) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val darkModeFlow = remember { ThemePreferenceManager.getDarkMode(context) }
    val darkModePref by darkModeFlow.collectAsState(initial = false)
    val verticalScroll = rememberScrollState()

    LaunchedEffect(shouldSaveTheme) {
        if (shouldSaveTheme) {
            ThemePreferenceManager.saveDarkMode(context, isDarkMode)
            shouldSaveTheme = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
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

            // Password Management Section
            if (hasPasswordProvider) {
                SettingSection {
                    Button(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Password")
                    }
                }
            }

            // Appearance Settings
            SettingSection(title = "Appearance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            isDarkMode = it
                            onThemeToggle(it)
                            shouldSaveTheme = true
                        }
                    )
                }
            }

            // Language Settings
            SettingSection(title = "Language") {
                LanguageSelector(
                    selectedLanguage = "en",
                    onLanguageSelected = {
                        Toast.makeText(context, "Language changed to $it", Toast.LENGTH_SHORT).show()
                    },
                    expanded = false,
                    onExpandedChange = {},
                    onButtonClicked = {
                        Toast.makeText(context, "Language changed to English", Toast.LENGTH_SHORT).show()
                    }
                )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        content()
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
    }
}


