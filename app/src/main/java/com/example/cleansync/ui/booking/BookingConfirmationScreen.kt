package com.example.cleansync.ui.booking

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleansync.utils.DateTimeUtils.toEpochMillis
import com.example.cleansync.utils.NotificationScheduler
import com.example.cleansync.utils.NotificationUtils
import java.time.format.DateTimeFormatter

@Composable
fun BookingConfirmationScreen(
    bookingViewModel: BookingViewModel,
    onReturnHome: () -> Unit
) {
    val context = LocalContext.current
    
    // Capture values immediately and remember them so they don't change during navigation
    val selectedDateTime = remember { bookingViewModel.selectedDateTime }
    val totalPrice = remember { bookingViewModel.totalPrice }
    
    val formattedDateTime = remember(selectedDateTime) {
        selectedDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
    }
    val formattedTotalPrice = remember(totalPrice) {
        totalPrice?.let { "€${"%.2f".format(it)}" } ?: "Not available"
    }

    // Send a confirmation push (once) and schedule a notification 1 hour before the booking
    LaunchedEffect(Unit) {
        selectedDateTime?.let {
            val bookingTimeMillis = it.toEpochMillis()
            val reminderTimeMillis = bookingTimeMillis - 3600000L // 1 hour before
            val now = System.currentTimeMillis()

            val delayMillis = reminderTimeMillis - now
//            val delayMillis = 10_000L // 10 seconds for testing

            if (delayMillis > 0) {
                NotificationScheduler.scheduleReminderNotification(
                    context = context,
                    delayMillis = delayMillis,
                    title = "Appointment Reminder",
                    message = "Your booking is in 1 hour. Time: $formattedDateTime"
                )
            } else {
                Log.w("BookingConfirmationScreen", "Reminder time is in the past. Skipping notification.")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Booking Confirmed!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        formattedDateTime?.let {
            Text(text = "⏰ Date & Time: $it", fontSize = 16.sp)
        }

        Text(text = "💰 Total Price: $formattedTotalPrice", fontSize = 16.sp)


        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onReturnHome()
                bookingViewModel.resetBooking()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}