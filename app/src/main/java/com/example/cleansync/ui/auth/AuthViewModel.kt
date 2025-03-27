package com.example.cleansync.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    private val authManager = FirebaseAuthManager()

    // State variables for UI
    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _resetPasswordState = MutableStateFlow<AuthState>(AuthState.Idle)
    val resetPasswordState: StateFlow<AuthState> = _resetPasswordState

    fun registerUser(name: String, email: String, password: String) {
        _registerState.value = AuthState.Loading  // Set loading state

        val validationMessage = validateInput(name, email, password)
        if (validationMessage != null) {
            _registerState.value = AuthState.Error(validationMessage.toString())
            return
        }

        viewModelScope.launch {
            val result = authManager.registerUser(name, email, password)

            if (result.isSuccess) {
                _registerState.value = AuthState.Success(result.getOrNull())
            } else {
                _registerState.value = AuthState.Error(
                    "Registration Error: " + (result.exceptionOrNull()?.message
                        ?: "Registration failed")
                )
            }
        }
    }

    fun validateInput(name: String, email: String, password: String): String? {
        return when {
            name.isEmpty() -> "Name cannot be empty"
            email.isEmpty() -> "Email cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            password.isEmpty() -> "Password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null // Input is valid
        }
    }

    fun loginUser(email: String, password: String) {
        _loginState.value = AuthState.Loading  // Set loading state

        // Call the suspend function within a coroutine
        viewModelScope.launch {
            val result = authManager.loginUser(email, password)

            if (result.isSuccess) {
                val user = result.getOrNull()
                _loginState.value = AuthState.Success(user)
            } else {
                _loginState.value =
                    AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authManager.logoutUser()
                _loginState.value = AuthState.Success(null)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout failed: ${e.message}")
            }
        }
    }

     fun resetPassword(email: String) {
        _resetPasswordState.value = AuthState.Loading
        try {
            val task = authManager.sendPasswordResetEmail(email)
            task.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _resetPasswordState.value = AuthState.Success(null)
                } else {
                    _resetPasswordState.value =
                        AuthState.Error(task.exception?.message ?: "Password reset failed")
                }
            }
        } catch (e: Exception) {
            _resetPasswordState.value = AuthState.Error("Error: ${e.message}")
        }
    }
}

    // UI States for Authentication
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}
