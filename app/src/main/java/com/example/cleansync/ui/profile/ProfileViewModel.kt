package com.example.cleansync.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.net.Uri

class ProfileViewModel(
    private val authManager: FirebaseAuthManager = FirebaseAuthManager()
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> get() = _profileState

    val currentUser: FirebaseUser? get() = authManager.currentUser

    // Update user profile (display name, photo URL)
    fun updateUserProfile(displayName: String, photoUri: Uri?) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                authManager.updateUserProfile(displayName, photoUri)
                _profileState.value = ProfileState.Success(currentUser)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update profile: ${e.message}")
            }
        }
    }

    // Update user's email
    fun updateEmail(newEmail: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                authManager.updateEmail(newEmail)
                _profileState.value = ProfileState.Success(currentUser)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update email: ${e.message}")
            }
        }
    }

    // Send email verification
    fun sendVerificationEmail() {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                authManager.sendVerificationEmail()
                _profileState.value = ProfileState.Success(currentUser)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to send verification email: ${e.message}")
            }
        }
    }

    // Re-authenticate user before performing sensitive operations (e.g., updating email)
    fun reauthenticateUser(email: String, password: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val result = authManager.reauthenticateUser(email, password)
                if (result.isSuccess) {
                    _profileState.value = ProfileState.Success(currentUser)
                } else {
                    _profileState.value = ProfileState.Error("Reauthentication failed")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Reauthentication failed: ${e.message}")
            }
        }
    }

    // Delete user account
    fun deleteUser() {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                authManager.deleteUser()
                _profileState.value = ProfileState.Success(null) // Return null to indicate no user
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to delete user account: ${e.message}")
            }
        }
    }
}

// UI States for Profile
sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: FirebaseUser?) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
