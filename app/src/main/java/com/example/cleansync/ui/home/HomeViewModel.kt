package com.example.cleansync.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.model.Booking
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeViewModel(
    private val authViewModel: AuthViewModel,
    private val notificationViewModel: NotificationViewModel
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _showEmailVerificationDialog = MutableStateFlow(false)
    val showEmailVerificationDialog: StateFlow<Boolean> = _showEmailVerificationDialog.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    val completedBookings: StateFlow<Int> = bookings
        .map { bookingList ->
            bookingList.count { booking ->
                LocalDateTime.parse(
                    booking.bookingDateTime,
                    DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
                ).isBefore(LocalDateTime.now())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val loyaltyPoints = MutableStateFlow(0) // Initialize with your loyalty system logic

    val bookingsByDate: StateFlow<Map<LocalDate, List<Booking>>> = bookings
        .map { bookingList ->
            bookingList.groupBy { booking ->
                LocalDateTime.parse(
                    booking.bookingDateTime,
                    DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
                ).toLocalDate()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    val currentUser get() = authViewModel.currentUser
    val isLoggedIn get() = authViewModel.isLoggedIn
    val isEmailVerified get() = authViewModel.isEmailVerified

    init {
        checkUserState()
        fetchUserBookings()
        calculateLoyaltyPoints()
    }

    private fun checkUserState() {
        viewModelScope.launch {
            if (!authViewModel.isLoggedIn) {
                authViewModel.signOut()
            } else if (!authViewModel.isEmailVerified) {
                _showEmailVerificationDialog.value = true
            }
        }
    }

    fun refreshBookings() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                fetchUserBookings()
                calculateLoyaltyPoints()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun fetchUserBookings() {
        val userId = authViewModel.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val result = db.collection("bookings")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val bookingList = result.mapNotNull { doc ->
                    try {
                        val booking = doc.toObject(Booking::class.java)
                        booking.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.filter { booking ->
                    // Only show upcoming bookings (future bookings)
                    try {
                        val bookingDateTime = LocalDateTime.parse(
                            booking.bookingDateTime,
                            DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
                        )
                        bookingDateTime.isAfter(LocalDateTime.now())
                    } catch (e: Exception) {
                        false
                    }
                }
                _bookings.value = bookingList
            } catch (e: Exception) {
                // Handle error (could emit to a separate error state if needed)
            }
        }
    }

    private fun calculateLoyaltyPoints() {
        viewModelScope.launch {
            // Replace with your actual loyalty points calculation logic
            // For example: 10 points per completed booking
            loyaltyPoints.value = completedBookings.value * 10
        }
    }

    fun resendVerificationEmail() {
        authViewModel.resendVerificationEmail()
    }

    fun dismissEmailDialog() {
        _showEmailVerificationDialog.value = false
    }

    fun signOut() {
        authViewModel.signOut()
    }

    fun unreadNotificationCount(): Int {
        return notificationViewModel.unreadNotificationsCount()
    }
}