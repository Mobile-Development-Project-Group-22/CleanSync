package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.notifications.NotificationSettingsViewModel
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

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
    var showUpdateProfileDialog by remember { mutableStateOf(false) }
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
fun ReviewUsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (rating: Float, review: String) -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Rate Us",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Rating Bar
                    var rating by remember { mutableStateOf(0f) }
                    RatingBar(
                        rating = rating,
                        onRatingChanged = { rating = it }
                    )

                    // Review Input
                    var reviewText by remember { mutableStateOf("") }
                    TextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Write your review") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Submit Button
                    Button(
                        onClick = {
                            onSubmit(rating, reviewText)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

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

@Composable
fun FAQDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .heightIn(min = 300.dp, max = 800.dp) // Adjust height range
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Title
                    Text(
                        text = "Frequently Asked Questions",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Scrollable FAQ Content
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(faqList) { faq ->
                            FAQItem(question = faq.first, answer = faq.second)
                            Divider(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Q: $question",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "A: $answer",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Sample FAQ Data
val faqList = listOf(
    "What is CleanSync?" to "CleanSync is a platform for managing bookings and notifications.",
    "How do I reset my password?" to "Go to the login screen and click on 'Forgot Password'.",
    "How can I contact support?" to "You can contact support via the 'Support' section in the app."
)

//for terms and conditions
@Composable
fun TermsAndConditionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f) // Cover 95% of the screen width
                    .fillMaxHeight(0.9f) // Cover 90% of the screen height
                    .padding(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title
                        Text(
                            text = "Terms and Conditions",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Scrollable Content
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Text(
                                    text = termsAndConditionsParagraph,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Close Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

// Sample Terms and Conditions Paragraph
val termsAndConditionsParagraph = """
    These Terms and Conditions ("Terms") govern your use of the CleanSync mobile app and services ("Service"). By using CleanSync, you agree to comply with these Terms. If you do not agree to these Terms, please do not use the Service.

1. Services Provided
CleanSync offers carpet cleaning services through a mobile app, including pick-up, cleaning, and delivery of carpets. Additional services such as stain removal and expedited cleaning may be offered as premium services.

2. Account Registration
To use the CleanSync Service, you must create an account. You agree to provide accurate, complete, and current information during registration and keep your account information up to date.

3. Booking and Payment
You agree to book services through the app and provide accurate information regarding the size and condition of the carpet. You will be charged based on the size and type of cleaning required. Payments will be processed through secure payment methods available within the app.

4. Cancellations and Refunds
You may cancel or reschedule your booking up to 24 hours before the scheduled pick-up. Cancellations or changes made less than 24 hours in advance may incur a cancellation fee. If the service is unsatisfactory, please contact customer support within 24 hours of receiving your cleaned carpet for a resolution, which may include a partial or full refund, at CleanSync's discretion.

5. Customer Obligations
You agree to provide accurate information regarding the size and condition of your carpet. You are responsible for ensuring that the carpet is free from hazardous substances or items that may cause harm or damage during cleaning. CleanSync is not liable for any damage caused by undisclosed issues such as excessive wear, embedded dirt, or damage due to neglect.

6. Intellectual Property
CleanSync’s app and all related content, including trademarks, logos, and service marks, are owned by CleanSync and protected by copyright and intellectual property laws. You may not reproduce, distribute, or modify any of CleanSync’s content without prior written consent.

7. Limitation of Liability
CleanSync’s liability is limited to the amount paid for the specific service rendered. CleanSync is not responsible for any indirect, incidental, or consequential damages, including damage to property or loss of use.

8. Privacy
By using the Service, you agree to our Privacy Policy (see below). CleanSync will handle your personal data with care and in compliance with applicable privacy laws.

9. Amendments
CleanSync reserves the right to update these Terms at any time. Any changes will be effective once posted in the app. Continued use of the Service constitutes acceptance of the updated Terms.

10. Governing Law
These Terms are governed by and construed in accordance with the laws of Finland. Any disputes shall be resolved in the courts of Finland.
""".trimIndent()

@Composable
fun PrivacyPolicyDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f) // Cover 95% of the screen width
                    .fillMaxHeight(0.9f) // Cover 90% of the screen height
                    .padding(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Scrollable Content
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Text(
                                    text = privacyPolicyText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Close Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

//Privacy policy text
val privacyPolicyText = """
    CleanSync values your privacy and is committed to protecting your personal information. This Privacy Policy explains how CleanSync collects, uses, and protects your information when you use our mobile app and services.

1. Information We Collect
When you use CleanSync, we may collect the following types of information:

Personal Information: Name, email address, phone number, and payment information.

Usage Data: Information about how you use our app, including device information, IP address, and app interaction data.

Payment Information: Payment details collected during transactions processed through our app.

2. How We Use Your Information
We use your personal information for the following purposes:

To provide and manage the carpet cleaning services.

To process payments securely.

To communicate with you about your orders and provide customer support.

To improve the app and enhance user experience.

To comply with legal and regulatory obligations.

3. Data Sharing
We do not sell or rent your personal information to third parties. We may share your information in the following circumstances:

Service Providers: We may share data with trusted third-party vendors who assist in the operation of the app (e.g., payment processors, delivery services).

Legal Compliance: We may disclose your information if required by law, such as in response to a subpoena or to comply with legal processes.

4. Data Retention
We retain your personal data only for as long as necessary to fulfill the purposes outlined in this Privacy Policy, including for legal, accounting, or reporting purposes.

5. Security
We use industry-standard security measures, including encryption and secure data storage, to protect your personal information. However, no method of transmission over the internet or electronic storage is 100% secure, and we cannot guarantee absolute security.

6. Your Rights
You have the right to:

Access and update your personal information through your CleanSync account settings.

Request the deletion of your personal data, subject to legal requirements.

Opt-out of marketing communications at any time.

7. Children's Privacy
Our services are not directed to children under the age of 16. We do not knowingly collect personal information from children. If you believe we have inadvertently collected such data, please contact us, and we will take steps to delete it.

8. Changes to This Privacy Policy
CleanSync reserves the right to update this Privacy Policy. Any changes will be posted in the app, and the updated policy will take effect immediately upon posting. Your continued use of the app after such updates signifies your acceptance of the revised policy.

9. Contact Us
If you have any questions or concerns about our Privacy Policy or data practices, please contact us at:contact@cleansync.com
""".trimIndent()