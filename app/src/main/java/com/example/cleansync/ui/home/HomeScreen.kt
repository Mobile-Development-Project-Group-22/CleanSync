package com.example.cleansync.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.utils.NotificationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    onNavigateToBooking: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    // Manually create HomeViewModel using provided dependencies
    val homeViewModel = remember { HomeViewModel(authViewModel, notificationViewModel) }
    val showEmailDialog by homeViewModel.showEmailVerificationDialog.collectAsStateWithLifecycle()
    val currentUser = homeViewModel.currentUser
    val context = LocalContext.current

    // Trigger logout if not logged in
    LaunchedEffect(homeViewModel.isLoggedIn) {
        if (!homeViewModel.isLoggedIn) {
            onLogout()
        }
    }

    // Show email verification dialog
    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { homeViewModel.dismissEmailDialog() },
            title = { Text("Email Verification") },
            text = { Text("Please verify your email to continue.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        homeViewModel.resendVerificationEmail()
                        homeViewModel.dismissEmailDialog()
                    }
                ) {
                    Text("Resend Verification Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { homeViewModel.dismissEmailDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            HomeAppBar(
                userName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User",
                unreadCount = homeViewModel.unreadNotificationCount(),
                onNotificationClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile
            )
        },
        content = { innerPadding ->
            HomeContent(
                modifier = Modifier.padding(innerPadding),
                onBookingClick = onNavigateToBooking,
                onLogoutClick = {
                    homeViewModel.signOut()
                    onLogout()
                },
            )
        },
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
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
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
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }

            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
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
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(
            onClick = onBookingClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Book a Cleaning", style = MaterialTheme.typography.labelLarge)
        }

        OutlinedButton(
            onClick = {
                NotificationUtils.triggerNotification(
                    context = context,
                    title = "Test Notification",
                    message = "This is a test notification",
                    read = false,
                    scheduleTimeMillis = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Test Notification", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.weight(1f))

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
