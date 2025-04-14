
package com.example.cleansync.ui.notifications

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cleansync.data.model.Notification
import com.example.cleansync.ui.theme.CleanSyncTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel(),
    navController: NavController? = null
) {
    val notifications by viewModel.notificationState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    // Show error messages with Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
            )
            viewModel.clearErrorMessage()
        }
    }

    // Calculate the unread notification count for badge
    val unreadCount = remember(notifications) {
        notifications.count { !it.read }
    }

    Scaffold(
        topBar = {
            NotificationTopBar(
                unreadCount = unreadCount,
                onClearAll = { viewModel.clearAllNotifications() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.clearAllNotifications() },
                icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                text = { Text("Clear All") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                notifications.isEmpty() -> {
                    EmptyNotificationsState()
                }
                else -> {
                    NotificationList(
                        notifications = notifications,
                        onMarkRead = { viewModel.toggleReadStatus(it) },
                        onRemove = { viewModel.removeNotification(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTopBar(
    unreadCount: Int,
    onClearAll: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Notifications",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgedBox(badge = {
                        Badge {
                            Text(unreadCount.toString())
                        }
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Unread notifications",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        actions = {
            IconButton(onClick = onClearAll) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Clear all notifications",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
fun EmptyNotificationsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.NotificationsOff,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No notifications yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "You'll see important updates here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationList(
    notifications: List<Notification>,
    onMarkRead: (Notification) -> Unit,
    onRemove: (Notification) -> Unit
) {
    val groupedNotifications = notifications.groupBy { notification ->
        val date = notification.timestamp?.toDate() ?: Date()
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
    }.toSortedMap(compareByDescending { it })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedNotifications.forEach { (monthYear, monthNotifications) ->
            stickyHeader {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = monthYear,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            items(monthNotifications) { notification ->
                NotificationItem(
                    notification = notification,
                    onMarkRead = { onMarkRead(notification) },
                    onRemove = { onRemove(notification) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onMarkRead: () -> Unit,
    onRemove: () -> Unit
) {
    val readColor = MaterialTheme.colorScheme.onSurfaceVariant
    val unreadColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onMarkRead,
                onLongClick = onRemove
            )
            .animateContentSize(spring()),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.read) 1.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold
                ),
                color = if (notification.read) readColor else unreadColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(
                    notification.timestamp?.toDate() ?: Date()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (notification.read) readColor else unreadColor
            )

            if (!notification.read) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = onMarkRead,
                    label = { Text("Mark as read") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewNotificationScreen() {
    CleanSyncTheme {
        NotificationScreen()
    }
}
