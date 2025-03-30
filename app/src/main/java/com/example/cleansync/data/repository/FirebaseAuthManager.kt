package com.example.cleansync.data.repository

import android.net.Uri
import android.util.Log
import com.example.cleansync.data.model.User
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { Result.success(it) } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Google sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                it.updateProfile(profileUpdates).await()
                saveUserToFirestore(it.uid, name, email)
            }
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign-up failed: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun saveUserToFirestore(uid: String, name: String, email: String) {
        val user = User(uid = uid, name = name, email = email)
        firestore.collection("users").document(uid).set(user).await()
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun updateUserProfile(displayName: String, photoUri: Uri?) {
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUri(photoUri)
            .build()
        try {
            user?.updateProfile(profileUpdates)?.await()
            refreshUser()
            Log.d("FirebaseAuthManager", "User profile updated and refreshed.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error updating profile: ${e.message}")
        }
    }

    // Update user's email
    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser
        try {
            user?.updateEmail(newEmail)?.await()
            refreshUser()
            Log.d("FirebaseAuthManager", "User email updated and refreshed.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error updating email: ${e.message}")
        }
    }
    // Update user's password
    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser
        try {
            user?.updatePassword(newPassword)?.await()
            Log.d("FirebaseAuthManager", "User password updated.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error updating password: ${e.message}")
        }
    }

    suspend fun refreshUser() {
        try {
            auth.currentUser?.reload()?.await()
            Log.d("FirebaseAuthManager", "User refreshed successfully.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error refreshing user: ${e.message}")
        }
    }

    suspend fun sendVerificationEmail() {
        try {
            auth.currentUser?.sendEmailVerification()?.await()
            Log.d("FirebaseAuthManager", "Verification email sent.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error sending verification email: ${e.message}")
        }
    }

    suspend fun sendPasswordResetEmail(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await() // Converts the callback into a suspending function
            Log.d("FirebaseAuthManager", "Password reset email sent.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error sending reset email: ${e.message}")
            throw e // Propagate the error up to be handled in the ViewModel
        }
    }


    suspend fun deleteUser(email: String? = null, password: String? = null, googleCredential: AuthCredential? = null) {
        val user = auth.currentUser ?: return
        try {
            val isReauthenticated = when {
                email != null && password != null -> reauthenticateWithEmailPassword(email, password).getOrNull() == true
                googleCredential != null -> reauthenticateWithGoogle(googleCredential).getOrNull() == true
                else -> false
            }

            if (isReauthenticated) {
                user.delete().await()
                Log.d("FirebaseAuthManager", "User account deleted.")
            } else {
                Log.e("FirebaseAuthManager", "Re-authentication failed.")
                throw Exception("Re-authentication failed")
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error deleting user: ${e.message}")
            throw e // Re-throw exception to handle in ViewModel or UI
        }
    }


    suspend fun reauthenticateWithEmailPassword(email: String, password: String): Result<Boolean> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.reauthenticate(credential)?.await()
            Log.d("FirebaseAuthManager", "User re-authenticated with email/password.")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error re-authenticating with email/password: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun reauthenticateWithGoogle(googleCredential: AuthCredential): Result<Boolean> {
        return try {
            auth.currentUser?.reauthenticate(googleCredential)?.await()
            Log.d("FirebaseAuthManager", "User re-authenticated with Google.")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error re-authenticating with Google: ${e.message}")
            Result.failure(e)
        }
    }


}
