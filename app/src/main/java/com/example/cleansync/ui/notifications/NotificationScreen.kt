
package com.example.cleansync.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.data.model.NotificationState
import com.google.firebase.Timestamp
import androidx.compose.animation.fadeOut as fadeOut1


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel = viewModel(),
) {
    val notificationState by notificationViewModel.notificationState.collectAsState()
    val errorMessage by notificationViewModel.errorMessage.collectAsState()
    val isNotificationsEmpty = notificationState.isEmpty()

    if (errorMessage != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = { TextButton(onClick = { /* Retry */ }) { Text("Retry") } }
        ) {
            Text(text = errorMessage ?: "")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (!isNotificationsEmpty) {
                    // Clear All Notifications Button with improved design
                    Button(
                        onClick = { notificationViewModel.clearAllNotifications() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            "Clear All Notifications",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                        )
                    }
                } else {
                    // Placeholder for empty state with smooth transition
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No new notifications",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = notificationState,
                        key = { it.id } // Add unique ID for each notification
                    ) { notification ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = shrinkVertically() + fadeOut1()
                        ) {
                            NotificationItem(
                                notification = notification,
                                onToggleReadStatus = { notificationViewModel.toggleReadStatus(it) },
                                onRemoveNotification = { notificationViewModel.removeNotification(it) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: NotificationState,
    onToggleReadStatus: (NotificationState) -> Unit,
    onRemoveNotification: (NotificationState) -> Unit
) {
    val unreadIndicatorColor by animateColorAsState(
        targetValue = if (notification.isRead) Color.Transparent
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        animationSpec = tween(durationMillis = 300)
    )

    var isHovered by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleReadStatus(notification) }
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .hoverable(interactionSource = remember { MutableInteractionSource() }),
        tonalElevation = if (isHovered) 4.dp else 1.dp,
        shape = RoundedCornerShape(16.dp),
        color = if (isHovered) MaterialTheme.colorScheme.surfaceContainerHigh
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated unread indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(unreadIndicatorColor, CircleShape)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        notification.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                            color = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = formatTimeAgo(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    notification.message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            IconButton(
                onClick = { onRemoveNotification(notification) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun formatTimeAgo(timestamp: Timestamp): String {
    val currentTime = Timestamp.now()
    val timeDifference = currentTime.seconds - timestamp.seconds

    return when {
        timeDifference < 60 -> "${timeDifference}s ago"
        timeDifference < 3600 -> "${timeDifference / 60}m ago"
        timeDifference < 86400 -> "${timeDifference / 3600}h ago"
        else -> "${timeDifference / 86400}d ago"
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    val navController = rememberNavController()
    val notificationViewModel: NotificationViewModel = viewModel()
    NotificationScreen(
        navController = navController,
        notificationViewModel = notificationViewModel
    )
}
