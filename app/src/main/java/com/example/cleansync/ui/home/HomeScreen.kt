package com.example.cleansync.ui.home

import android.media.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cleansync.model.Booking
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.booking.BookingViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.example.cleansync.ui.components.ChatbotDialog
import com.google.accompanist.swiperefresh.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.cleansync.R
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.cleansync.ChatActivity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    bookingViewModel: BookingViewModel,
    onNavigateToBooking: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,

    onNavigateToMyBookings: (String?) -> Unit,
    onLogout: () -> Unit

) {
    val homeViewModel = remember { HomeViewModel(authViewModel, notificationViewModel) }
    val bookings by homeViewModel.bookings.collectAsStateWithLifecycle()
    val isRefreshing by homeViewModel.isRefreshing.collectAsStateWithLifecycle()
    val completedBookings by homeViewModel.completedBookings.collectAsStateWithLifecycle()
    val loyaltyPoints by homeViewModel.loyaltyPoints.collectAsStateWithLifecycle()
    val userName = homeViewModel.currentUser?.displayName?.split(" ")?.firstOrNull() ?: "User"
    var isChatOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current


    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                HomeAppBar(
                    userName = userName,
                    unreadCount = homeViewModel.unreadNotificationCount(),
                    onNotificationClick = onNavigateToNotifications,
                    onProfileClick = onNavigateToProfile,

                )
            },
            floatingActionButton = {
                // Container for both FABs to arrange them
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),  // Provide some padding so they don't stick to edges
                    contentAlignment = Alignment.BottomEnd // Align to the bottom right corner
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Adds space between buttons
                    ) {
                        FloatingActionButton(
                            onClick = onNavigateToBooking,
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Book Now"
                            )
                        }

                        FloatingActionButton(
                            onClick = { isChatOpen = true },
                            shape = CircleShape,
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.chatbot), // Replace with your image resource
                                contentDescription = "Chat with Assistant",
                                modifier = Modifier.size(45
                                    .dp) // Adjust size as needed
                            )
                        }
                    }
                }
            },
            content = { paddingValues ->
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = { homeViewModel.refreshBookings() },
                    modifier = Modifier.fillMaxSize(),
                    indicator = { state, trigger ->
                        SwipeRefreshIndicator(
                            state = state,
                            refreshTriggerDistance = trigger,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.extraLarge
                        )
                    }
                ) {
                    HomeContent(
                        bookings = bookings,
                        completedBookings = completedBookings,
                        userName = userName,
                        onBookingClick = onNavigateToBooking,
                        onBookingCardClick = onNavigateToMyBookings,
                        onLogoutClick = {
                            homeViewModel.signOut()
                            onLogout()
                        },
                        contentPadding = paddingValues
                    )
                }
            }
        )


        // Show Chatbot Dialog when open
        if (isChatOpen) {
            ChatbotDialog(
                onDismiss = { isChatOpen = false },
                onNavigateToBooking = onNavigateToBooking,
                onNavigateToMyBookings = { onNavigateToMyBookings(null) },
                onNavigateToAgentChat = {
                    isChatOpen = false   // Close chatbot dialog first

                    context.startActivity(
                        Intent(context, ChatActivity::class.java)
                    )
                }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeAppBar(
    userName: String,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,

) {
    val isUserLoggedIn = userName.isNotEmpty() // Check if the user is logged in
    TopAppBar(
        title = {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.animateContentSize() // Animation for title resizing
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {

            // Profile Icon with Dynamic User State
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 20.dp)
                    .animateContentSize() // Animation for button press
            ) {
                if (isUserLoggedIn) {
                    // Display user profile if logged in
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    // Show a default icon or a login icon
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "Login",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Notification Icon with Badge
            // Using BadgedBox to show the unread count

            BadgedBox(
                badge = {

                        Badge(
                            modifier = Modifier.padding(
                            ),
                        ) {
                            Text(
                                unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondary,

                            )
                        }

                }
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .padding(
                            start = 0.dp,
                            end = 0.dp,
                            bottom = 20.dp,
                        )
                        .size(40.dp)
                        .animateContentSize() // Animation for button press
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = if (unreadCount > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(

                            )
                            .animateContentSize() // Animation for button press
                    )
                }
            }


        },
        modifier = Modifier.height(80.dp) // Set desired height for top bar
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    bookings: List<Booking>,
    completedBookings: Int,
    userName: String,
    onBookingClick: () -> Unit,
    onBookingCardClick: (String?) -> Unit,
    onLogoutClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val bookingsByDate = bookings.groupBy {
        LocalDateTime.parse(it.bookingDateTime, DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")).toLocalDate()
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        )
    ) {
        item {
            UserGreetingCard(userName = userName, bookings = bookings, completedBookings = completedBookings)
        }

        item {
            CalendarView(bookingsByDate = bookingsByDate) {
                selectedDate = it
                showDialog = true
            }
        }

        item {
            Text("Upcoming Bookings", style = MaterialTheme.typography.titleMedium)
        }

        if (bookings.isEmpty()) {
            item {
                EmptyBookingCard(onBookingClick)
            }
        } else {
            items(
                bookings.sortedBy { booking ->
                    try {
                        LocalDateTime.parse(
                            booking.bookingDateTime,
                            DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
                        )
                    } catch (e: Exception) {
                        LocalDateTime.MAX
                    }
                }
            ) { booking ->
                BookingCard(
                    booking = booking,
                    onClick = { onBookingCardClick(booking.id) }
                )
            }
        }

    }

    if (showDialog && selectedDate != null) {
        BookingDialog(
            date = selectedDate!!,
            bookings = bookingsByDate[selectedDate] ?: emptyList(),
            onDismiss = { showDialog = false },
            onBookClick = {
                showDialog = false
                onBookingClick()
            }
        )
    }
}

// All other components such as UserGreetingCard, StatItem, BookingCard, etc., remain unchanged.


@Composable
fun UserGreetingCard(userName: String, bookings: List<Booking>, completedBookings: Int) {
    val upcoming = bookings.size
    val name = userName.split(" ").firstOrNull() ?: userName

    SectionCard {
        Text("Welcome back, $name! 👋", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            StatItem(upcoming, "Upcoming Booking${if (upcoming != 1) "s" else ""}", Icons.Default.Event)
            StatItem(completedBookings, "Cleaning${if (completedBookings != 1) "s" else ""} Done", Icons.Default.CheckCircle)
        }
    }
}

@Composable
private fun StatItem(value: Int, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(value.toString(), fontWeight = FontWeight.Bold)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

@Composable
fun BookingCard(booking: Booking, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Customer Name
                Text(
                    text = booking.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Date and Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = booking.bookingDateTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Arrow in circular background
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Details",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBookingCard(onBookingClick: () -> Unit) {
    SectionCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Text("No bookings yet", style = MaterialTheme.typography.titleSmall)
            Text("Book your first cleaning service", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onBookingClick) {
                Text("Book Now")
            }
        }
    }
}

@Composable
fun BookingDialog(date: LocalDate, bookings: List<Booking>, onDismiss: () -> Unit, onBookClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Bookings on ${date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
        },
        text = {
            Column {
                if (bookings.isEmpty()) {
                    Text("No bookings scheduled for this day")
                } else {
                    bookings.forEach {
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(it.name, fontWeight = FontWeight.Bold)
                                Text(it.bookingDateTime)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = if (bookings.isEmpty()) onBookClick else onDismiss) {
                Text(if (bookings.isEmpty()) "Book Now" else "Close")
            }
        }
    )
}


// A reusable card component for sections
@Composable
fun SectionCard(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            title?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            }
            content()
        }
    }
}
