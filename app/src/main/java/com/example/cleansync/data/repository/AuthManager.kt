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

    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { Result.success(it) } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Log.e("AuthManager", "Google sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Log.e("AuthManager", "Sign-in failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser?> {
        return try {
            // 1. Create user account
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            // 2. Update user profile with name
            result.user?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                // 3. Send verification email
                user.sendEmailVerification().await()

                // 4. Save additional user data to Firestore
                saveUserToFirestore(user.uid, name, email)
            }

            Result.success(result.user)
        } catch (e: Exception) {
            Log.e("AuthManager", "Sign-up failed: ${e.message}")
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

    fun sendPasswordResetEmail(email: String) {

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthManager", "Password reset email sent.")
                } else {
                    Log.e(
                        "AuthManager",
                        "Failed to send password reset email: ${task.exception?.message}"
                    )
                }
            }
    }

    suspend fun reauthenticateWithEmailPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        val user = auth.currentUser
        return if (user != null) {
            val credential = EmailAuthProvider.getCredential(email, password)
            try {
                // Reauthenticate the user with their credentials
                user.reauthenticate(credential).await()
                Result.success(user)
            } catch (e: Exception) {
                Log.e("AuthManager", "Re-authentication failed: ${e.message}")
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("No user is currently signed in"))
        }
    }
}
