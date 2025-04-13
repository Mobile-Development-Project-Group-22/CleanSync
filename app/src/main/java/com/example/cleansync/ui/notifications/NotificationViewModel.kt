package com.example.cleansync.ui.notifications

import android.R.id
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.cleansync.data.model.Notification
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val notificationsRef = db.collection("notifications")

    // Use a StateFlow to hold the list of notifications
    private val _notificationState = MutableStateFlow<List<Notification>>(emptyList())
    val notificationState: StateFlow<List<Notification>> get() = _notificationState


    // Add an error state for UI updates
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    init {
        fetchNotificationsFromFirestore()
        subscribeToNotifications()
    }

    // Subscribe to notifications using Firebase Cloud Messaging
    private fun subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("notifications")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("NotificationViewModel", "Subscribed to notifications topic")
                } else {
                    Log.e("NotificationViewModel", "Failed to subscribe to notifications topic")
                }
            }
    }

    private fun fetchNotificationsFromFirestore() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            _errorMessage.value = "User not authenticated"
            Log.e("NotificationViewModel", "User not authenticated")
            return
        }

        Log.d("NotificationViewModel", "Fetching notifications for user: $currentUserId")

        notificationsRef
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Error fetching notifications"
                    Log.e("NotificationViewModel", "Fetch error: ${error.message}")
                    return@addSnapshotListener
                }

                Log.d("NotificationViewModel", "Received snapshot with ${snapshot?.size()} documents")

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Notification::class.java)?.copy(id = doc.id).also {
                            Log.d("NotificationViewModel", "Parsed notification: ${it?.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("NotificationViewModel", "Parse error for doc ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d("NotificationViewModel", "Fetched ${notifications.size} notifications")
                _notificationState.value = notifications
            }
    }

    fun addNotification(notification: Notification) {
        notificationsRef.add(notification)
            .addOnSuccessListener { documentReference ->
                Log.d("NotificationViewModel", "Notification added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error adding notification"
                Log.e("NotificationViewModel", "Error adding notification", e)
            }
    }
    // Mark notification as read
    fun markNotificationAsRead(notification: Notification) {
        notificationsRef.document(notification.id)
            .update("read", true) // Ensure 'read' is used here instead of 'isRead'
            .addOnSuccessListener {
                Log.d("NotificationViewModel", "Notification marked as read successfully.")
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error marking notification as read"
                Log.e("NotificationViewModel", "Error marking notification as read", e)
            }
    }

    // Remove a notification
    fun removeNotification(notification: Notification) {
        notificationsRef.document(notification.id)
            .delete()
            .addOnSuccessListener {
                // Handle UI updates
                Log.d("NotificationViewModel", "Notification removed successfully.")
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error removing notification"
                Log.e("NotificationViewModel", "Error removing notification", e)
            }
    }


    // Clear all notifications for the current user
    fun clearAllNotifications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        notificationsRef.whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    notificationsRef.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("NotificationViewModel", "Notification cleared successfully.")
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = "Error clearing notification"
                            Log.e("NotificationViewModel", "Error clearing notification", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error fetching notifications to clear"
                Log.e("NotificationViewModel", "Error fetching notifications to clear", e)
            }
    }

    // Toggle read status of a notification
    fun toggleReadStatus(notification: Notification) {
        val newStatus = !notification.read
        notificationsRef.document(notification.id)
            .update("read", newStatus) // Use 'read' instead of 'isRead'
            .addOnSuccessListener {
                Log.d("NotificationViewModel", "Notification read status updated successfully.")
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error updating notification read status"
                Log.e("NotificationViewModel", "Error updating notification read status", e)
            }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun refreshNotifications() {
        fetchNotificationsFromFirestore()
    }
    // Count unread notifications
    fun unreadNotificationsCount(): Int {
        return _notificationState.value.count { !it.read }  // Use 'read' here instead of 'isRead'
    }
}