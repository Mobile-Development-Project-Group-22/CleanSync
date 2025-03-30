package com.example.cleansync.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.FirebaseAuthManager
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel(
    private val authManager: FirebaseAuthManager = FirebaseAuthManager()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> get() = _authState

    val currentUser: FirebaseUser? get() = authManager.currentUser

    // Derived state to check if user is logged in
    val isLoggedIn: StateFlow<Boolean> = _authState
        .map { it is AuthState.Success && it.user != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, currentUser != null)

    // Sign in with Google
    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
                val result = authManager.signInWithCredential(credential)

                _authState.value = result.fold(
                    onSuccess = { AuthState.Success(it) },
                    onFailure = { AuthState.Error(it.message ?: "Google authentication failed") }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error occurred during Google sign-in: ${e.message}")
            }
        }
    }

    // Sign in with email and password
    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authManager.signIn(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = {
                    val errorMessage = when (it) {
                        is FirebaseAuthInvalidUserException -> "User not found. Please sign up."
                        is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                        else -> it.message ?: "Authentication failed"
                    }
                    AuthState.Error(errorMessage)
                }
            )
        }
    }

    // Sign up with name, email, and password
    fun signUp(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authManager.signUp(name, email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    // Sign out user
    fun signOut() {
        authManager.signOut()
        _authState.value = AuthState.Idle
    }
    fun sendPasswordResetEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // Reauthenticate the user with email and password
                val reauthenticationResult = authManager.reauthenticateWithEmailPassword(email, password)

                if (reauthenticationResult.isSuccess) {
                    // If reauthentication is successful, send password reset email
                    authManager.sendPasswordResetEmail(email)
                    _authState.value = AuthState.Success(null) // Password reset email sent
                } else {
                    _authState.value = AuthState.Error("Reauthentication failed. Please check your credentials.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An error occurred: ${e.message}")
            }
        }
    }

    // Reauthenticate user for sensitive operations (e.g., updating email, password, or deleting account)
    fun reauthenticateUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authManager.reauthenticateWithEmailPassword(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(null) },  // Re-authentication successful
                onFailure = { AuthState.Error(it.message ?: "Re-authentication failed") }
            )
        }
    }

    // change password
    fun changePassword(currentPassword: String, newPassword: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // Use authManager to update the password
                authManager.updatePassword(newPassword)
                _authState.value = AuthState.Success(null) // Password updated successfully
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to update password: ${e.message}")
            }
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

