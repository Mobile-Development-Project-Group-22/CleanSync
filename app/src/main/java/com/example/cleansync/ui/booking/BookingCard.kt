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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    // Determine if booking is in the past
    val isPastBooking = remember(booking.bookingDateTime) {
        try {
            val bookingDate = LocalDateTime.parse(
                booking.bookingDateTime,
                DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
            )
            bookingDate.isBefore(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = booking.bookingDateTime,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isPastBooking) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small,
                                tonalElevation = 2.dp
                            ) {
                                Text(
                                    text = "Past",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
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

                // Booking Details
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoRow(icon = "👤", label = "Name", value = booking.name)
                    InfoRow(icon = "📧", label = "Email", value = booking.email)
                    InfoRow(icon = "📞", label = "Phone", value = booking.phoneNumber)
                    InfoRow(icon = "📍", label = "City", value = "${booking.city}, ${booking.postalCode}")
                    InfoRow(icon = "📏", label = "Size", value = "${booking.length}m × ${booking.width}m")
                    InfoRow(icon = "💶", label = "Total", value = "€${"%.2f".format(booking.totalPrice)}")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                BookingActionRow(
                    booking = booking,
                    onEdit = onEdit,
                    onCancel = onCancel,
                    showDebug = true,
                    onDebugClick = { showDebugDialog = true }
                )
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
                    .background(color = circleColor, shape = CircleShape),
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
        0 -> if (progressStage in listOf("booked", "collected", "cleaned", "returned")) "completed" else "incomplete"
        1 -> if (progressStage in listOf("collected", "cleaned", "returned")) "completed" else "incomplete"
        2 -> if (progressStage in listOf("cleaned", "returned")) "completed" else "incomplete"
        3 -> if (progressStage == "returned") "completed" else "incomplete"
        else -> "incomplete"
    }
}

@Composable
fun BookingActionRow(
    booking: Booking,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    showDebug: Boolean = false,
    onDebugClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit Button
            FilledTonalButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Booking",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit")
            }

            // Cancel Button
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Cancel Booking",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add to Calendar Button (full width minus debug)
            Button(
                onClick = { booking.addToCalendarSafe(context) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Add to Calendar")
            }

            // Optional Debug Button
            if (showDebug && onDebugClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDebugClick) {
                    Text("🔧", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun NotificationDebugDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var workInfos by remember { mutableStateOf<List<WorkInfo>>(emptyList()) }
    var filterDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        workInfos = WorkManager.getInstance(context)
            .getWorkInfosByTag("booking_reminder")
            .get()
    }

    val filteredWorkInfos = if (filterDate.isNotEmpty()) {
        workInfos.filter { info -> info.tags.any { it.contains(filterDate) } }
    } else workInfos

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("🔧 Scheduled Notifications") },
        text = {
            Column {
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

                            OutlinedButton(
                                onClick = { WorkManager.getInstance(context).cancelWorkById(info.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text("Cancel") }

                            OutlinedButton(
                                onClick = {
                                    NotificationScheduler.scheduleReminderNotification(
                                        context,
                                        delayMillis = 10_000L,
                                        title = "Rescheduled Reminder",
                                        message = "Your booking is rescheduled."
                                    )
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) { Text("Reschedule") }
                        }
                    }
                }
            }
        }
    )
}