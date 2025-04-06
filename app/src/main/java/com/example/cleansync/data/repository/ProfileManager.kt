package com.example.cleansync.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class ProfileManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser = auth.currentUser

    suspend fun updateUserProfile(displayName: String, photoUri: Uri?) {
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUri(photoUri)
            .build()
        try {
            user?.updateProfile(profileUpdates)?.await()
            refreshUser()
            Log.d("ProfileManager", "User profile updated and refreshed.")
        } catch (e: Exception) {
            Log.e("ProfileManager", "Error updating profile: ${e.message}")
        }
    }

    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser
        try {
            user?.updateEmail(newEmail)?.await()
            refreshUser()
            Log.d("ProfileManager", "User email updated and refreshed.")
        } catch (e: Exception) {
            Log.e("ProfileManager", "Error updating email: ${e.message}")
        }
    }

    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser
        try {
            user?.updatePassword(newPassword)?.await()
            Log.d("ProfileManager", "User password updated.")
        } catch (e: Exception) {
            Log.e("ProfileManager", "Error updating password: ${e.message}")
        }
    }

    suspend fun refreshUser() {
        try {
            auth.currentUser?.reload()?.await()
            Log.d("ProfileManager", "User refreshed successfully.")
        } catch (e: Exception) {
            Log.e("ProfileManager", "Error refreshing user: ${e.message}")
        }
    }


}
