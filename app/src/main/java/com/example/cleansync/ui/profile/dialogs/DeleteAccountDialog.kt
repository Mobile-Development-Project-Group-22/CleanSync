package com.example.cleansync.ui.profile.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun DeleteAccountDialog(
    isGoogleProvider: Boolean,
    currentPassword: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String,
    isLoading: Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Confirm Account Deletion") },
        text = {
            Column {
                if (isGoogleProvider) {
                    Text("You are signed in with Google. Please confirm to delete your account.")
                } else {
                    Text("Please enter your current password to delete your account.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = onPasswordChange,
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = errorMessage.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.semantics { contentDescription = "Confirm Delete Account" }
            ) {
                Text("Delete Account")
            }

        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.semantics { contentDescription = "Cancel Delete Account" }
            ) {
                Text("Cancel")
            }
        }
    )
}
