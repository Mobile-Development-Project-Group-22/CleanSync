package com.example.cleansync.ui.home

import android.widget.CalendarView
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.cleansync.data.model.Notification
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.profile.ProfileViewModel
import com.example.cleansync.utils.NotificationUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = authViewModel.currentUser
    val showEmailVerificationDialog = remember { mutableStateOf(false) }
    val unreadCount = notificationViewModel.unreadNotificationsCount()
    val notificationState by notificationViewModel.notificationState.collectAsState()

    LaunchedEffect(authViewModel.isLoggedIn) {
        if (!authViewModel.isLoggedIn) {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(Screen.HomeScreen.route) { inclusive = true }
            }
        } else if (!authViewModel.isEmailVerified) {
            showEmailVerificationDialog.value = true
        }
    }

    if (showEmailVerificationDialog.value) {
        AlertDialog(
            onDismissRequest = { showEmailVerificationDialog.value = false },
            title = { Text("Email Verification") },
            text = { Text("Please verify your email to continue.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.isEmailVerified
                        showEmailVerificationDialog.value = false
                    }
                ) {
                    Text("Resend Verification Email")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEmailVerificationDialog.value = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

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
                onBookingClick = { navController.navigate(Screen.BookingStartScreen.route) },
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
    val context = LocalContext.current
    val selectedDate = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Calendar at the top
        CalendarView(
            selectedDate = selectedDate.value,
            bookingDate = "2023-12-25", // Example booking date
            onDateChange = { date ->
                selectedDate.value = date
            }
        )

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
                NotificationUtils.sendCustomNotification(
                    context = context,
                    title = "Test Notification",
                    message = "This is a test notification"
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

@Composable
fun CalendarView(
    selectedDate: String,
    bookingDate: String?, // Pass the booking date
    onDateChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Selected Date: $selectedDate",
            style = MaterialTheme.typography.titleMedium
        )
        AndroidView(
            factory = { context ->
                CalendarView(context).apply {
                    // Highlight the booking date if available
                    bookingDate?.let { date ->
                        val parts = date.split("-")
                        if (parts.size == 3) {
                            val year = parts[0].toInt()
                            val month = parts[1].toInt() - 1 // Month is 0-based
                            val day = parts[2].toInt()
                            val calendar = java.util.Calendar.getInstance()
                            calendar.set(year, month, day)
                            this.date = calendar.timeInMillis
                        }
                    }

                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val date = "$year-${month + 1}-$dayOfMonth"
                        onDateChange(date)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun sendTestNotification(viewModel: NotificationViewModel) {
    viewModel.addNotification(
        Notification(
            userId = "test_user_id",
            message = "This is a test notification",
            read = false,
            timestamp = com.google.firebase.Timestamp.now()
        )
    )
}