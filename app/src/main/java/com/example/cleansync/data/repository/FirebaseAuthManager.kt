package com.example.cleansync.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Register user with suspend function
    suspend fun registerUser(name: String, email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("User creation failed"))

            val userMap = hashMapOf(
                "name" to name,
                "email" to email,
                "profilePicture" to ""
            )

            firestore.collection("users").document(user.uid).set(userMap).await()
            Result.success(user)
        } catch (e: Exception) {
            // Log the error message for debugging purposes
            Log.e("FirebaseAuthManager", "Error registering user: ${e.message}", e)

            // Handle specific Firebase errors (Optional: add more specific error handling)
            when (e) {
                is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                    return Result.failure(Exception("User already exists"))
                }
                is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                    return Result.failure(Exception("Weak password"))
                }
                else -> return Result.failure(e)
            }
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(
                email,
                password
            ).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun logout() {
        auth.signOut()
    }
}
