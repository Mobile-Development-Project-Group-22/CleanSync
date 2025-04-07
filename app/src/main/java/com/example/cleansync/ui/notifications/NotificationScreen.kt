package com.example.cleansync.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CardDefaults.shape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleansync.data.model.NotificationState
import com.google.firebase.Timestamp

@Composable
fun NotificationsScreen(
    notifications: List<NotificationState>,
    onMarkAsRead: (NotificationState) -> Unit,
    onRemove: (NotificationState) -> Unit,
    onClearAll: () -> Unit
) {
    if (notifications.isEmpty()) {
        EmptyNotificationsMessage()
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notifications) { notification ->
                NotificationItem(
                    notification = notification,
                    onToggleRead = { onMarkAsRead(notification) },
                    onRemove = { onRemove(notification) }
                )
            }
        }
        ClearAllNotificationsButton(onClearAll)
    }
}

@Composable
fun EmptyNotificationsMessage() {
    Text(
        text = "No new notifications",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun ClearAllNotificationsButton(onClearAll: () -> Unit) {
    TextButton(
        onClick = onClearAll,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        enabled = true
    ) {
        Text("Clear All", color = Color.Red)
    }
}


@Composable
fun NotificationTitle(notification: NotificationState) {
    Text(
        text = notification.title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun NotificationMessage(notification: NotificationState) {
    Text(
        text = notification.message,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray
    )
}

@Composable
fun NotificationTimestamp(notification: NotificationState) {
    Text(
        text = "Received: ${notification.timestamp.toDate()}",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray
    )
}

@Composable
fun NotificationItem(
    notification: NotificationState,
    onToggleRead: () -> Unit,
    onRemove: () -> Unit
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Display confirmation dialog for removal
    if (showConfirmationDialog) {
        RemovalConfirmationDialog(
            onDismiss = { showConfirmationDialog = false },
            onConfirm = {
                onRemove()
                showConfirmationDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            NotificationTitle(notification = notification)
            Spacer(modifier = Modifier.height(8.dp))
            NotificationMessage(notification = notification)
            Spacer(modifier = Modifier.height(8.dp))
            NotificationTimestamp(notification = notification)
            Spacer(modifier = Modifier.height(8.dp))
            // Pass notification to NotificationActions
            NotificationActions(
                notification = notification,
                onToggleRead = onToggleRead,
                onRemove = onRemove,
                onShowConfirmationDialog = { showConfirmationDialog = true }
            )
        }
    }
}

@Composable
fun NotificationActions(
    notification: NotificationState,
    onToggleRead: () -> Unit,
    onRemove: () -> Unit,
    onShowConfirmationDialog: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onToggleRead) {
            Icon(
                imageVector = if (notification.isRead) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle Read",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        TextButton(onClick = onShowConfirmationDialog) {
            Text("Remove", color = Color.Red)
        }
    }
}

@Composable
fun RemovalConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Removal") },
        text = { Text("Are you sure you want to remove this notification?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}

// Extension function for converting timestamp to relative time
fun Timestamp.toRelativeTime(): String {
    val currentTime = System.currentTimeMillis()
    val timeDifference = currentTime - this.toDate().time

    return when {
        timeDifference < 60000 -> "Just now"
        timeDifference < 3600000 -> "${timeDifference / 60000} minutes ago"
        timeDifference < 86400000 -> "${timeDifference / 3600000} hours ago"
        else -> "${timeDifference / 86400000} days ago"
    }
}