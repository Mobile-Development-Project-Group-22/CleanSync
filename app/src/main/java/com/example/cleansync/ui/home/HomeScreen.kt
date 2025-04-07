package com.example.cleansync.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.example.cleansync.ui.auth.AuthState
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.profile.ProfileViewModel
import com.example.cleansync.ui.notifications.NotificationDialog

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
    var showDialog by remember { mutableStateOf(false) }
    val notificationState by notificationViewModel.notificationState.collectAsState()

    // Notification Dialog
    if (showDialog) {
        NotificationDialog(
            onDismiss = { showDialog = false },
            notificationState = notificationState,
            onToggleRead = { notificationViewModel.toggleReadStatus(it) },
            onRemove = { notificationViewModel.removeNotification(it) },
            onClearAll = { notificationViewModel.clearAllNotifications() }
        )
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                if (navController.currentBackStackEntry?.destination?.route == Screen.LoginScreen.route) {
                    navController.popBackStack() // Return to home if already logged in
                }
            }
            is AuthState.Error -> {
                // Handle error state (e.g., show a Snackbar with the error message)
            }
            is AuthState.Loading -> {
                // Show loading indicator
            }
            else -> {
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CleanSync",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Welcome message
                Text(
                    text = "Welcome ${currentUser?.displayName ?: "Guest"}!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Button to trigger test notification
                Button(
                    onClick = { sendTestNotification(notificationViewModel) },
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Send Test Notification",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Go to Booking Screen
                Button(
                    onClick = { navController.navigate(Screen.BookingScreen.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Go to Booking", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                OutlinedButton(
                    onClick = {
                        authViewModel.signOut()
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(Screen.HomeScreen.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    )
}

fun sendTestNotification(viewModel: NotificationViewModel) {
    val title = "Test Notification"
    val message = "This is a test notification sent from the button click."
    viewModel.addNotification(
        NotificationState(
            title = title,
            message = message,
            isRead = false
        )
    )
}