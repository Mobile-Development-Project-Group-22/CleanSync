package com.example.cleansync.ui.booking

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.cleansync.model.Booking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EditBookingDialog(
    booking: Booking,
    newDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Booking") },
        text = { Text("Change booking to: ${newDateTime.format(formatter)}?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
