package com.example.cleansync.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProfileManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser


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

    // Update password
    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser
        try {
            user?.updatePassword(newPassword)?.await()
            Log.d("ProfileManager", "User password updated.")
            // No Firestore update needed for password
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

    suspend fun uploadProfilePhoto(photoUri: Uri): Uri? {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val photoRef = storageReference.child("profile_photos/${UUID.randomUUID()}.jpg")

        return try {
            // Upload photo to Firebase Storage
            val uploadTask = photoRef.putFile(photoUri).await()

            // Get the download URL after the upload is complete
            val downloadUrl = photoRef.downloadUrl.await()
            downloadUrl
        } catch (e: Exception) {
            Log.e("ProfileManager", "Error uploading photo: ${e.message}")
            null
        }
    }


    suspend fun updateUserProfile(displayName: String, photoUri: Uri?) {
        val user = auth.currentUser
        val photoDownloadUrl = photoUri?.let { uploadProfilePhoto(it) }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUri(photoDownloadUrl)
            .build()

        try {
            // Update Firebase Authentication profile
            user?.updateProfile(profileUpdates)?.await()
            refreshUser()  // Refresh user to make sure profile updates are reflected locally
            Log.d("ProfileManager", "User profile updated and refreshed.")

            // Update Firestore user document
            user?.uid?.let { uid ->
                val updates = mutableMapOf<String, Any>(
                    "name" to displayName
                )

                // Update profile image URL if photoUri is not null
                photoDownloadUrl?.let {
                    updates["profileImageUrl"] = it.toString()
                }

                firestore.collection("users").document(uid).update(updates as Map<String, Any>).await()
                Log.d("ProfileManager", "Firestore user document updated.")
            }

        } catch (e: Exception) {
            Log.e("ProfileManager", "Error updating profile: ${e.message}")
        }
    }

}