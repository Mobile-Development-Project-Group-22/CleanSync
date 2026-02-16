package com.example.cleansync.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.ProfileManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri
import com.example.cleansync.data.model.LoyaltyTier
import com.example.cleansync.data.service.loyalty.LoyaltyManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest

class ProfileViewModel(
    private val profileManager: ProfileManager = ProfileManager()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> get() = _profileState

    val currentUser = profileManager.currentUser


    // Loyalty state for the current user
    private val _loyaltyState = MutableStateFlow(LoyaltyUiState())
    val loyaltyState: StateFlow<LoyaltyUiState> = _loyaltyState

    // check if the user's email is verified
    val isEmailVerified: Boolean
        get() = currentUser?.isEmailVerified ?: false



    fun calculateProgress(totalSpent: Double, tier: LoyaltyTier): Float {
        return when (tier) {
            LoyaltyTier.BRONZE -> (totalSpent / 500).toFloat().coerceIn(0f, 1f)
            LoyaltyTier.SILVER -> ((totalSpent - 500) / 500).toFloat().coerceIn(0f, 1f)
            LoyaltyTier.GOLD -> 1f
        }
    }

    fun loadLoyaltyData() {
        viewModelScope.launch {
            try {
                val user = profileManager.getUserProfile() ?: return@launch

                val progress = LoyaltyManager.calculateProgress(
                    user.totalSpent,
                    user.tier
                )

                _loyaltyState.value = LoyaltyUiState(
                    loyaltyPoints = user.loyaltyPoints,
                    totalSpent = user.totalSpent,
                    tier = user.tier,
                    progressToNextTier = progress
                )

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading loyalty: ${e.message}")
            }
        }
    }


    // Update user display name
    fun updateDisplayName(displayName: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateDisplayName(displayName)
                _profileState.value = ProfileState.Success("Display name updated successfully")
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update name: ${e.message}")
            }
        }
    }

    fun uploadProfileImage(
        uri: Uri,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not logged in")
                val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/${user.uid}")

                val uploadTaskSnapshot = storageRef.putFile(uri).await()

                if (uploadTaskSnapshot.metadata == null) {
                    throw Exception("Upload failed. Metadata is null.")
                }

                val downloadUrl = storageRef.downloadUrl.await().toString()

                val profileUpdates = userProfileChangeRequest {
                    photoUri = downloadUrl.toUri()
                }
                user.updateProfile(profileUpdates).await()

                user.reload().await()

                profileManager.updateUserPhotoUrlInFirestore(downloadUrl)

                onSuccess(user)
                _profileState.value = ProfileState.Success("Profile picture updated successfully")

            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to upload image")
                Log.e("ProfileViewModel", "Error uploading profile image: ${e.message}")
                _profileState.value = ProfileState.Error("Error uploading image: ${e.message}")
            }
        }
    }

    // Update profile picture
    fun updateProfilePicture(photoUri: Uri) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateProfilePicture(photoUri)
                _profileState.value = ProfileState.Success("Profile picture updated successfully")
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update picture: ${e.message}")
            }
        }
    }

    // Update user email
    fun updateEmail(newEmail: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateEmail(newEmail)
                _profileState.value = ProfileState.Success("Email updated successfully")
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update email: ${e.message}")
            }
        }
    }

    // Delete user password
    fun deletePassword(
        currentPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not logged in")

                // Reauthenticate first
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()

                // Remove password provider
                user.unlink(EmailAuthProvider.PROVIDER_ID).await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to delete password")
            }
        }
    }

    // Add password to user
    fun addPasswordToUser(password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not logged in")

                user.updatePassword(password).await()  // Use kotlinx-coroutines-play-services for await()

                onSuccess()
                _profileState.value = ProfileState.Success("Password updated successfully")
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to update password"
                onFailure(errorMsg)
                _profileState.value = ProfileState.Error(errorMsg)
            }
        }
    }

    // Change password
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not logged in")

                // Reauthenticate first
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()

                // Update password
                user.updatePassword(newPassword).await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed to change password")
            }
        }
    }

    // Re-authenticate user and delete account
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
                user.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        onSuccess()
                        // Optionally, you can sign out the user after deletion
                        FirebaseAuth.getInstance().signOut()
                    } else {
                        onFailure("Failed to delete account")
                    }
                }
            } else {
                onFailure("Reauthentication failed")
            }
        }
    }

    // Re-authenticate with Google
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
                    }
                }
            } else {
                onFailure("Google Sign-In token is missing.")
            }
        } else {
            onFailure("No user is currently signed in.")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                profileManager.signOut()
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to sign out: ${e.message}")
            }
        }
    }
}

// UI States for Profile
sealed class ProfileState {
    object Idle : ProfileState() // Initial state
    object Loading : ProfileState() // Represents a loading state
    data class Success(val message: String) : ProfileState() // Represents success
    data class Error(val error: String) : ProfileState() // Represents error
}


data class LoyaltyUiState(
    val loyaltyPoints: Int = 0,
    val totalSpent: Double = 0.0,
    val tier: LoyaltyTier = LoyaltyTier.BRONZE,
    val progressToNextTier: Float = 0f
)
