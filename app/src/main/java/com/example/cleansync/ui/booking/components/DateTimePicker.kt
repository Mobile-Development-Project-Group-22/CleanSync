// components/DateTimePicker.kt
package com.example.cleansync.ui.booking.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import com.example.cleansync.ui.booking.BookingViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

fun showCustomDateTimePicker(
    context: Context,
    bookingViewModel: BookingViewModel
) {
    val now = Calendar.getInstance()

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val selectedDate = LocalDate.of(year, month + 1, day)

            // Custom hour-only picker
            val hourPicker = TimePickerDialog(
                context,
                { _, hourOfDay, _ ->
                    if (hourOfDay in 10..17) {
                        val selectedDateTime = LocalDateTime.of(
                            selectedDate,
                            LocalTime.of(hourOfDay, 0) // Always set minutes to 0
                        )

                        if (selectedDateTime.isBefore(LocalDateTime.now())) {
                            bookingViewModel.errorMessage = "Selected time is in the past."
                        } else {
                            bookingViewModel.updateSelectedDateTime(selectedDateTime)
                            bookingViewModel.errorMessage = null
                        }
                    } else {
                        bookingViewModel.errorMessage = "Please select a time between 10 AM and 6 PM."
                    }
                },
                maxOf(10, now.get(Calendar.HOUR_OF_DAY)), // start from current hour or 10
                0, // Always start at minute 0
                true
            )

            hourPicker.show()

            // Try hiding the minute picker (not officially supported, but set to 0 anyway)
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    )

    datePicker.datePicker.minDate = now.timeInMillis
    datePicker.show()
}
