package com.example.cleansync.ui.profile.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordDialog(
    onAddPassword: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Password") },
        text = {
            Column {
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = validatePassword(it)
                    },
                    label = { Text("New Password") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Text(
                            text = if (showPassword) "Hide" else "Show",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showPassword = !showPassword }
                        )
                    },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage.isNotEmpty()) {
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
                onClick = {
                    if (errorMessage.isEmpty()) {
                        onAddPassword(password)
                    }
                },
                enabled = password.isNotBlank() && errorMessage.isEmpty()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


fun validatePassword(password: String): String {
    return when {
        password.length < 6 -> "Password must be at least 6 characters"
        !password.any { it.isDigit() } -> "Include at least one number"
        !password.any { it.isLetter() } -> "Include at least one letter"
        else -> ""
    }
}

