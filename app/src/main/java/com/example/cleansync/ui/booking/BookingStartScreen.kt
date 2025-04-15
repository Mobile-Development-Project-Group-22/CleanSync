package com.example.cleansync.ui.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.util.*

@Composable
fun BookingStartScreen(
    bookingViewModel: BookingViewModel,
    onBookingConfirmed: () -> Unit,
    onBookingCancelled: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = { bookingViewModel.toggleInputFields() }) {
            Text("Calculate Price")
        }

        if (bookingViewModel.showInputFields) {
            OutlinedTextField(
                value = bookingViewModel.length,
                onValueChange = { bookingViewModel.length = it },
                label = { Text("Carpet Length (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bookingViewModel.width,
                onValueChange = { bookingViewModel.width = it },
                label = { Text("Carpet Width (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { bookingViewModel.calculatePrice() }) {
                Text("Calculate Now")
            }
        }

        bookingViewModel.estimatedPrice?.let { price ->
            Text("Estimated Price: €$price")

            Button(onClick = {
                showDateTimePicker(context, bookingViewModel)
            }) {
                Text("Select Date & Time")
            }
        }

        bookingViewModel.selectedDateTime?.let {
            Text("Selected: ${bookingViewModel.formattedDateTime}")

            Button(onClick = {
                onBookingConfirmed()
            }) {
                Text("Continue to Booking Form")
            }

            Button(onClick = onBookingCancelled) {
                Text("Cancel")
            }
        }
    }
}

fun showDateTimePicker(context: Context, bookingViewModel: BookingViewModel) {
    val current = Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val selectedDateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                    bookingViewModel.updateSelectedDateTime(selectedDateTime) // updated name
                },
                current.get(Calendar.HOUR_OF_DAY),
                current.get(Calendar.MINUTE),
                true
            ).show()
        },
        current.get(Calendar.YEAR),
        current.get(Calendar.MONTH),
        current.get(Calendar.DAY_OF_MONTH)
    ).show()
}
