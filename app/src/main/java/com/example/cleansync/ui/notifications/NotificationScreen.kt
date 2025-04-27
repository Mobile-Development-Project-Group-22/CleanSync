package com.example.cleansync.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Notifications",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.Center)
                                .padding(end = 18.dp) // Padding around the title
                                .fillMaxWidth(), // Fill the width to center the text
                        )
                    }
                        },
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
                },
                modifier = Modifier.height(80.dp)
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
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp), // Spacing between elements
                            modifier = Modifier
                                .padding(24.dp) // Add padding around the entire layout
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ){
                            Icon(
                                imageVector = Icons.Default.NotificationAdd,
                                contentDescription = "No Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(64.dp) // Slightly larger icon for better visibility
                            )
                            Text(
                                text = "No Notifications",
                                style = MaterialTheme.typography.headlineMedium, // Larger, bolder text
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "You will see notifications here when you receive them.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge, // Slightly larger body text
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 32.dp) // Padding on left and right for better balance
                            )
                        }

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


