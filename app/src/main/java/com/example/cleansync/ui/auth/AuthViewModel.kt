package com.example.cleansync.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.AuthManager
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: AuthManager = AuthManager(),
) : ViewModel() {

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class SignupSuccess(val user: FirebaseUser) : AuthState()
        data class LoginSuccess(val user: FirebaseUser) : AuthState()
        data class PasswordResetSent(val email: String) : AuthState()
        data class Error(val message: String, val type: ErrorType) : AuthState()
    }

    enum class ErrorType {
        SIGNUP, LOGIN, PASSWORD_RESET, GOOGLE_SIGNIN, GENERAL
    }

    val isLoggedIn : Boolean
        get() = authManager.currentUser != null
    val isEmailVerified: Boolean
        get() = authManager.currentUser?.isEmailVerified ?: false

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: FirebaseUser? get() = authManager.currentUser

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
                val result = authManager.signInWithCredential(credential)

                result.fold(
                    onSuccess = { user ->
                        if (user.isEmailVerified) {
                            _authState.value = AuthState.LoginSuccess(user)
                        } else {
                            _authState.value = AuthState.Error(
                                "Please verify your email first",
                                ErrorType.GOOGLE_SIGNIN
                            )
                        }
                    },
                    onFailure = { error ->
                        _authState.value = AuthState.Error(
                            error.message ?: "Google authentication failed",
                            ErrorType.GOOGLE_SIGNIN
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "Error during Google sign-in: ${e.message}",
                    ErrorType.GOOGLE_SIGNIN
                )
            }
        }
    }

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authManager.signInWithEmailAndPassword(email, password)
            result.fold(
                onSuccess = { user ->
                    if (user?.isEmailVerified == true) {
                        _authState.value = AuthState.LoginSuccess(user)
                    } else {
                        _authState.value = AuthState.Error(
                            "Please verify your email before logging in",
                            ErrorType.LOGIN
                        )
                    }
                },
                onFailure = { error ->
                    val errorMessage = when (error) {
                        is FirebaseAuthInvalidUserException -> "Account not found. Please sign up."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid credentials. Please try again."
                        else -> error.message ?: "Login failed"
                    }
                    _authState.value = AuthState.Error(errorMessage, ErrorType.LOGIN)
                }
            )
        }
    }

    fun signUp(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authManager.signUp(name, email, password)
            result.fold(
                onSuccess = { user ->
                    user?.let {
                        it.sendEmailVerification()
                        _authState.value = AuthState.SignupSuccess(it)
                    } ?: run {
                        _authState.value = AuthState.Error("User creation failed", ErrorType.SIGNUP)
                    }
                },
                onFailure = { error ->
                    val errorMessage = when (error) {
                        is FirebaseAuthUserCollisionException -> "Email already in use"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                        else -> error.message ?: "Signup failed"
                    }
                    _authState.value = AuthState.Error(errorMessage, ErrorType.SIGNUP)
                }
            )
        }
    }

    fun sendPasswordResetEmail(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                authManager.sendPasswordResetEmail(email)
                _authState.value = AuthState.PasswordResetSent(email)
            } catch (e: FirebaseAuthInvalidUserException) {
                _authState.value = AuthState.Error(
                    "No account found with this email",
                    ErrorType.PASSWORD_RESET
                )
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.Error(
                    "Invalid email format",
                    ErrorType.PASSWORD_RESET
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "Error sending password reset email: ${e.message}",
                    ErrorType.PASSWORD_RESET
                )
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _authState.value = AuthState.Idle
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}