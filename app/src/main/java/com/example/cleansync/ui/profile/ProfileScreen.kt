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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.example.cleansync.R
import com.example.cleansync.navigation.Screen



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

    // Language preference state
    var selectedLanguage by remember { mutableStateOf("en") }  // Default language is English
    val languageOptions = listOf("English", "Spanish", "French")
    var expanded by remember { mutableStateOf(false) } // For managing the dropdown menu expansion state

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

        // TopAppBar with fixed height and no padding
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

        // Add a spacer to maintain proper gap between TopAppBar and content

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp,
                    end = 16.dp,
                     top = 54.dp
                ),  // Adjust padding for the rest of the content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp)) // Space for TopAppBar

            // Profile Image
            val imagePainter = rememberAsyncImagePainter(
                model = currentUser?.photoUrl ?: Uri.parse("https://example.com/default_profile.png"),
            )
            Image(
                painter = imagePainter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = currentUser?.displayName ?: "No Name",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = currentUser?.email ?: "No Email",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Language Preferences Section
            Text(text = "Language Preferences",
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
                    // Handle the button click, for example, show a message
                    Toast.makeText(context, "Change language button clicked", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notification Preferences Section to the left side
            Text(text = "Notification Preferences",
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

        // Handle Success/Error States
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
                                profileViewModel.updateUserProfile(newDisplayName, null)
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
                                profileViewModel.deleteUser(currentPassword)
                                showDeleteDialog = false
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
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}



@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onButtonClicked: () -> Unit // Added a lambda for the button click event
) {
    val languageOptions = listOf("English", "Spanish", "French")
    val languageMap = mapOf("English" to "en", "Spanish" to "es", "French" to "fr")

    Column {
        // Row to place dropdown text on the left and button on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween, // Space out elements
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) } // Toggle dropdown visibility
                .padding(8.dp)
        ) {
            // Text showing selected language
            Text(
                text = languageOptions.find { languageMap[it] == selectedLanguage } ?: "Select Language",
                modifier = Modifier.weight(1f) // Take available space
            )

            // Button on the right side of the row
            Button(
                onClick = onButtonClicked, // Handle button click
                modifier = Modifier.padding(start = 8.dp) // Optional padding
            ) {
                Text(text = "Change")
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            languageOptions.forEach { language ->
                DropdownMenuItem(
                    onClick = {
                        onLanguageSelected(languageMap[language] ?: "en")
                        onExpandedChange(false) // Close dropdown after selection
                    },
                    text = { Text(language) },
                    trailingIcon = {
                        if (selectedLanguage == languageMap[language]) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun NotificationPreferences(
    emailNotificationsEnabled: Boolean,
    pushNotificationsEnabled: Boolean,
    onEmailNotificationsChanged: (Boolean) -> Unit,
    onPushNotificationsChanged: (Boolean) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Email Notifications", modifier = Modifier.weight(1f))
            Switch(
                checked = emailNotificationsEnabled,
                onCheckedChange = onEmailNotificationsChanged
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Push Notifications", modifier = Modifier.weight(1f))
            Switch(
                checked = pushNotificationsEnabled,
                onCheckedChange = onPushNotificationsChanged
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
