package com.example.cleansync.data.service.loyalty

import com.example.cleansync.data.model.LoyaltyTier

object LoyaltyManager {
    fun calculateProgress(totalSpent: Double, tier: LoyaltyTier): Float {
        return when (tier) {
            LoyaltyTier.BRONZE -> (totalSpent / 500).toFloat().coerceIn(0f, 1f)
            LoyaltyTier.SILVER -> ((totalSpent - 500) / 500).toFloat().coerceIn(0f, 1f)
            LoyaltyTier.GOLD -> 1f
        }
    }
}
