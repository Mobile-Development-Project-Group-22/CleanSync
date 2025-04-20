// BookingStartScreen.kt
package com.example.cleansync.ui.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.booking.components.CarpetInputForm
import com.example.cleansync.ui.booking.components.DateAndHourPicker

@Composable
fun BookingStartScreen(
    bookingViewModel: BookingViewModel,
    onBookingConfirmed: () -> Unit,
    onBookingCancelled: () -> Unit
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Book Carpet Cleaning",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(onClick = { bookingViewModel.toggleInputFields() }) {
            Text("Start Calculation")
        }

        AnimatedVisibility(visible = bookingViewModel.showInputFields) {
            CarpetInputForm(
                length = bookingViewModel.length,
                width = bookingViewModel.width,
                onLengthChange = { bookingViewModel.length = it },
                onWidthChange = { bookingViewModel.width = it },
                onCalculate = bookingViewModel::calculatePrice
            )
        }

        bookingViewModel.estimatedPrice?.let { price ->
            Text("Estimated Price: €$price", style = MaterialTheme.typography.bodyLarge)

            Button(onClick = {
                showDatePicker = true
            }) {
                Text("Select Date & Time")
            }

            if (showDatePicker) {
                DateAndHourPicker(
                    context = context,
                    bookingViewModel = bookingViewModel
                )
            }
        }

        bookingViewModel.selectedDateTime?.let {
            Text("Selected: ${bookingViewModel.formattedDateTime}")

            Button(onClick = onBookingConfirmed) {
                Text("Continue to Booking Form")
            }

            OutlinedButton(onClick = onBookingCancelled) {
                Text("Cancel")
            }
        }

        bookingViewModel.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
