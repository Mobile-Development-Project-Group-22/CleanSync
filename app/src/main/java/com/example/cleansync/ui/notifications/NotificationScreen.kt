package com.example.cleansync.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cleansync.data.model.Notification
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(viewModel: NotificationViewModel = viewModel()) {
    val notifications by viewModel.notificationState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()


    LaunchedEffect(Unit) {
        if (FirebaseAuth.getInstance().currentUser != null) {
        viewModel.refreshNotifications()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    TextButton(onClick = { viewModel.clearAllNotifications() }) {
                        Text("Clear All", color = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            errorMessage?.let { msg ->
                ErrorBanner(message = msg, onDismiss = viewModel::clearErrorMessage)
            }

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notifications available")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onToggleRead = { viewModel.toggleReadStatus(notification) },
                            onRemove = { viewModel.removeNotification(notification) }
                        )
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
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggleRead() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) Color.LightGray else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text = notification.title ?: "No title",
//                fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal,
//                style = MaterialTheme.typography.titleMedium
//            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message ?: "No message",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRemove) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = Color.Red,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = message, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = Color.White)
            }
        }
    }
}
