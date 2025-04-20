package com.example.cleansync.ui.booking

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun CancelBookingDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Booking") },
        text = { Text("Are you sure you want to cancel this booking?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}
