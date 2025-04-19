package com.example.cleansync.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cleansync.data.model.Notification
import com.example.cleansync.ui.theme.CleanSyncTheme
import com.google.accompanist.swiperefresh.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(viewModel: NotificationViewModel = viewModel()) {
    val notifications by viewModel.notificationState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    LaunchedEffect(userId) {
        if (userId != null) viewModel.refreshNotifications()
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.refreshNotifications()
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAllNotifications() }) {
                            Text("Clear All", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { isRefreshing = true },
            modifier = Modifier.padding(padding)
        ) {
            when {
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Error loading notifications.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No notifications yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    val grouped = groupNotifications(notifications)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        grouped.forEach { (label, items) ->
                            item {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth()
                                )
                            }
                            items(items) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    onToggleRead = { viewModel.toggleReadStatus(notification) },
                                    onDelete = { viewModel.removeNotification(notification) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onToggleRead: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (notification.read)
        MaterialTheme.colorScheme.surface
    else
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggleRead() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.message ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!notification.read) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp.seconds * 1000 + timestamp.nanoseconds / 1_000_000))
}

fun groupNotifications(notifications: List<Notification>): Map<String, List<Notification>> {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

    fun dateLabel(time: Timestamp?): String {
        if (time == null) return "Unknown"
        val date = Calendar.getInstance().apply {
            timeInMillis = time.seconds * 1000 + time.nanoseconds / 1_000_000
        }
        return when {
            isSameDay(date, today) -> "Today"
            isSameDay(date, yesterday) -> "Yesterday"
            else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date.time)
        }
    }

    return notifications.groupBy { dateLabel(it.timestamp) }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}


