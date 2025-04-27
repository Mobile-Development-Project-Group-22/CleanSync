package com.example.cleansync.ui.booking

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var editingBooking by remember { mutableStateOf<Booking?>(null) }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
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

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "My Bookings",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.Center)
                                .padding(end = 18.dp) // Padding around the title
                                .fillMaxWidth(), // Fill the width to center the text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),

                modifier = Modifier.height(80.dp)

            )
                 },

        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->



        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                bookings.isEmpty() ->  EmptyBookingCard(onBookingClick)
                else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(bookings.size) { index ->
                        val booking = bookings[index]
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

        }
    }
}
