package com.example.cleansync.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleansync.data.repository.ProfileManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val profileManager: ProfileManager = ProfileManager()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> get() = _profileState

    val currentUser = profileManager.currentUser

    // check if the user's email is verified
    val isEmailVerified: Boolean
        get() = currentUser?.isEmailVerified ?: false

    fun updateDisplayName(displayName: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateDisplayName(displayName)
                _profileState.value = ProfileState.Success(profileManager.currentUser)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update name: ${e.message}")
            }
        }
    }
    // Upload profile picture
    fun uploadProfileImage(
        uri: Uri,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not logged in")
                val storageRef: StorageReference =
                    FirebaseStorage.getInstance().reference.child("profile_pictures/${user.uid}")

                // Upload the image
                storageRef.putFile(uri).await()

                // Get the download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // Update the user's Firebase Auth profile
                val profileUpdates = userProfileChangeRequest {
                    photoUri = Uri.parse(downloadUrl)
                }
                user.updateProfile(profileUpdates).await()

                // Update Firestore with the new photo URL
                profileManager.updateUserPhotoUrlInFirestore(downloadUrl)

                // Trigger success callback with updated user
                onSuccess(profileManager.currentUser)
            } catch (e: Exception) {
                // Trigger failure callback with error message
                onFailure(e.message ?: "Failed to upload image")
                Log.e("ProfileViewModel", "Error uploading profile image: ${e.message}")
            }
        }
    }


    // Update profile picture
    fun updateProfilePicture(photoUri: Uri) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateProfilePicture(photoUri)
                _profileState.value = ProfileState.Success(profileManager.currentUser)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update picture: ${e.message}")
            }
        }
    }

    // Update email
    fun updateEmail(newEmail: String) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateEmail(newEmail) // Sync with Firebase and Firestore
                _profileState.value = ProfileState.Success(profileManager.currentUser)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to update email: ${e.message}")
            }
        }
    }

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

    fun addPasswordToUser(password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser ?: throw Exception("User not logged in")

                user.updatePassword(password).await()  // Use kotlinx-coroutines-play-services for await()

                onSuccess()
                _profileState.value = ProfileState.Success(profileManager.currentUser)
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
                    } else if (deleteTask.exception?.message == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") {
                        onFailure("Network error. Please try again.")
                        Log.e("ProfileViewModel", "Network error: ${deleteTask.exception?.message}")
                    } else if (deleteTask.exception?.message == "The password is invalid or the user does not have a password.") {
                        onFailure("Invalid password. Please try again.")
                        Log.e("ProfileViewModel", "Invalid password: ${deleteTask.exception?.message}")
                    } else {
                        onFailure("Failed to delete account")
                        Log.e("ProfileViewModel", "Failed to delete account: ${deleteTask.exception?.message}")
                    }
                }
            } else {
                onFailure("Reauthentication failed")
                Log.e("ProfileViewModel", "Reauthentication failed: ${task.exception?.message}")
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

    fun signOut() {
        viewModelScope.launch {
            try {
                profileManager.signOut()
                _profileState.value = ProfileState.Idle
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to sign out: ${e.message}")
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