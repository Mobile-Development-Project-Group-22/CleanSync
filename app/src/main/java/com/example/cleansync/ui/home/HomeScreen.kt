package com.example.cleansync.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cleansync.data.model.NotificationState
import com.example.cleansync.navigation.Screen

import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = profileViewModel.currentUser

    // Observe the notification state
    val notificationState by notificationViewModel.notificationState.collectAsState()
    val unreadCount = notificationState.count { !it.isRead }

    Scaffold(
        topBar = {
            HomeAppBar(
                userName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User",
                unreadCount = unreadCount,
                onNotificationClick = { navController.navigate(Screen.NotificationScreen.route) },
                onProfileClick = { navController.navigate(Screen.ProfileScreen.route) }
            )
        },
        content = { innerPadding ->
            HomeContent(
                modifier = Modifier.padding(innerPadding),
                onBookingClick = { navController.navigate(Screen.BookingScreen.route) },
                onLogoutClick = {
                    authViewModel.signOut()
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                },
                onTestNotification = { sendTestNotification(notificationViewModel) }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeAppBar(
    userName: String,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Hi, $userName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
            // Notification icon with badge
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge {
                            Text(unreadCount.toString())
                        }
                    }
                }
            ) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }

            // Profile icon
            IconButton(onClick = onProfileClick) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile"
                )
            }
        }
    )
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    onBookingClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onTestNotification: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Primary action button
        FilledTonalButton(
            onClick = onBookingClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Book a Cleaning", style = MaterialTheme.typography.labelLarge)
        }

        // Secondary action button
        OutlinedButton(
            onClick = onTestNotification,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Test Notification", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout button
        TextButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Logout",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun sendTestNotification(viewModel: NotificationViewModel) {
    viewModel.addNotification(
        NotificationState(
            title = "Test Notification",
            message = "This is a test notification",
            isRead = false
        )
    )
}