package com.example.cleansync.data.model

import com.google.firebase.Timestamp

data class Notification(
    var id: String = "",
    val userId: String = "",
    val message: String = "",
    val read: Boolean = false,
    val timestamp: com.google.firebase.Timestamp? = null
)