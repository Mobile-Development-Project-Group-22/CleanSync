package com.example.cleansync.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date


object DateTimeUtils {
    /**
     * Safely converts a LocalDateTime to milliseconds since epoch (API 24+ compatible).
     */
    fun LocalDateTime?.toEpochMillis(): Long {
        return this?.let {
            Date.from(it.atZone(ZoneId.systemDefault()).toInstant()).time
        } ?: 0L // Default to 0 if LocalDateTime is null
    }
}