package com.example.cleansync.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    // Sign in with email and password
    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authManager.signIn(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(it.message ?: "Unknown error") }
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

    // Reauthenticate user for sensitive operations (e.g., updating email, password, or deleting account)
    fun reauthenticateUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authManager.reauthenticateUser(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(null) },  // Re-authentication successful
                onFailure = { AuthState.Error(it.message ?: "Re-authentication failed") }
            )
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