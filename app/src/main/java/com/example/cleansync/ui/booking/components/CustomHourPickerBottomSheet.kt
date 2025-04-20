// Booking/components/CustomHourPickerBottomSheet.kt
package com.example.cleansync.ui.booking.components

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.booking.BookingViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateAndHourPicker(
    context: Context,
    bookingViewModel: BookingViewModel
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    // Launch date picker first
    LaunchedEffect(Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
                selectedDate = pickedDate
                showSheet = true
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = now.timeInMillis
        }.show()
    }

    if (showSheet && selectedDate != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time (10 AM - 6 PM)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Hour buttons
                val hours = (10..17).toList()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    hours.chunked(4).forEach { rowHours ->
                        Column(modifier = Modifier.weight(1f)) {
                            rowHours.forEach { hour ->
                                OutlinedButton(
                                    onClick = {
                                        val dateTime = LocalDateTime.of(
                                            selectedDate!!,
                                            LocalTime.of(hour, 0)
                                        )
                                        if (dateTime.isBefore(LocalDateTime.now())) {
                                            bookingViewModel.errorMessage = "Time is in the past!"
                                        } else {
                                            bookingViewModel.updateSelectedDateTime(dateTime)
                                            bookingViewModel.errorMessage = null
                                        }
                                        coroutineScope.launch {
                                            bottomSheetState.hide()
                                            showSheet = false
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    val displayHour = if (hour > 12) "${hour - 12} PM" else "$hour AM"
                                    Text(displayHour)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
