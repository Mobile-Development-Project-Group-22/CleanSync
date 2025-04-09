package com.example.cleansync.data.model

import com.google.firebase.Timestamp

data class NotificationState(
    var id: String = "", // Make sure id has a default value
    val title: String = "", // Add default value if needed
    val message: String = "", // Add default value if needed
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)