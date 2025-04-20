package com.example.cleansync.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.notifications.NotificationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authViewModel: AuthViewModel,
    private val notificationViewModel: NotificationViewModel
) : ViewModel() {

    private val _showEmailVerificationDialog = MutableStateFlow(false)
    val showEmailVerificationDialog: StateFlow<Boolean> = _showEmailVerificationDialog.asStateFlow()

    init {
        checkUserState()
    }

    val currentUser get() = authViewModel.currentUser
    val isLoggedIn get() = authViewModel.isLoggedIn
    val isEmailVerified get() = authViewModel.isEmailVerified

    fun unreadNotificationCount(): Int {
        return notificationViewModel.unreadNotificationsCount()
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
}
