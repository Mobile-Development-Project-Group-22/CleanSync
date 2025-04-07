//package com.example.cleansync.ui.notifications
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//
//import androidx.compose.ui.unit.dp
//
//import androidx.compose.ui.window.Dialog
//import com.example.cleansync.data.model.NotificationState
//
//@Composable
//fun NotificationDialog(
//    onDismiss: () -> Unit,
//    notificationState: List<NotificationState>,
//    onToggleRead: (NotificationState) -> Unit,
//    onRemove: (NotificationState) -> Unit,
//    onClearAll: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        confirmButton = {},
//        title = { Text("Notifications", style = MaterialTheme.typography.headlineSmall) },
//        text = {
//            if (notificationState.isEmpty()) {
//                Text(
//                    "No new notifications",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color.Gray
//                )
//            } else {
//                Column {
//                    LazyColumn(modifier = Modifier.height(300.dp)) {
//                        items(notificationState) { notification ->
//                            NotificationItem(
//                                notification = notification,
//                                onToggleRead = { onToggleRead(notification) },
//                                onRemove = { onRemove(notification) },
//                                onToggleReadStatus = { updatedNotification ->
//                                    onToggleRead(updatedNotification)
//                                },
//                                onRemoveNotification = { removedNotification ->
//                                    onRemove(removedNotification)
//                                }
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Clear all notifications button
//                    TextButton(
//                        onClick = onClearAll,
//                        enabled = notificationState.isNotEmpty(),
//                        modifier = Modifier.align(Alignment.End)
//                    ) {
//                        Text(
//                            "Clear All",
//                            color = if (notificationState.isNotEmpty()) Color.Red else Color.Gray
//                        )
//                    }
//                }
//            }
//        }
//    )
//}
//
