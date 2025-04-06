package com.example.cleansync.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.ProfileManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileManager: ProfileManager = ProfileManager()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> get() = _profileState

    val currentUser = profileManager.currentUser

    // Update user profile
    fun updateUserProfile(displayName: String, photoUri: Uri?) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateUserProfile(displayName, photoUri)
                _profileState.value = ProfileState.Success(profileManager.currentUser)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update profile: ${e.message}")
            }
        }
    }

    // Update email
    fun updateEmail(newEmail: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateEmail(newEmail)
                _profileState.value = ProfileState.Success(profileManager.currentUser)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update email: ${e.message}")
            }
        }
    }

    // Change password
    fun changePassword(currentPassword: String, newPassword: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updatePassword(newPassword)
                _profileState.value = ProfileState.Success(null)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update password: ${e.message}")
            }
        }
    }

    fun reAuthenticateAndDeleteUser(
        password: String?,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential =
            if (user?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true) {
                val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
                val idToken = googleSignInAccount?.idToken
                GoogleAuthProvider.getCredential(idToken, null)
            } else {
                // Handle email/password authentication
                val email = user?.email ?: return
                EmailAuthProvider.getCredential(email, password ?: "")
            }

        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user?.delete()?.addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("Failed to delete account")
                    }
                }
            } else {
                onFailure("Reauthentication failed")
            }
        }
    }

    fun reAuthenticateWithGoogle(
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
            val idToken = googleSignInAccount?.idToken

            if (idToken != null) {
                val googleCredential = GoogleAuthProvider.getCredential(idToken, null)

                user.reauthenticate(googleCredential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure("Re-authentication failed. Please sign in again.")
                        Log.e(
                            "ProfileViewModel",
                            "Re-authentication failed: ${task.exception?.message}"
                        )
                    }
                }
            } else {
                onFailure("Google Sign-In token is missing.")
                Log.e("ProfileViewModel", "Google Sign-In token is missing.")
            }
        } else {
            onFailure("No user is currently signed in.")
            Log.e("ProfileViewModel", "No user is currently signed in.")
        }
    }

    private fun reAuthenticateWithEmailPassword(
        user: FirebaseUser,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = EmailAuthProvider.getCredential(user.email ?: "", password)

        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Proceed to delete user
                user.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        onSuccess() // Success callback
                    } else {
                        onFailure("Failed to delete account")
                    }
                }
            } else {
                onFailure("Re-authentication failed. Please check your password.")
                Log.e("ProfileViewModel", "Re-authentication failed: ${task.exception?.message}")
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
