package com.example.cleansync.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.cleansync.data.model.NotificationState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val notificationsRef = db.collection("notifications")

    private val _notificationState = MutableStateFlow<List<NotificationState>>(emptyList())
    val notificationState: StateFlow<List<NotificationState>> get() = _notificationState

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

    // Fetch notifications from Firestore with real-time updates using snapshot listener
    private fun fetchNotificationsFromFirestore() {
        notificationsRef
            .orderBy("timestamp")
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    // Update error message state
                    _errorMessage.value = "Error fetching notifications"
                    Log.e("NotificationViewModel", "Error fetching notifications", e)
                    return@addSnapshotListener
                }
                val notifications = documents?.map { doc ->
                    doc.toObject(NotificationState::class.java).apply {
                        id = doc.id
                    }
                } ?: emptyList()
                _notificationState.value = notifications
            }
    }

    // Add a new notification to Firestore
    fun addNotification(notification: NotificationState) {
        notificationsRef.whereEqualTo("title", notification.title)
            .whereEqualTo("message", notification.message)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    notificationsRef.add(notification)
                        .addOnSuccessListener {
                            Log.d("NotificationViewModel", "Notification added successfully.")
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = "Error adding notification"
                            Log.e("NotificationViewModel", "Error adding notification", e)
                        }
                } else {
                    _errorMessage.value = "Notification already exists"
                    Log.d("NotificationViewModel", "Notification already exists.")
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error checking for duplicate notification"
                Log.e("NotificationViewModel", "Error checking for duplicate notification", e)
            }
    }

    // Mark notification as read
    fun markNotificationAsRead(notification: NotificationState) {
        notificationsRef.document(notification.id)
            .update("isRead", true)
            .addOnSuccessListener {
                Log.d("NotificationViewModel", "Notification marked as read successfully.")
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error marking notification as read"
                Log.e("NotificationViewModel", "Error marking notification as read", e)
            }
    }

    // Remove a notification
    fun removeNotification(notification: NotificationState) {
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

    // Clear all notifications
    fun clearAllNotifications() {
        notificationsRef
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
    fun toggleReadStatus(notification: NotificationState) {
        val newStatus = !notification.isRead
        notificationsRef.document(notification.id)
            .update("isRead", newStatus)
            .addOnSuccessListener {
                Log.d("NotificationViewModel", "Notification read status updated successfully.")
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error updating notification read status"
                Log.e("NotificationViewModel", "Error updating notification read status", e)
            }
    }


}

