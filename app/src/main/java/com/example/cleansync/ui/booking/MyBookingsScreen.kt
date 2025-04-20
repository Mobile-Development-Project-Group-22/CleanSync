package com.example.cleansync.ui.booking

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleansync.model.Booking
import com.example.cleansync.ui.booking.components.DateAndHourPicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(bookingViewModel: BookingViewModel) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var expandedCard by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")

    var editingBooking by remember { mutableStateOf<Booking?>(null) }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var showCancelDialog by remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch bookings
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val snapshot = db.collection("bookings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                bookings = snapshot.documents.mapNotNull {
                    it.toObject(Booking::class.java)?.copy(id = it.id)
                }
            } catch (e: Exception) {
                Log.e("MyBookingsScreen", "Failed to fetch bookings", e)
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Bookings") })
        }
    ) { innerPadding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(12.dp)
            ) {
                items(bookings.size) { index ->
                    val booking = bookings[index]
                    BookingCard(
                        booking = booking,
                        isExpanded = expandedCard == booking.id,
                        onExpandToggle = {
                            expandedCard = if (expandedCard == booking.id) null else booking.id
                        },
                        onEdit = {
                            editingBooking = booking
                            selectedDateTime = null
                            showDateTimePicker = true
                        },
                        onCancel = {
                            bookingToCancel = booking
                            showCancelDialog = true
                        }
                    )
                }
            }

            // Date picker
            if (showDateTimePicker && editingBooking != null) {
                DateAndHourPicker(
                    context = context,
                    onDateTimeSelected = {
                        selectedDateTime = it
                        showDateTimePicker = false
                        showEditDialog = true
                    }
                )
            }

            // Edit Dialog
            if (showEditDialog && editingBooking != null && selectedDateTime != null) {
                AlertDialog(
                    onDismissRequest = {
                        editingBooking = null
                        selectedDateTime = null
                        showEditDialog = false
                    },
                    title = { Text("Edit Booking") },
                    text = {
                        Text("Change booking to: ${selectedDateTime!!.format(formatter)}?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val formatted = selectedDateTime!!.format(formatter)
                                    val id = editingBooking?.id

                                    if (id != null) {
                                        try {
                                            db.collection("bookings")
                                                .document(id)
                                                .update("bookingDateTime", formatted)
                                                .await()

                                            bookings = bookings.map {
                                                if (it.id == id) it.copy(bookingDateTime = formatted)
                                                else it
                                            }
                                        } catch (e: Exception) {
                                            Log.e("EditBooking", "Failed to update booking", e)
                                        }
                                    }

                                    editingBooking = null
                                    selectedDateTime = null
                                    showEditDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            editingBooking = null
                            selectedDateTime = null
                            showEditDialog = false
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Cancel Dialog
            if (showCancelDialog && bookingToCancel != null) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = { Text("Cancel Booking") },
                    text = { Text("Are you sure you want to cancel this booking?") },
                    confirmButton = {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                try {
                                    db.collection("bookings")
                                        .document(bookingToCancel!!.id!!)
                                        .delete()
                                        .await()
                                    bookings = bookings.filter { it.id != bookingToCancel!!.id }
                                } catch (e: Exception) {
                                    Log.e("CancelBooking", "Error deleting booking", e)
                                } finally {
                                    showCancelDialog = false
                                }
                            }
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onExpandToggle() },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📅 ${booking.bookingDateTime}", fontSize = 18.sp)
            Text("🏠 ${booking.streetAddress}", fontSize = 14.sp)

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${booking.name}")
                Text("Email: ${booking.email}")
                Text("Phone: ${booking.phoneNumber}")
                Text("Area: ${booking.length}m x ${booking.width}m")
                Text("Price: €${booking.estimatedPrice}")
                Text("City: ${booking.city}")
                Text("Postal Code: ${booking.postalCode}")

                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

