package com.example.cleansync.ui.profile.profileItems

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.cleansync.ui.profile.ProfileViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseUser
import java.io.File

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfilePictureSection(
    profileViewModel: ProfileViewModel,
    user: FirebaseUser?
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Function to create an image Uri for storing the captured image using MediaStore
    fun createImageUri(contentResolver: ContentResolver): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "profile_pic_${System.currentTimeMillis()}")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    // Image picker launcher (Gallery)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                selectedImageUri = it
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

    // Camera launcher using MediaStore for Android 13+ (API level 33+)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && selectedImageUri != null) {
                isUploading = true
                profileViewModel.uploadProfileImage(
                    uri = selectedImageUri!!,
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

    // Handle camera permission check for Android 13+
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // Reset to default functionality
    fun resetToDefault() {
        selectedImageUri =
            "https://static.vecteezy.com/system/resources/previews/005/544/718/original/profile-icon-design-free-vector.jpg".toUri()
        profileViewModel.updateProfilePicture(selectedImageUri!!) // Make sure your view model handles this.
        Toast.makeText(context, "Profile picture reset to default", Toast.LENGTH_SHORT).show()
    }

    // Image painter for displaying the profile image or selected preview
    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(
                data = selectedImageUri ?: user?.photoUrl
                ?: null
            )
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    )

    // Image selection dialog for gallery, camera, and reset to default
    if (showImagePickerDialog) {
        BasicAlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            content = {
                Column {
                    Text("Do you want to reset your profile picture, take a photo with the camera, or choose a new one?")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showImagePickerDialog = false
                                resetToDefault()
                            }
                        ) {
                            Text("Reset to Default")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showImagePickerDialog = false
                                imagePickerLauncher.launch("image/*") // Open gallery for photo selection
                            }
                        ) {
                            Text("Choose from Gallery")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    showImagePickerDialog = false
                                    // Create a file in shared storage for the image to be taken.
                                    val contentResolver = context.contentResolver
                                    val imageUri = createImageUri(contentResolver)
                                    selectedImageUri = imageUri
                                    cameraLauncher.launch(imageUri)
                                } else {
                                    // Request camera permission if not granted
                                    showCameraPermissionDialog = true
                                }
                            }
                        ) {
                            Text("Take a Photo")
                        }
                    }
                }
            }
        )
    }

    // Handle camera permission request
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            content = {
                Column {
                    Text("This app needs camera permission to take a photo.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(
                            onClick = {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        ) {
                            Text("Grant Permission")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showCameraPermissionDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        )
    }



    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 12.dp, bottom = 1.dp)
            .wrapContentHeight(align = Alignment.CenterVertically)
            .clickable { showImagePickerDialog = true } // Show dialog when clicked
    ) {
        // Smooth transition for profile image
        AnimatedVisibility(
            visible = !isUploading,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)) + slideInVertically(),
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) + slideOutVertically()
        ) {
            Image(
                painter = imagePainter,
                contentDescription = "Profile Picture Preview",
                modifier = Modifier
                    .size(120.dp) // You can adjust this size based on your requirements
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop // Maintains aspect ratio by cropping the image if necessary
            )
        }

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