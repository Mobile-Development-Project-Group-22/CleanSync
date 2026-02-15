package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.notifications.NotificationSettingsViewModel
import com.example.cleansync.ui.profile.dialogs.*
import com.example.cleansync.ui.profile.profileItems.ProfilePictureSection
import com.example.cleansync.ui.theme.ThemeMode
import com.google.firebase.auth.GoogleAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    preferencesViewModel: NotificationSettingsViewModel,
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit,
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToContact: () -> Unit,
) {
    val profileState = profileViewModel.profileState.collectAsState().value
    val preferencesState = preferencesViewModel.preferences.collectAsState().value

    val currentUser = profileViewModel.currentUser
    val context = LocalContext.current

    // Dialog and input state
    var showReviewDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showAddPasswordDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showFAQDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        preferencesViewModel.loadPreferences()
    }

    val isGoogleSignIn = currentUser?.providerData?.any {
        it.providerId == GoogleAuthProvider.PROVIDER_ID
    } == true

    val items = listOf(
        "My Bookings" to onNavigateToBookings,
        "Payment Methods" to {},
        "Contact Us" to onNavigateToContact,
        "Review Us" to { showReviewDialog = true },
        "FAQ" to { showFAQDialog = true },
        "Settings" to onNavigateToSettings,
        "Terms & Conditions" to { showTermsDialog = true },
        "Privacy Policy" to { showPrivacyDialog = true },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Profile",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(start = 22.dp)
                                .fillMaxWidth()
                        )
                    }
                },
                actions = {
                    AnimatedContent(
                        targetState = currentThemeMode,
                        label = "ThemeIcon"
                    ) { mode ->
                        IconButton(
                            onClick = {
                                val nextMode = when (mode) {
                                    ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                    ThemeMode.LIGHT -> ThemeMode.DARK
                                    ThemeMode.DARK -> ThemeMode.SYSTEM
                                }
                                onThemeSelected(nextMode)
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            val icon = when (mode) {
                                ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Change Theme"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier.height(80.dp)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (profileState is ProfileState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfilePictureSection(profileViewModel, currentUser)
                    Spacer(modifier = Modifier.height(16.dp))

                    items.forEach { (label, action) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = action,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current
                                )
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Divider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            profileViewModel.signOut()
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Logout", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Password & Delete Dialogs
                if (showChangePasswordDialog) {
                    ChangePasswordDialog(
                        currentPassword, newPassword, confirmNewPassword,
                        onCurrentPasswordChange = { currentPassword = it },
                        onNewPasswordChange = { newPassword = it },
                        onConfirmNewPasswordChange = { confirmNewPassword = it },
                        errorMessage,
                        onDismiss = { showChangePasswordDialog = false },
                        onConfirm = {
                            if (newPassword == confirmNewPassword && newPassword.isNotEmpty()) {
                                profileViewModel.changePassword(
                                    currentPassword, newPassword,
                                    onSuccess = {
                                        Toast.makeText(context, "Password changed successfully", Toast.LENGTH_LONG).show()
                                    },
                                    onFailure = { error -> errorMessage = error }
                                )
                                showChangePasswordDialog = false
                            } else errorMessage = "Passwords do not match or are empty"
                        }
                    )
                }

                if (showDeleteDialog) {
                    DeleteAccountDialog(
                        isGoogleProvider = isGoogleSignIn,
                        currentPassword,
                        onPasswordChange = { currentPassword = it },
                        errorMessage,
                        onDismiss = { showDeleteDialog = false },
                        onConfirm = {
                            profileViewModel.reAuthenticateAndDeleteUser(
                                currentPassword,
                                onSuccess = {
                                    Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
                                    onNavigateToLogin()
                                },
                                onFailure = { error -> errorMessage = error },
                                context = context
                            )
                            showDeleteDialog = false
                        },
                        isLoading = CircularProgressIndicator()
                    )
                }

                ReviewUsDialog(showDialog = showReviewDialog, onDismiss = { showReviewDialog = false }) { rating, review ->
                    Toast.makeText(context, "Rating: $rating, Review: $review", Toast.LENGTH_LONG).show()
                }

                FAQDialog(showDialog = showFAQDialog, onDismiss = { showFAQDialog = false })
                TermsAndConditionsDialog(showDialog = showTermsDialog, onDismiss = { showTermsDialog = false })
                PrivacyPolicyDialog(showDialog = showPrivacyDialog, onDismiss = { showPrivacyDialog = false })
            }
        }
    }
}
