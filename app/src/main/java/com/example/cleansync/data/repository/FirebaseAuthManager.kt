package com.example.cleansync.data.repository

import android.net.Uri
import android.util.Log
import com.example.cleansync.data.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Get current logged-in user
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    // Sign up with email and password
    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            firebaseUser?.let {
                // Set display name in Firebase Authentication
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name) // Set the user's name
                    .build()
                it.updateProfile(profileUpdates).await()

                // Save user details to Firestore
                saveUserToFirestore(it.uid, name, email)
            }
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign-up failed: ${e.message}")
            Result.failure(e)
        }
    }


    // Save user details to Firestore
    private suspend fun saveUserToFirestore(uid: String, name: String, email: String) {
        val user = User(uid = uid, name = name, email = email)
        firestore.collection("users").document(uid).set(user).await()
    }

    // Sign out user
    fun signOut() {
        auth.signOut()
    }

    // Update user's profile (display name, photo URL)
    suspend fun updateUserProfile(displayName: String, photoUri: Uri?) {
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUri(photoUri)
            .build()

        try {
            user?.updateProfile(profileUpdates)?.await()
            refreshUser() // Ensure we get the latest updated user
            Log.d("FirebaseAuthManager", "User profile updated and refreshed.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error updating profile: ${e.message}")
        }
    }


    // Reload user to get updated profile data
    suspend fun refreshUser() {
        val user = auth.currentUser
        try {
            user?.reload()?.await()
            Log.d("FirebaseAuthManager", "User refreshed successfully.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error refreshing user: ${e.message}")
        }
    }

    // Send email verification
    suspend fun sendVerificationEmail() {
        val user = auth.currentUser
        try {
            user?.sendEmailVerification()?.await()
            Log.d("FirebaseAuthManager", "Verification email sent.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error sending verification email: ${e.message}")
        }
    }

    // Update user's email
    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser
        try {
            user?.updateEmail(newEmail)?.await()
            Log.d("FirebaseAuthManager", "User email updated.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error updating email: ${e.message}")
        }
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
            Log.d("FirebaseAuthManager", "Password reset email sent.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error sending reset email: ${e.message}")
        }
    }

    // Delete user account
    suspend fun deleteUser() {
        val user = auth.currentUser
        try {
            user?.delete()?.await()
            Log.d("FirebaseAuthManager", "User account deleted.")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error deleting user: ${e.message}")
        }
    }

    // Re-authenticate user (useful for sensitive operations like updating email)
    suspend fun reauthenticateUser(email: String, password: String): Result<Boolean> {
        val user = auth.currentUser
        val credential = EmailAuthProvider.getCredential(email, password)

        return try {
            user?.reauthenticate(credential)?.await()
            Log.d("FirebaseAuthManager", "User re-authenticated.")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error re-authenticating user: ${e.message}")
            Result.failure(e)
        }
    }
}
