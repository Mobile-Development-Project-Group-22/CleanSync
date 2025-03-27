package com.example.cleansync.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Get the current user
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Register user with suspend function
    suspend fun registerUser(name: String, email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("User creation failed"))

            // Save user info to Firestore
            saveUserToFirestore(user.uid, name, email)

            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error registering user: ${e.message}", e)

            // Handle specific Firebase errors
            when (e) {
                is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                    return Result.failure(Exception("User already exists"))
                }
                is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                    return Result.failure(Exception("Password is too weak"))
                }
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                    return Result.failure(Exception("Invalid email format"))
                }
                else -> return Result.failure(Exception("Unknown registration error: ${e.message}"))
            }
        }
    }

    // Helper function to save user data to Firestore
    private suspend fun saveUserToFirestore(userId: String, name: String, email: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "profilePicture" to "" // Empty profile picture initially
        )
        firestore.collection("users").document(userId).set(userMap).await()
    }

    // Login user and fetch their data
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                val name = getUserNameFromFirestore(user.uid)
                if (name != null && user.displayName != name) {
                    updateUserProfile(user, name)
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error logging in: ${e.message}", e)

            // Handle login-specific errors
            when (e) {
                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                    return Result.failure(Exception("Incorrect email or password"))
                }
                else -> return Result.failure(Exception("Login failed: ${e.message}"))
            }
        }
    }

    // Helper function to get user's name from Firestore
    private suspend fun getUserNameFromFirestore(userId: String): String? {
        val document = firestore.collection("users").document(userId).get().await()
        return document.getString("name") // Fetch the name from Firestore
    }

    // Update user's profile with fetched name
    private fun updateUserProfile(user: FirebaseUser, name: String) {
        user.updateProfile(
            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
        ).addOnFailureListener { e ->
            Log.e("FirebaseAuthManager", "Error updating user profile: ${e.message}", e)
        }
    }


    // Logout user
    fun logoutUser() {
        auth.signOut()
        Log.d("FirebaseAuthManager", "User logged out successfully")
    }
    // CRUD: Create User Data
    suspend fun createUserData(userId: String, name: String, email: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "profilePicture" to "" // Empty profile picture initially
        )

        try {
            firestore.collection("users").document(userId).set(userMap).await()
            Log.d("FirebaseAuthManager", "User data created successfully")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error creating user data: ${e.message}", e)
        }
    }

    // CRUD: Read User Data
    suspend fun getUserData(userId: String): Result<Map<String, Any>> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                Result.success(document.data ?: emptyMap())
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error retrieving user data: ${e.message}", e)
            Result.failure(Exception("Error retrieving user data"))
        }
    }

    // CRUD: Update User Data
    suspend fun updateUserData(userId: String, name: String, email: String, profilePicture: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "profilePicture" to profilePicture
        )
        try {
            firestore.collection("users").document(userId).update(userMap as Map<String, Any>).await()
            Log.d("FirebaseAuthManager", "User data updated successfully")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error updating user data: ${e.message}", e)
        }
    }

    // CRUD: Delete User Data
    suspend fun deleteUserData(userId: String) {
        try {
            firestore.collection("users").document(userId).delete().await()
            Log.d("FirebaseAuthManager", "User data deleted successfully")
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error deleting user data: ${e.message}", e)
        }
    }

    fun sendPasswordResetEmail(email: String): com.google.android.gms.tasks.Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }
    fun updateUserPassword(newPassword: String): com.google.android.gms.tasks.Task<Void> {
        val user = auth.currentUser
        return user?.updatePassword(newPassword) ?: throw Exception("No user is currently signed in.")
    }

}
