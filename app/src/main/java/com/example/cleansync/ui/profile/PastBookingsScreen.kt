package com.example.cleansync.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cleansync.model.Booking
import com.example.cleansync.ui.booking.BookingViewModel
import com.example.cleansync.ui.booking.components.DateAndHourPicker
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastBookingsScreen(
    profileViewModel: ProfileViewModel,
    bookingViewModel: BookingViewModel,
    onBackClick: () -> Unit,
    onNavigateToConfirmation: () -> Unit
) {
    val pastBookings by profileViewModel.pastBookings.collectAsState()
    val context = LocalContext.current
    
    var showDateTimePicker by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(Unit) {
        profileViewModel.fetchPastBookings()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Past Bookings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (pastBookings.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Past Bookings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your completed bookings will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pastBookings) { booking ->
                        PastBookingCard(
                            booking = booking,
                            onRebook = {
                                selectedBooking = booking
                                showDateTimePicker = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Date and Time Picker Dialog
    if (showDateTimePicker && selectedBooking != null) {
        DateAndHourPicker(
            context = context,
            bookingViewModel = null,
            onDateTimeSelected = { dateTime ->
                val booking = selectedBooking!!
                
                // Set all booking details from past booking
                bookingViewModel.resetBooking()
                bookingViewModel.name = booking.name
                bookingViewModel.email = booking.email
                bookingViewModel.phoneNumber = booking.phoneNumber
                bookingViewModel.streetAddress = booking.streetAddress
                bookingViewModel.postalCode = booking.postalCode
                bookingViewModel.city = booking.city
                bookingViewModel.length = booking.length
                bookingViewModel.width = booking.width
                bookingViewModel.fabricType = booking.fabricType
                bookingViewModel.selectedDateTime = dateTime
                bookingViewModel.calculatePrice()
                
                // Save the booking immediately
                bookingViewModel.saveBookingToFirestore(
                    onSuccess = {
                        showDateTimePicker = false
                        selectedBooking = null
                        Toast.makeText(
                            context,
                            "Booking created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onNavigateToConfirmation()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            context,
                            "Error: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        )
    }
}

@Composable
fun PastBookingCard(
    booking: Booking,
    onRebook: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Booking #${booking.id?.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "COMPLETED",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Booking details
            BookingDetailRow(label = "Carpet Size", value = "${booking.length} x ${booking.width} m")
            BookingDetailRow(label = "Fabric Type", value = booking.fabricType)
            BookingDetailRow(label = "Price", value = "€${booking.totalPrice}")
            BookingDetailRow(label = "Address", value = booking.streetAddress)
            BookingDetailRow(label = "City", value = "${booking.postalCode}, ${booking.city}")
            
            // Date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = try {
                Date(booking.timestamp)
            } catch (e: Exception) {
                Date()
            }
            BookingDetailRow(label = "Booking Date", value = dateFormat.format(date))

            Spacer(modifier = Modifier.height(16.dp))

            // Rebook button
            Button(
                onClick = onRebook,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rebook with Same Details")
            }
        }
    }
}

@Composable
fun BookingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f)
        )
    }
}
