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
            .padding(vertical = 8.dp)
            .clickable { onExpandToggle()
                onShowExplanation()},
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📅 ${booking.bookingDateTime}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "🏠 ${booking.streetAddress}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Show progress bar
            BookingProgressBar(progressStage = booking.progressStage)

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    "👤 ${booking.name}",
                    "📧 ${booking.email}",
                    "📞 ${booking.phoneNumber}",
                    "📏 ${booking.length}m x ${booking.width}m",
                    "💶 €${booking.estimatedPrice}",
                    "🏙️ ${booking.city}",
                    "📬 ${booking.postalCode}"
                ).forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }

                    if (BuildConfig.DEBUG) {
                        OutlinedButton(
                            onClick = { showDebugDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("🔧 Dev")
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
fun BookingProgressBar(progressStage: String) {
    val stages = listOf("Booked", "Collected", "Cleaned", "Returned")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            // Check if the stage is completed or not
            val isCompleted = getProgressStageColor(progressStage, index) == "completed"

            // Circle color
            val circleColor = if (isCompleted) Color.Green else Color.Gray

            // Line color (only green up to the current stage)
            val lineColor = if (isCompleted) Color.Green else Color.Gray

            // Circle for the stage
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = circleColor,
                        shape = CircleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stage.first().toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Line after the circle
            if (index < stages.size - 1) {
                Spacer(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(
                            // The line is green only up to the current stage
                            if (isCompleted) lineColor else Color.Gray
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
