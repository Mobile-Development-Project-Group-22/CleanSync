package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.notifications.NotificationSettingsViewModel
import com.example.cleansync.ui.profile.dialogs.*
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
    onNavigateToSupport : () -> Unit,
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth().height(70.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfilePictureSection(profileViewModel, currentUser)
            Spacer(modifier = Modifier.height(16.dp))

            val items = listOf(
                "My Bookings" to onNavigateToBookings,
                "Payment Methods" to {},
                "Support" to onNavigateToSupport,
                "Review Us" to { showReviewDialog = true },
                "FAQ" to { showFAQDialog = true },
                "Settings" to onNavigateToSettings,
                "Terms & Conditions" to { showTermsDialog = true },
                "Privacy Policy" to { showPrivacyDialog = true },
            )

            items.forEach { (label, action) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { action() }
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
                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    profileViewModel.signOut()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
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

        LaunchedEffect(profileState) {
            when (profileState) {
                is ProfileState.Error -> Toast.makeText(context, profileState.message, Toast.LENGTH_LONG).show()
                is ProfileState.Success -> if (profileState.user == null) onNavigateToLogin() else Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_LONG).show()
                else -> {}
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

@Composable
fun RatingBar(rating: Float, onRatingChanged: (Float) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp).clickable { onRatingChanged(i.toFloat()) }
            )
        }
    }
}
