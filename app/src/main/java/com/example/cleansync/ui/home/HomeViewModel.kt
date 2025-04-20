package com.example.cleansync.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.model.Booking
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
    val bookingsByDate: StateFlow<Map<LocalDate, List<Booking>>> = bookings
        .map { bookingList ->
            bookingList.groupBy { booking ->
                LocalDateTime.parse(
                    booking.bookingDateTime,
                    DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm")
                ).toLocalDate()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val currentUser get() = authViewModel.currentUser
    val isLoggedIn get() = authViewModel.isLoggedIn
    val isEmailVerified get() = authViewModel.isEmailVerified

    init {
        checkUserState()
        fetchUserBookings()
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

    fun fetchUserBookings() {
        val userId = authViewModel.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val bookingList = result.mapNotNull { doc ->
                    try {
                        val booking = doc.toObject(Booking::class.java)
                        booking.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                _bookings.value = bookingList
            }
            .addOnFailureListener { exception ->
                // Log or handle the error if needed
            }
    }
}
