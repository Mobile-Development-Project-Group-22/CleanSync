package com.example.cleansync.ui.notifications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.ui.theme.CleanSyncTheme
import com.example.cleansync.data.model.Notification

@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel
) {
    // Collecting the state from ViewModel
    val notificationState = viewModel.notificationState.collectAsState()
    val errorMessage = viewModel.errorMessage.collectAsState()

    // Show error message if it exists
    errorMessage.value?.let { error ->
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp),
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title Section
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display a loading message if notifications are empty
        if (notificationState.value.isEmpty()) {
            Text(
                text = "No notifications",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            // Display the list of notifications
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(notificationState.value) { notification ->
                    NotificationItem(notification, viewModel)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, viewModel: NotificationViewModel) {
    val backgroundColor = if (notification.read) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = notification.message, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Received: ${notification.timestamp?.toDate()?.toString() ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons: Mark as Read / Remove
            Column(horizontalAlignment = Alignment.End) {
                // Mark as Read button
                if (!notification.read) {
                    Button(
                        onClick = { viewModel.markNotificationAsRead(notification) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("Mark as Read", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                // Remove button
                Button(
                    onClick = { viewModel.removeNotification(notification) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    CleanSyncTheme {
        val mockViewModel = NotificationViewModel()  // You can create a mock ViewModel if needed for preview
        NotificationScreen(navController = rememberNavController(), viewModel = mockViewModel)
    }
}