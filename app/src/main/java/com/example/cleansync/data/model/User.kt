package com.example.cleansync.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val phoneNumber: String = "",
    val createdAt: Timestamp? = null,

    val loyaltyPoints: Int = 0,
    val totalSpent: Double = 0.0,
    val tier: LoyaltyTier = LoyaltyTier.BRONZE
)


enum class LoyaltyTier {
    BRONZE,
    SILVER,
    GOLD
}
