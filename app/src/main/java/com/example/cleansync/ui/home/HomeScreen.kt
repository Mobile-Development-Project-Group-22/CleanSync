package com.example.cleansync.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.example.cleansync.model.Booking
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.utils.NotificationUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward


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
    val homeViewModel = remember { HomeViewModel(authViewModel, notificationViewModel) }
    val showEmailDialog by homeViewModel.showEmailVerificationDialog.collectAsStateWithLifecycle()
    val bookings by homeViewModel.bookings.collectAsStateWithLifecycle()
    val currentUser = homeViewModel.currentUser
    val context = LocalContext.current

    LaunchedEffect(homeViewModel.isLoggedIn) {
        if (!homeViewModel.isLoggedIn) {
            onLogout()
        }
    }

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
                bookings = bookings,
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
    bookings: List<Booking>,
    onBookingClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val context = LocalContext.current

    val bookingsByDate = bookings.groupBy {
        LocalDateTime.parse(it.bookingDateTime, DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")).toLocalDate()
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
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
        }

        item {
            CalendarView(
                bookingsByDate = bookingsByDate,
                onDateClick = { date ->
                    selectedDate = date
                    showDialog = true
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Upcoming Bookings",
                    style = MaterialTheme.typography.titleMedium
                )
            }

        }

        if (bookings.isEmpty()) {
            item {
                Text("No bookings yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(bookings.sortedBy { it.timestamp }) { booking ->
                BookingCard(booking = booking)
            }
        }

        item {
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

    if (showDialog && selectedDate != null) {
        val bookingsOnDate = bookingsByDate[selectedDate] ?: emptyList()
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Bookings on ${selectedDate.toString()}") },
            text = {
                Column {
                    bookingsOnDate.forEach {
                        Text("• ${it.name} at ${it.bookingDateTime}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}


@Composable
fun CalendarView(
    bookingsByDate: Map<LocalDate, List<Booking>>,
    onDateClick: (LocalDate) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    val today = remember { LocalDate.now() }

    val daysInMonth = selectedMonth.lengthOfMonth()
    val firstDayOfMonth = selectedMonth.withDayOfMonth(1)
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value + 6) % 7 // Monday = 0

    val dates = List(startDayOfWeek) { null } + List(daysInMonth) {
        selectedMonth.plusDays(it.toLong())
    }

    val rows = dates.chunked(7).map { week ->
        if (week.size < 7) week + List(7 - week.size) { null } else week
    }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                selectedMonth = selectedMonth.minusMonths(1)
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = selectedMonth.month.name.lowercase()
                    .replaceFirstChar { it.uppercase() } + " ${selectedMonth.year}",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = {
                selectedMonth = selectedMonth.plusMonths(1)
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day Labels (Mon to Sun)
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar Grid
        rows.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date == null) {
                            Spacer(modifier = Modifier.fillMaxSize())
                        } else {
                            val isBooked = bookingsByDate.containsKey(date)
                            val isToday = date == today

                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    isBooked -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onDateClick(date) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




@Composable
private fun BookingCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Name: ${booking.name}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text("City: ${booking.city}", style = MaterialTheme.typography.bodyMedium)
            Text("Booking Time: ${booking.bookingDateTime}", style = MaterialTheme.typography.bodyMedium)
            Text("Estimated Price: \$${booking.estimatedPrice}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
