package com.example.cleansync.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.FirebaseAuthManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val authManager = FirebaseAuthManager()

    // State variables for UI
    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    private var _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Fetch user name from Firebase or other source
    fun getUserName() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = authManager.getCurrentUser() // Fetch the current logged-in user
                _userName.value = user?.displayName ?: "No Name"
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
                _isLoading.value = false
            }
        }
    }

    // Fetch user email from Firebase
    fun getUserEmail() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = authManager.getCurrentUser()
                _userEmail.value = user?.email
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
                _isLoading.value = false
            }
        }
    }



    fun updateProfile(newUserName: String, newUserEmail: String, currentPassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = authManager.getCurrentUser()
                user?.let {
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                    // Re-authenticate the user
                    it.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // Update name first
                            try {
                                it.updateProfile(userProfileChangeRequest { displayName = newUserName })
                                _userName.value = newUserName
                            } catch (e: Exception) {
                                _errorMessage.value = "Error updating name: ${e.localizedMessage}"
                                Log.e("ProfileViewModel", "Error updating name: ${e.localizedMessage}")
                            }

                            // Update email
                            try {
                                it.updateEmail(newUserEmail)
                                _userEmail.value = newUserEmail
                            } catch (e: Exception) {
                                _errorMessage.value = "Error updating email: ${e.localizedMessage}"
                            }
                        } else {
                            _errorMessage.value = "Re-authentication failed: ${reauthTask.exception?.localizedMessage}"
                        }
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating profile: ${e.localizedMessage}"
                _isLoading.value = false
            }
        }
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }


}
