package com.example.cleansync.ui.profile.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ChangePasswordDialog(
    currentPassword: String,
    newPassword: String,
    confirmNewPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmNewPasswordChange: (String) -> Unit,
    errorMessage: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusRequesterCurrent = remember { FocusRequester() }
    val focusRequesterNew = remember { FocusRequester() }
    val focusRequesterConfirm = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        focusRequesterCurrent.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Password",
                color = colorScheme.onSurface,
                modifier = Modifier.semantics { contentDescription = "Change Password Title" }
            )
        },
        text = {
            Column {
                PasswordTextField(
                    label = "Current Password",
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    isVisible = showCurrentPassword,
                    onVisibilityToggle = { showCurrentPassword = it },
                    focusRequester = focusRequesterCurrent,
                    imeAction = ImeAction.Next,
                    onNext = { focusRequesterNew.requestFocus() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordTextField(
                    label = "New Password",
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    isVisible = showNewPassword,
                    onVisibilityToggle = { showNewPassword = it },
                    focusRequester = focusRequesterNew,
                    imeAction = ImeAction.Next,
                    onNext = { focusRequesterConfirm.requestFocus() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordTextField(
                    label = "Confirm New Password",
                    value = confirmNewPassword,
                    onValueChange = onConfirmNewPasswordChange,
                    isVisible = showConfirmPassword,
                    onVisibilityToggle = { showConfirmPassword = it },
                    focusRequester = focusRequesterConfirm,
                    imeAction = ImeAction.Done,
                    isError = errorMessage.isNotEmpty(),
                    onDone = {
                        focusManager.clearFocus(force = true)
                        onConfirm()
                    }
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { contentDescription = "Error: $errorMessage" }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                modifier = Modifier.semantics { contentDescription = "Confirm Change Password" }
            ) {
                Text("Change Password")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surface,
                    contentColor = colorScheme.onSurface
                ),
                modifier = Modifier.semantics { contentDescription = "Cancel Change Password" }
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityToggle: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    imeAction: ImeAction,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    isError: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("Enter $label") },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { onVisibilityToggle(!isVisible) }) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isVisible) "Hide $label" else "Show $label",
                    tint = colorScheme.onSurface
                )
            }
        },
        isError = isError,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics { contentDescription = "$label Field" },
        keyboardOptions =
            KeyboardOptions.Default.copy(
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            onDone = { onDone?.invoke() }
        ),
        colors = TextFieldDefaults.colors(

                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface,
                disabledContainerColor = colorScheme.surface,
                cursorColor = colorScheme.primary,

                focusedIndicatorColor = colorScheme.primary,
                unfocusedIndicatorColor = colorScheme.onSurface.copy(alpha = 0.5f),

            )
    )
}
