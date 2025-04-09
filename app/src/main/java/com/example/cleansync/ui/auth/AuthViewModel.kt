package com.example.cleansync.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.AuthManager
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class AuthViewModel(
    private val authManager: AuthManager = AuthManager(),
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
            val result = authManager.signInWithEmailAndPassword(email, password)
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

    fun signUp(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authManager.signUp(name, email, password)
            _authState.value = result.fold(
                onSuccess = {
                    if (it != null) {
                        AuthState.Success(it)
                    } else {
                        AuthState.Error("User creation failed")
                    }
                },
                onFailure = { AuthState.Error(it.message ?: "Unknown error") }
            )
        }
    }


    // Sign out user
    fun signOut() {
        authManager.signOut()
        _authState.value = AuthState.Idle
    }

    // Send password reset email
    fun sendPasswordResetEmail(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                authManager.sendPasswordResetEmail(email)


                _authState.value = AuthState.Success(null) // Indicate success
            } catch (e: FirebaseAuthInvalidUserException) {
                _authState.value = AuthState.Error("No account found with this email")
                Log.e("AuthViewModel", "No account found with this email", e)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Error("Invalid email format")
                Log.e("AuthViewModel", "Invalid email format", e)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error sending password reset email: ${e.message}")
            }
        }
    }

    // Reauthenticate user for sensitive operations (e.g., updating email, password, or deleting account)
//    fun reauthenticateUser(email: String, password: String) {
//        _authState.value = AuthState.Loading
//        viewModelScope.launch {
//            val result = authManager.reauthenticateWithEmailPassword(email, password)
//            _authState.value = result.fold(
//                onSuccess = { AuthState.Success(null) },  // Re-authentication successful
//                onFailure = { AuthState.Error(it.message ?: "Re-authentication failed") }
//            )
//        }
//    }


}

// UI States for Authentication
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

