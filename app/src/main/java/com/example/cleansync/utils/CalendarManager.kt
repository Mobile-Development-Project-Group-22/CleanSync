package com.example.cleansync.utils

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import android.util.Log

class CalendarManager(private val context: Context) {

    // Add event to primary calendar
    fun addEvent(
        title: String,
        description: String,
        location: String,
        startMillis: Long,
        endMillis: Long,
        reminderMinutes: Int = 60,
        recurrenceRule: String? = null
    ): Long? {

        val calendarId = getPrimaryCalendarId()
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
            recurrenceRule?.let { put(CalendarContract.Events.RRULE, it) }
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLong()

            // Add reminder
            eventId?.let {
                val reminderValues = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, it)
                    put(CalendarContract.Reminders.MINUTES, reminderMinutes)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
            }

            eventId

        } catch (e: Exception) {
            Log.e("CalendarManager", "Failed to insert event", e)
            null
        }
    }

    // Delete event if needed
    fun deleteEvent(eventId: Long) {
        try {
            context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                "${CalendarContract.Events._ID}=?",
                arrayOf(eventId.toString())
            )
        } catch (e: Exception) {
            Log.e("CalendarManager", "Failed to delete event", e)
        }
    }

    // Get primary calendar ID
    private fun getPrimaryCalendarId(): Long {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY
        )
        val uri = CalendarContract.Calendars.CONTENT_URI
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val isPrimary = cursor.getInt(1)
                if (isPrimary == 1) return id
            }
        }
        return 1L // fallback if no primary
    }
}