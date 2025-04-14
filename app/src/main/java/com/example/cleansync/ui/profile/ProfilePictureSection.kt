package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.example.cleansync.utils.NotificationUtils
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfilePictureSection(
    profileViewModel: ProfileViewModel,
    user: FirebaseUser?
) {
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // For name dialog
    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(user?.displayName ?: "") }
    var nameError by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                selectedImageUri = it
                profileViewModel.uploadProfileImage(
                    uri = it,
                    onSuccess = {

                        Toast.makeText(
                            context,
                            "Profile picture updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            context,
                            "Failed to update profile picture: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

            }
        }
    )

    val imagePainter = rememberAsyncImagePainter(
        model = selectedImageUri ?: user?.photoUrl ?: "https://static.vecteezy.com/system/resources/previews/005/544/718/original/profile-icon-design-free-vector.jpg".toUri()
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = imagePainter,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Clickable Name to show dialog
        Text(
            text = user?.displayName ?: "User Name",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { showNameDialog = true }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Text(
                text = user?.email ?: "No Email",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (profileViewModel.isEmailVerified) "Verified" else "Not Verified",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = if (profileViewModel.isEmailVerified) Color.Green else Color.Red
            )
        }

        // Snackbar
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(top = 16.dp)
        )
    }

    // Show dialog when triggered
    if (showNameDialog) {
        UpdateNameDialog(
            newDisplayName = nameInput,
            onNameChange = {
                nameInput = it
                Toast.makeText(
                    context,
                    "Name updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showNameDialog = false },
            onConfirm = {
                if (nameInput.isBlank()) {
                    nameError = "Name cannot be empty"
                } else {
                    profileViewModel.updateDisplayName(nameInput)
                    showNameDialog = false
                    NotificationUtils.sendCustomNotification(
                        context = context,
                        title = "Profile Update",
                        message = "Your display name has been updated to $nameInput"
                    )
                    Toast.makeText(
                        context,
                        "Name updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    nameError = ""
                }
            },
            errorMessage = nameError

        )
    }
}


