package com.example.cleansync.model

data class Booking(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val streetAddress: String = "",
    val postalCode: String = "",
    val city: String = "",
    val length: String = "",
    val width: String = "",
    val estimatedPrice: Float = 0f,
    val bookingDateTime: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var id: String? = null // 🔥 Add this line to hold Firestore document ID
)
