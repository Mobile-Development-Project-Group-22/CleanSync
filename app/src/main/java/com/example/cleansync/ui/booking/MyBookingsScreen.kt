package com.example.cleansync.ui.booking

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cleansync.model.Booking
import com.example.cleansync.ui.booking.components.DateAndHourPicker
import com.example.cleansync.ui.home.EmptyBookingCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    viewModel: BookingViewModel,
    onBookingClick: () -> Unit,
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var bookings by remember { mutableStateOf(emptyList<Booking>()) }
    var loading by remember { mutableStateOf(true) }
    
    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var editingBooking by remember { mutableStateOf<Booking?>(null) }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var explanationShown by rememberSaveable { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(false) }


    if (showExplanationDialog) {
        BookingStatusExplanationAnimation(onDismiss = { showExplanationDialog = false })
    }

    // Fetch bookings on launch
    LaunchedEffect(userId) {
        userId?.let {
            try {
                val result = db.collection("bookings").whereEqualTo("userId", it).get().await()
                bookings = result.documents.mapNotNull { doc -> doc.toObject(Booking::class.java)?.copy(id = doc.id) }
            } catch (e: Exception) {
                Log.e("Bookings", "Error fetching bookings", e)
            } finally {
                loading = false
            }
        }
    }

    // Filter bookings
    val filteredBookings = remember(bookings, fromDate, toDate) {
        bookings.filter { booking ->
            try {
                val bookingDate = LocalDateTime.parse(
                    booking.bookingDateTime,
                    DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
                ).toLocalDate()
                
                val afterFrom = fromDate?.let { bookingDate >= it } ?: true
                val beforeTo = toDate?.let { bookingDate <= it } ?: true
                
                afterFrom && beforeTo
            } catch (e: Exception) {
                false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Bookings",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->



        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                bookings.isEmpty() ->  EmptyBookingCard(onBookingClick)
                else -> Column(modifier = Modifier.fillMaxSize()) {
                    // Date Range Filter Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Filter by Date Range",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // From Date
                            FilterChip(
                                selected = fromDate != null,
                                onClick = { showFromDatePicker = true },
                                label = {
                                    Text(
                                        fromDate?.format(dateFormatter) ?: "From Date"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "From date",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            // To Date
                            FilterChip(
                                selected = toDate != null,
                                onClick = { showToDatePicker = true },
                                label = {
                                    Text(
                                        toDate?.format(dateFormatter) ?: "To Date"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "To date",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Clear and Results Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (fromDate != null || toDate != null) {
                                TextButton(
                                    onClick = { 
                                        fromDate = null
                                        toDate = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear filter",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear Filter")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                            
                            Text(
                                text = "${filteredBookings.size} booking${if (filteredBookings.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider()

                    // Bookings List
                    if (filteredBookings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "No bookings found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Try adjusting your filters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredBookings.size) { index ->
                                val booking = filteredBookings[index]
                                BookingCard(
                                    booking = booking,
                                    isExpanded = booking.id == expandedCardId,
                                    onExpandToggle = {
                                        expandedCardId = if (expandedCardId == booking.id) null else booking.id
                                    },
                                    onEdit = {
                                        editingBooking = booking
                                        showDatePicker = true
                                    },
                                    onCancel = {
                                        bookingToCancel = booking
                                        showCancelDialog = true
                                    },
                                    onShowExplanation = {
                                        if (!explanationShown) {
                                            showExplanationDialog = true
                                            explanationShown = true
                                        }
                                    }

                                )
                            }
                        }
                    }
                }
            }

            if (showDatePicker && editingBooking != null) {
                DateAndHourPicker(context) {
                    selectedDateTime = it
                    showDatePicker = false
                    showEditDialog = true
                }
            }

            if (showEditDialog && editingBooking != null && selectedDateTime != null) {
                EditBookingDialog(
                    booking = editingBooking!!,
                    newDateTime = selectedDateTime!!,
                    onDismiss = {
                        editingBooking = null
                        selectedDateTime = null
                        showEditDialog = false
                    },
                    onConfirm = {
                        showEditDialog = false
                        coroutineScope.launch {
                            try {
                                val formatted = selectedDateTime!!.format(formatter)
                                db.collection("bookings").document(editingBooking!!.id!!).update("bookingDateTime", formatted).await()
                                bookings = bookings.map {
                                    if (it.id == editingBooking!!.id) it.copy(bookingDateTime = formatted) else it
                                }
                                snackbarHostState.showSnackbar("Booking updated.")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed to update booking.")
                            }
                            showEditDialog = false
                        }
                    }
                )
            }

            if (showCancelDialog && bookingToCancel != null) {
                CancelBookingDialog(
                    onDismiss = { showCancelDialog = false },
                    onConfirm = {
                        showCancelDialog = false
                        coroutineScope.launch {
                            try {
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                val id = bookingToCancel!!.id!!
                                Log.d("CancelBooking", "Attempting to delete booking with ID: $id")
                                db.collection("bookings").document(id).delete().await()
                                bookings = bookings.filterNot { it.id == id }
                                snackbarHostState.showSnackbar("Booking canceled.")
                            } catch (e: Exception) {
                                Log.e("CancelBooking", "Delete failed: ${e.message}", e)
                                snackbarHostState.showSnackbar("Failed to cancel booking.")
                            }
                            showCancelDialog = false
                        }
                    }
                )
            }

            // Date Filter Pickers
            if (showFromDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showFromDatePicker = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val instant = java.time.Instant.ofEpochMilli(millis)
                                    val zoneId = java.time.ZoneId.systemDefault()
                                    fromDate = LocalDateTime.ofInstant(instant, zoneId).toLocalDate()
                                }
                                showFromDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFromDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            if (showToDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showToDatePicker = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val instant = java.time.Instant.ofEpochMilli(millis)
                                    val zoneId = java.time.ZoneId.systemDefault()
                                    toDate = LocalDateTime.ofInstant(instant, zoneId).toLocalDate()
                                }
                                showToDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showToDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

        }
    }
}
