package com.example.cleansync.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val phoneNumber: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)