package com.example.cleansync.ui.profile.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShowImagePickerDialog(
    onDismiss: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onResetToDefault: () -> Unit,
    showCameraPermissionDialog: Boolean
) {
    if (showCameraPermissionDialog) {
        ShowCameraPermissionDialog(onDismiss)
    } else {
        androidx.compose.material3.BasicAlertDialog(
            onDismissRequest = onDismiss,
            content = {
                Column {
                    Text("Do you want to reset your profile picture, take a photo with the camera, or choose a new one?")
                    Row {
                        Button(onClick = { onResetToDefault() }) {
                            Text("Reset to Default")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onChooseFromGallery() }) {
                            Text("Choose from Gallery")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onTakePhoto() }) {
                        Text("Take a Photo")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowCameraPermissionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        content = {
            Column {
                Text("This app needs camera permission to take a photo.")
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { /* Request permission */ }) {
                        Text("Grant Permission")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    )
}
