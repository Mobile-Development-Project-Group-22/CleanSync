package com.example.cleansync.ui.profile


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme


import androidx.compose.material3.Text
import androidx.compose.material3.TextField


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.ui.unit.dp
@Composable
fun DeletePasswordDialog(
    currentPassword: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    showWarning: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Password") },
        text = {
            Column {
                Text("Are you sure you want to delete your password?")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Note: You won't be able to sign in with email/password after this.")

                Spacer(modifier = Modifier.height(16.dp))
                if (showWarning) {
                    Text(
                        text = "Warning: This is your only sign-in method. You must add another sign-in method first.",
                        color = Color.Red
                    )
                } else {
                    TextField(
                        value = currentPassword,
                        onValueChange = onPasswordChange,
                        label = { Text("Current Password (for verification)") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = errorMessage.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(text = errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !showWarning,

            ) {
                Text("Delete Password")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
