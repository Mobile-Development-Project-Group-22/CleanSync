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
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.cleansync.ui.profile.dialogs.UpdateNameDialog
import com.example.cleansync.utils.NotificationUtils
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfilePictureSection(
    profileViewModel: ProfileViewModel,
    user: FirebaseUser?
) {
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                // Preview the selected image
                selectedImageUri = it
                // Show loading spinner during upload
                isUploading = true
                profileViewModel.uploadProfileImage(
                    uri = it,
                    onSuccess = {
                        isUploading = false
                        Toast.makeText(
                            context,
                            "Profile picture updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onFailure = { error ->
                        isUploading = false
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

    // Image painter for displaying the profile image or selected preview
    val imagePainter = rememberAsyncImagePainter(
        model = selectedImageUri ?: user?.photoUrl ?: "https://static.vecteezy.com/system/resources/previews/005/544/718/original/profile-icon-design-free-vector.jpg".toUri()
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 12.dp, bottom = 1.dp)
            .wrapContentHeight(align = Alignment.CenterVertically)
            .clickable { imagePickerLauncher.launch("image/*") }
    ) {
        // Show image preview if available
        Image(
            painter = imagePainter,
            contentDescription = "Profile Picture Preview",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") }
        )

        // Show loading indicator if uploading
        if (isUploading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = user?.displayName ?: "User Name",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable { }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Email and verification status
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

        // Snackbar for status messages
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


