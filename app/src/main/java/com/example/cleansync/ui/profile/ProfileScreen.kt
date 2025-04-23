package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.cleansync.ui.profile.dialogs.FAQDialog
import com.example.cleansync.ui.profile.dialogs.PrivacyPolicyDialog
import com.example.cleansync.ui.profile.dialogs.ReviewUsDialog
import com.example.cleansync.ui.profile.dialogs.TermsAndConditionsDialog
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
) {
    val profileState = profileViewModel.profileState.collectAsState().value
    val preferencesState = preferencesViewModel.preferences.collectAsState().value

    val currentUser = profileViewModel.currentUser
    val context = LocalContext.current

    var showReviewDialog by remember { mutableStateOf(false) }

    // States for dialogs and preferences
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeletePasswordDialog by remember { mutableStateOf(false) }
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

    // Observe changes in dark mode preference
     LaunchedEffect(shouldSaveTheme) {
        if (shouldSaveTheme) {
            ThemePreferenceManager.saveDarkMode(context, isDarkMode)
            shouldSaveTheme = false
        }
    }


    // Check if user has password provider
    val hasPasswordProvider = currentUser?.providerData?.any {
        it.providerId == "password"
    } == true

    LaunchedEffect(Unit) {
        // Load user preferences
        preferencesViewModel.loadPreferences()
    }

    // Check if user is logged in via Google
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
            ProfilePictureSection(
                profileViewModel = profileViewModel,
                user = currentUser,
            )

            Spacer(modifier = Modifier.height(16.dp))



            // Newly added sections
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToBookings()
                    }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Bookings",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { // 👈 Navigation works here
                    }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Methods",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // 👈 Navigation works here
                    }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Support",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showReviewDialog = true // Show the Review Us dialog
                    }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Review Us",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFAQDialog = true }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FAQ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)
            var isSettingsExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSettingsExpanded = !isSettingsExpanded }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isSettingsExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowForward,
                    contentDescription = "Expand/Collapse Settings"
                )
            }

            if (isSettingsExpanded) {
                // Add settings options here (e.g., Change Password, Dark Mode, etc.)
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



                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))


                // Dark Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            isDarkMode = it
                            onThemeToggle(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.background,
                            uncheckedThumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language Selector
                LanguageSelector(
                    selectedLanguage = "en",
                    onLanguageSelected = { language ->
                        Toast.makeText(
                            context,
                            "Language changed to $language",
                            Toast.LENGTH_SHORT
                        ).show()
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


                // Notification Preferences
                NotificationPreferencesScreen(
                    preferences = preferencesState,
                    onEmailChange = { email ->
                        preferencesViewModel.updatePreference(email = email)
                        Toast.makeText(
                            context,
                            if (email) "Email notifications enabled" else "Email notifications disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onPushChange = { push ->
                        preferencesViewModel.updatePreference(push = push)
                        Toast.makeText(
                            context,
                            if (push) "Push notifications enabled" else "Push notifications disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

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
                Spacer(modifier = Modifier.height(16.dp))
            }
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showTermsDialog = true
                    }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terms & Conditions ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showPrivacyDialog = true
                    }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(16.dp))
            // Logout Button
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
                Text(text = "Logout", style = MaterialTheme.typography.labelLarge)
            }
        }


        LaunchedEffect(profileState) {
                when (profileState) {
                    is ProfileState.Error -> {
                        Toast.makeText(context, profileState.message, Toast.LENGTH_LONG).show()
                    }

                    is ProfileState.Success -> {
                        if (profileState.user == null) {
                            onNavigateToLogin()
                        } else {
                            Toast.makeText(
                                context,
                                "Profile updated successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    else -> {
                        // Do nothing
                    }
                }
            }

            // Dialogs for password change, delete, etc.

            // Change Password Dialog
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
                        if (newPassword == confirmNewPassword && newPassword.isNotEmpty()) {
                            profileViewModel.changePassword(
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Password changed successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                onFailure = { error ->
                                    errorMessage = error
                                }
                            )
                            showChangePasswordDialog = false
                        } else {
                            errorMessage = "Passwords do not match or are empty"
                        }
                    }
                )
            }

            // Delete Account Dialog
            if (showDeleteDialog) {
                DeleteAccountDialog(
                    isGoogleProvider = currentUser?.providerData?.any {
                        it.providerId == GoogleAuthProvider.PROVIDER_ID
                    } == true,
                    currentPassword = currentPassword,
                    onPasswordChange = { currentPassword = it },
                    errorMessage = errorMessage,
                    onDismiss = { showDeleteDialog = false },
                    onConfirm = {
                        profileViewModel.reAuthenticateAndDeleteUser(
                            password = currentPassword,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Account deleted successfully",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            context = context,
                            onFailure = { error ->
                                errorMessage = error
                            }
                        )
                        showDeleteDialog = false
                        onNavigateToLogin()
                    },
                    isLoading = CircularProgressIndicator()
                )
            }
            ReviewUsDialog(
                showDialog = showReviewDialog,
                onDismiss = { showReviewDialog = false },
                onSubmit = { rating, review ->
                    // Handle the submitted rating and review
                    Toast.makeText(context, "Rating: $rating, Review: $review", Toast.LENGTH_LONG).show()
                }
            )
        FAQDialog(
            showDialog = showFAQDialog,
            onDismiss = { showFAQDialog = false }
        )

        TermsAndConditionsDialog(
            showDialog = showTermsDialog,
            onDismiss = { showTermsDialog = false }
        )

        PrivacyPolicyDialog(
            showDialog = showPrivacyDialog,
            onDismiss = { showPrivacyDialog = false }
        )

        }
    }


// Review Us Dialog


@Composable
fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChanged(i.toFloat()) }
            )
        }
    }
}

//For FAQ Section


