// BookingScreen.kt

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
fun BookingScreen(
    bookingViewModel: BookingViewModel,
    onBookingConfirmed: () -> Unit,
    onBookingCancelled: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Step 1: Show Calculate Price Button
        Button(onClick = { bookingViewModel.toggleInputFields() }) {
            Text("Calculate Price")
        }

        // Step 2: Show Length & Width inputs if toggle is true
        if (bookingViewModel.showInputFields) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bookingViewModel.length,
                onValueChange = { bookingViewModel.length = it },
                label = { Text("Carpet Length (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bookingViewModel.width,
                onValueChange = { bookingViewModel.width = it },
                label = { Text("Carpet Width (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Calculate Now Button
            Button(onClick = { bookingViewModel.calculatePrice() }) {
                Text("Calculate Now")
            }
        }

        // Step 4: Show Calculated Price
        bookingViewModel.estimatedPrice?.let { price ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Estimated Price: €$price")

            Spacer(modifier = Modifier.height(16.dp))

            // Step 5: Show Date & Time Picker
            Button(onClick = {
                showDateTimePicker(context, bookingViewModel)
            }) {
                Text("Select Date & Time")
            }
        }

        // Step 6: Show selected date & time summary
        bookingViewModel.selectedDateTime?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selected: ${bookingViewModel.formattedDateTime}")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Next step: location form and save to Firestore
                onBookingConfirmed()
            }) {
                Text("Continue to Booking Form")
            }
        }
    }
}

fun showDateTimePicker(context: Context, viewModel: BookingViewModel) {
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    viewModel.selectedDateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
