package com.example.cleansync.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProfileManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    // Sign out method
    fun signOut() {
        auth.signOut()
        Log.d("ProfileManager", "User signed out.")
    }

    // Update email address
    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser
        try {
            user?.updateEmail(newEmail)?.await()
            refreshUser()  // Refresh user to make sure email is updated locally
            Log.d("ProfileManager", "User email updated and refreshed.")

            // Update Firestore user document
            user?.uid?.let { uid ->
                firestore.collection("users").document(uid).update(
                    mapOf("email" to newEmail)
                ).await()
                Log.d("ProfileManager", "Firestore user email updated.")
            }

        } catch (e: Exception) {
            Log.e("ProfileManager", "Error updating email: ${e.message}")
        }
    }

    // Update profile picture URL in Firestore
    suspend fun updateUserPhotoUrlInFirestore(profileImageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        // Check if the user document exists
        val userDoc = db.collection("users").document(user.uid).get().await()
        if (!userDoc.exists()) {
            // Create the document if it doesn't exist
            db.collection("users").document(user.uid).set(mapOf(
                "uid" to user.uid,
                "name" to user.displayName,
                "email" to user.email,
                "photoImageUrl" to profileImageUrl
            )).await()
        } else {
            // Update the document if it exists
            db.collection("users").document(user.uid).update("photoImageUrl", profileImageUrl).await()
        }
    }


    // Update password
    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser
        try {
            user?.updatePassword(newPassword)?.await()
            Log.d("ProfileManager", "User password updated.")
        } catch (e: Exception) {
            Log.e("ProfileManager", "Error updating password: ${e.message}")
        }
    }

    // Refresh the current user to get the latest data from Firebase
    suspend fun refreshUser() {
        try {
            auth.currentUser?.reload()?.await()
            Log.d("ProfileManager", "User refreshed successfully.")
        } catch (e: Exception) {
            Log.e("ProfileManager", "Error refreshing user: ${e.message}")
        }
    }

    // Upload profile photo to Firebase Storage
    suspend fun uploadProfilePhoto(uri: Uri): Uri {
        val user = auth.currentUser ?: throw Exception("No authenticated user")
        val storageRef = FirebaseStorage.getInstance().reference
        val photoRef = storageRef.child("profile_images/${user.uid}.jpg")

        photoRef.putFile(uri).await()
        return photoRef.downloadUrl.await() // Return the download URL
    }

    // Update display name
    suspend fun updateDisplayName(displayName: String) {
        val user = auth.currentUser ?: throw Exception("No authenticated user")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates).await()
        refreshUser()

        firestore.collection("users").document(user.uid)
            .update("name", displayName)
            .await()

        Log.d("ProfileManager", "Display name updated.")
    }

    // Update profile picture in Firebase and Firestore
    suspend fun updateProfilePicture(photoUri: Uri) {
        val user = auth.currentUser ?: throw Exception("No authenticated user")

        try {
            // Upload the new profile picture
            val photoDownloadUrl = uploadProfilePhoto(photoUri)

            // Update the profile with the new photo URL
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(photoDownloadUrl)
                .build()

            user.updateProfile(profileUpdates).await()
            refreshUser()

            // Update the photo URL in Firestore
            firestore.collection("users").document(user.uid)
                .update("profileImageUrl", photoDownloadUrl.toString())
                .await()

            Log.d("ProfileManager", "Profile picture updated successfully.")
        } catch (e: Exception) {
            Log.e("ProfileManager", "Error updating profile picture: ${e.message}")
        }
    }
}
