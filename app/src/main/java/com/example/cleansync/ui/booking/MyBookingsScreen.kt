package com.example.cleansync.ui.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cleansync.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController: NavController,
    bookingViewModel: BookingViewModel
) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var expandedCard by remember { mutableStateOf<String?>(null) }
    var bookingBeingEdited by remember { mutableStateOf<Booking?>(null) }

    // Fetch bookings from Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            val snapshot = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            bookings = snapshot.documents.mapNotNull {
                it.toObject(Booking::class.java)?.copy(id = it.id)
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Bookings") })
        }
    ) { innerPadding ->
        if (loading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp)) {
                items(bookings.size) { index ->
                    val booking = bookings[index]
                    BookingCard(
                        booking = booking,
                        isExpanded = expandedCard == booking.id,
                        onExpandToggle = {
                            expandedCard = if (expandedCard == booking.id) null else booking.id
                        },
                        onEdit = {
                            bookingBeingEdited = booking
                        },
                        onCancel = {
                            db.collection("bookings").document(booking.id!!).delete()
                            bookings = bookings.filter { it.id != booking.id }
                        }
                    )
                }
            }

            bookingBeingEdited?.let { booking ->
                EditBookingDialog(
                    booking = booking,
                    onDismiss = { bookingBeingEdited = null },
                    onSave = { newDate ->
                        db.collection("bookings").document(booking.id!!)
                            .update("bookingDateTime", newDate)
                            .addOnSuccessListener {
                                bookings = bookings.map {
                                    if (it.id == booking.id) it.copy(bookingDateTime = newDate) else it
                                }
                            }
                        bookingBeingEdited = null
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

@Composable
fun EditBookingDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
    var selectedDateTime by remember { mutableStateOf(LocalDateTime.now()) }

    fun openDateTimePicker(context: Context, onResult: (LocalDateTime) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val dateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                        onResult(dateTime)
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(Unit) {
        openDateTimePicker(context) {
            selectedDateTime = it
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Booking Date & Time") },
        text = {
            Text("New: ${selectedDateTime.format(formatter)}")
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(selectedDateTime.format(formatter))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
