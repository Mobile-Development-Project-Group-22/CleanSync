package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.notifications.NotificationSettingsViewModel
import com.example.cleansync.ui.profile.dialogs.*
import com.example.cleansync.ui.profile.profileItems.ProfilePictureSection
import com.google.firebase.auth.GoogleAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    preferencesViewModel: NotificationSettingsViewModel,
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToContact: () -> Unit,
) {
    val profileState = profileViewModel.profileState.collectAsState().value
    val preferencesState = preferencesViewModel.preferences.collectAsState().value

    val currentUser = profileViewModel.currentUser
    val context = LocalContext.current

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

    val darkModeFlow = remember { ThemePreferenceManager.getDarkMode(context) }
    val darkModePref by darkModeFlow.collectAsState(initial = false)

    var isDarkMode by remember { mutableStateOf(darkModePref) }
    var shouldSaveTheme by remember { mutableStateOf(false) }

    var showFAQDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(shouldSaveTheme) {
        if (shouldSaveTheme) {
            ThemePreferenceManager.saveDarkMode(context, isDarkMode)
            shouldSaveTheme = false
        }
    }

    val hasPasswordProvider = currentUser?.providerData?.any {
        it.providerId == "password"
    } == true

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
                            modifier = Modifier.align(Alignment.Center)
                                .padding(start = 22.dp) // Padding around the title
                                .fillMaxWidth(), // Fill the width to center the text
                        )
                    }
                },
                actions = {
                    AnimatedContent(targetState = isDarkMode, label = "ThemeIcon") { dark ->
                        IconButton(onClick = {
                            isDarkMode = !isDarkMode
                            shouldSaveTheme = true
                            onThemeToggle(isDarkMode)
                        },
                            modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                            Icon(
                                imageVector = if (dark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Theme"
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
                    modifier = Modifier.fillMaxSize().background(Color.White),
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
                                    indication = ripple(
                                        bounded = true,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    ),
                                    interactionSource = remember { MutableInteractionSource() }
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
                        HorizontalDivider(
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
                            .fillMaxWidth()
                            .animateContentSize(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Logout", style = MaterialTheme.typography.labelLarge)
                    }
                }

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