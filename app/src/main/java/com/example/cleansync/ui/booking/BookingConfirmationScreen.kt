package com.example.cleansync.ui.booking

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
import com.example.cleansync.utils.NotificationUtils
import java.time.format.DateTimeFormatter

@Composable
fun BookingConfirmationScreen(
    bookingViewModel: BookingViewModel,
    onReturnHome: () -> Unit
) {
    val context = LocalContext.current
    val selectedDateTime = bookingViewModel.selectedDateTime
    val estimatedPrice = bookingViewModel.estimatedPrice
    val formattedDateTime = remember(selectedDateTime) {
        selectedDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
    }
    val formattedPrice = remember(estimatedPrice) {
        estimatedPrice?.let { "€$it" } ?: "Not available"
    }

    // Send a confirmation push (once)
    LaunchedEffect(Unit) {
        formattedDateTime?.let {
            NotificationUtils.sendCustomNotification(
                context = context,
                title = "Booking Confirmation",
                message = "Your booking has been confirmed for $it. Estimated Price: $formattedPrice"
            )
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

        Text(text = "💰 Estimated Price: $formattedPrice", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Schedule reminder 1 hour before
                selectedDateTime?.let { dateTime ->
                    val millis = selectedDateTime
                        ?.minusMinutes(60)
                        ?.toEpochMillis()

                    NotificationUtils.scheduleLocalNotification(
                        context = context,
                        title = "Booking Reminder",
                        message = "Your booking is in 1 hour!",
                        timeInMillis = millis
                    )
                }

                bookingViewModel.resetBooking()
                onReturnHome()
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