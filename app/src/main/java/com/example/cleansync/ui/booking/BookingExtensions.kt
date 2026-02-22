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


private const val PREFS_NAME = "calendar_bookings"
private const val KEY_ADDED = "added_bookings"

fun Booking.markAsAddedToCalendar(context: Context) {
    val bookingKey = this.id ?: return // Use 'id' as unique key, skip if null
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val addedSet = prefs.getStringSet(KEY_ADDED, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    addedSet.add(bookingKey)
    prefs.edit().putStringSet(KEY_ADDED, addedSet).apply()
}

fun Booking.isInCalendar(context: Context): Boolean {
    val bookingKey = this.id ?: return false // If null, treat as not added
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val addedSet = prefs.getStringSet(KEY_ADDED, mutableSetOf()) ?: mutableSetOf()
    return addedSet.contains(bookingKey)
}

// Extension function to add booking to calendar safely

fun Booking.addToCalendarSafe(context: Context): Boolean {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
        val startDateTime = LocalDateTime.parse(this.bookingDateTime, formatter)
        val startMillis = startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = startMillis + 60 * 60 * 1000 // 1 hour default

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "CleanSync Booking: ${this@addToCalendarSafe.name}")
            putExtra(CalendarContract.Events.DESCRIPTION, "Booking at ${this@addToCalendarSafe.streetAddress}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, this@addToCalendarSafe.streetAddress)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        }

        context.startActivity(intent)

        // Optionally store a flag locally that it was added
        this.markAsAddedToCalendar(context)

        Toast.makeText(context, "Booking added to calendar", Toast.LENGTH_SHORT).show()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error adding booking to calendar", Toast.LENGTH_SHORT).show()
        false
    }
}