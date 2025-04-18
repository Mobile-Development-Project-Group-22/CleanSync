package com.example.cleansync.data.repository

import android.net.Uri
import android.util.Log
import com.example.cleansync.data.model.User
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Sign in using an authentication credential (Google, Facebook, etc.)
    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { Result.success(it) } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            handleAuthError<FirebaseUser>("Google sign-in failed", e)
        }
    }

    // Sign in with email and password
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            handleAuthError<FirebaseUser?>("Sign-in failed", e)
        }
    }

    // resend email verification
    suspend fun resendEmailVerification(email: String): Result<Unit> {
        return try {
            val user = auth.currentUser
            user?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleAuthError<Unit>("Failed to send email verification", e)
        }
    }

    // Sign up a new user with email and password
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        profileImageUrl: String = "",
        phoneNumber: String = ""
    ): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            result.user?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                // Update user profile and send email verification
                user.updateProfile(profileUpdates).await()

                // Save user data to Firestore
                saveUserToFirestore(user.uid, name, email, profileImageUrl, phoneNumber)
            }

            Result.success(result.user)
        } catch (e: Exception) {
            handleAuthError<FirebaseUser?>("Sign-up failed", e)
        }
    }

    // Save user data to Firestore
    private suspend fun saveUserToFirestore(
        uid: String,
        name: String,
        email: String,
        profileImageUrl: String,
        phoneNumber: String
    ) {
        val user = User(
            uid = uid,
            name = name,
            email = email,
            profileImageUrl = profileImageUrl,
            phoneNumber = phoneNumber,
            createdAt = com.google.firebase.Timestamp.now()
        )
        firestore.collection("users").document(uid).set(user).await()
    }

    // Sign out the user
    fun signOut() {
        auth.signOut()
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleAuthError<Unit>("Failed to send password reset email", e)
        }
    }

    // Reauthenticate using email and password
    suspend fun reauthenticateWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        val user = auth.currentUser
        return if (user != null) {
            val credential = EmailAuthProvider.getCredential(email, password)
            try {
                user.reauthenticate(credential).await()
                Result.success(user)
            } catch (e: Exception) {
                handleAuthError<FirebaseUser>("Re-authentication failed", e)
            }
        } else {
            Result.failure(Exception("No user is currently signed in"))
        }
    }

    // Handle common authentication errors
    private fun <T> handleAuthError(message: String, exception: Exception): Result<T> {
        Log.e("AuthManager", "$message: ${exception.message}")
        return Result.failure(Exception("$message: ${exception.message}"))
    }
}