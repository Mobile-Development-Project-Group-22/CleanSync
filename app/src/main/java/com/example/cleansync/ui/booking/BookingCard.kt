package com.example.cleansync.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.cleansync.model.Booking
import com.example.cleansync.utils.NotificationScheduler
import com.firebase.ui.auth.BuildConfig

import java.util.*

@Composable
fun BookingCard(
    booking: Booking,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onShowExplanation: () -> Unit
) {
    var showDebugDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onExpandToggle(); onShowExplanation() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.bookingDateTime,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = booking.streetAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            BookingProgressBar(progressStage = booking.progressStage)

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Details in a clean grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoRow(icon = "👤", label = "Name", value = booking.name)
                    InfoRow(icon = "📧", label = "Email", value = booking.email)
                    InfoRow(icon = "📞", label = "Phone", value = booking.phoneNumber)
                    InfoRow(icon = "📍", label = "City", value = "${booking.city}, ${booking.postalCode}")
                    InfoRow(icon = "📏", label = "Size", value = "${booking.length}m × ${booking.width}m")
                    InfoRow(icon = "💶", label = "Total", value = "€${"%.2f".format(booking.totalPrice)}")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel")
                    }

                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = { showDebugDialog = true }) {
                            Text("🔧", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    if (showDebugDialog) {
        NotificationDebugDialog(onDismiss = { showDebugDialog = false })
    }
}

@Composable
fun InfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
@Composable
fun BookingProgressBar(progressStage: String) {
    val stages = listOf("Booked", "Collected", "Cleaned", "Returned")
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            val isCompleted = getProgressStageColor(progressStage, index) == "completed"
            val circleColor = if (isCompleted) primaryColor else Color.Gray.copy(alpha = 0.4f)

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = circleColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stage.first().toString(),
                    color = if (isCompleted) Color.White else Color.Gray,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (index < stages.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            color = if (getProgressStageColor(progressStage, index + 1) == "completed") 
                                primaryColor else Color.Gray.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }
    }
}


private fun getProgressStageColor(progressStage: String, index: Int): String {
    return when (index) {
        0 -> if (progressStage == "booked" || progressStage == "collected" || progressStage == "cleaned" || progressStage == "returned") "completed" else "incomplete"
        1 -> if (progressStage == "collected" || progressStage == "cleaned" || progressStage == "returned") "completed" else "incomplete"
        2 -> if (progressStage == "cleaned" || progressStage == "returned") "completed" else "incomplete"
        3 -> if (progressStage == "returned") "completed" else "incomplete"
        else -> "incomplete"
    }
}



@Composable
fun NotificationDebugDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var workInfos by remember { mutableStateOf<List<WorkInfo>>(emptyList()) }
    var filterDate by remember { mutableStateOf("") }

    // Fetch workInfo from WorkManager when the dialog opens
    LaunchedEffect(Unit) {
        workInfos = WorkManager.getInstance(context)
            .getWorkInfosByTag("booking_reminder")
            .get()  // Use get() to block and return results (for debug)
    }

    // Filter by booking date when user updates filter
    val filteredWorkInfos = if (filterDate.isNotEmpty()) {
        workInfos.filter { workInfo ->
            // Check if the booking time matches the filter (we assume it's a string)
            val workInfoDate = workInfo.tags.find { it.contains(filterDate) }
            workInfoDate != null
        }
    } else {
        workInfos
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("🔧 Scheduled Notifications") },
        text = {
            Column {
                // Filter UI
                TextField(
                    value = filterDate,
                    onValueChange = { filterDate = it },
                    label = { Text("Filter by booking date") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (filteredWorkInfos.isEmpty()) {
                    Text("No scheduled notifications found.")
                } else {
                    filteredWorkInfos.forEach { info ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("ID: ${info.id}")
                            Text("State: ${info.state.name}")
                            Text("Run Attempts: ${info.runAttemptCount}")

                            // Cancel button
                            OutlinedButton(
                                onClick = {
                                    // Cancel the scheduled notification
                                    WorkManager.getInstance(context)
                                        .cancelWorkById(info.id)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Cancel")
                            }

                            // Reschedule button
                            OutlinedButton(
                                onClick = {
                                    // Reschedule with updated delay (example: 10 seconds)
                                    val newDelayMillis = 10 * 1000L // 10 seconds delay for testing
                                    NotificationScheduler.scheduleReminderNotification(
                                        context = context,
                                        delayMillis = newDelayMillis,
                                        title = "Rescheduled Reminder",
                                        message = "Your booking is rescheduled."
                                    )
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Reschedule")
                            }
                        }
                    }
                }
            }
        }
    )

}
