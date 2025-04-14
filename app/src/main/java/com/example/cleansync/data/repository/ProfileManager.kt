package com.example.cleansync.data.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

class ProfileManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser


    // signout
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


    suspend fun updateUserPhotoUrlInFirestore(photoUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid)
            .update("photoUrl", photoUrl)
            .await()
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

    suspend fun uploadProfilePhoto(uri: Uri): Uri {
        val user = auth.currentUser ?: throw Exception("No authenticated user")
        val storageRef = FirebaseStorage.getInstance().reference
        val photoRef = storageRef.child("profile_images/${user.uid}.jpg")

        photoRef.putFile(uri).await()
        return photoRef.downloadUrl.await()
    }




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

    suspend fun updateProfilePicture(photoUri: Uri) {
        val user = auth.currentUser ?: throw Exception("No authenticated user")

        val photoDownloadUrl = uploadProfilePhoto(photoUri)

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(photoDownloadUrl)
            .build()

        user.updateProfile(profileUpdates).await()
        refreshUser()

        firestore.collection("users").document(user.uid)
            .update("profileImageUrl", photoDownloadUrl.toString())
            .await()

        Log.d("ProfileManager", "Profile picture updated.")
    }




}