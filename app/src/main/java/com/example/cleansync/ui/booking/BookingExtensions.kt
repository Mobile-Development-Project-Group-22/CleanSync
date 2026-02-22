package com.example.cleansync.ui.booking

import android.content.Context
import android.widget.Toast
import com.example.cleansync.model.Booking
import com.example.cleansync.utils.CalendarManager
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.provider.CalendarContract
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun Booking.getStartMillis(): Long? {
    return try {
        val format = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
        format.parse(this.bookingDateTime)?.time
    } catch (e: Exception) {
        null
    }
}

fun Booking.getEndMillis(): Long? {
    // Default 1-hour booking
    return getStartMillis()?.plus(60 * 60 * 1000)
}




// Extension function to add booking to calendar safely

fun Booking.addToCalendarSafe(context: Context) {
    try {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
        val startDateTime = LocalDateTime.parse(this.bookingDateTime, formatter)

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "CleanSync Booking: ${this@addToCalendarSafe.name}")
            putExtra(CalendarContract.Events.DESCRIPTION, "Booking at ${this@addToCalendarSafe.streetAddress}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, this@addToCalendarSafe.streetAddress)
            putExtra(
                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                startDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )

            // Ensure it works from non-Activity context
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Verify there is an activity to handle this intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No calendar app found", Toast.LENGTH_SHORT).show()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error adding booking to calendar", Toast.LENGTH_SHORT).show()
    }
}