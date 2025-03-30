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
                // **Re-fetch the user to get the latest data**
                authManager.refreshUser()
                _profileState.value = ProfileState.Success(authManager.currentUser)
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
                val result = authManager.reauthenticateWithEmailPassword(email, password)
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
    // Delete user account after re-authenticating with the current password
    fun deleteUser(currentPassword: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                // Reauthenticate the user with the current password before deleting the account
                val result = authManager.reauthenticateWithEmailPassword(currentUser?.email ?: "", currentPassword)
                if (result.isSuccess) {
                    // Proceed to delete the user account if reauthentication is successful
                    authManager.deleteUser()
                    _profileState.value = ProfileState.Success(null) // Indicate no user after deletion
                } else {
                    _profileState.value = ProfileState.Error("Reauthentication failed")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to delete user account: ${e.message}")
            }
        }
    }

    // verify the password
    fun verifyPassword(currentPassword: String, param: (Any) -> Unit) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val result = authManager.reauthenticateWithEmailPassword(currentUser?.email ?: "", currentPassword)
                if (result.isSuccess) {
                    _profileState.value = ProfileState.Success(null) // Password verified successfully
                } else {
                    _profileState.value = ProfileState.Error("Password verification failed")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to verify password: ${e.message}")
            }
        }
    }

    // Send password reset email
    fun sendPasswordResetEmail(email: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                authManager.sendPasswordResetEmail(email)
                _profileState.value = ProfileState.Success(null) // Indicate success
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to send password reset email: ${e.message}")
            }
        }
    }
    // change password
    fun changePassword(currentPassword: String, newPassword: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                // Use authManager to update the password
                authManager.updatePassword(newPassword)
                _profileState.value = ProfileState.Success(null) // Password updated successfully
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update password: ${e.message}")
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
