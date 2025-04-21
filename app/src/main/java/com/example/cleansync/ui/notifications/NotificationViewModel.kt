package com.example.cleansync.ui.notifications

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.cleansync.data.model.Notification
import com.example.cleansync.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val notificationsRef = db.collection("notifications")
    private val auth = FirebaseAuth.getInstance()

    private val _notificationState = MutableStateFlow<List<Notification>>(emptyList())
    val notificationState: StateFlow<List<Notification>> get() = _notificationState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                Log.d("NotificationVM", "Auth restored, fetching notifications")
                subscribeToNotifications()
                fetchNotifications()
            } else {
                Log.w("NotificationVM", "Auth not yet available")
            }
        }
    }

    /**
     * Subscribe to notifications using Firebase Cloud Messaging (FCM).
     */
    private fun subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("notifications")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("NotificationVM", "Subscribed to notifications topic")
                } else {
                    Log.e("NotificationVM", "Subscription failed", task.exception)
                }
            }
    }

    /**
     * Fetch notifications from Firestore for the authenticated user.
     */
    private fun fetchNotifications() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        notificationsRef
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (snapshot == null || snapshot.isEmpty) {
                    _notificationState.value = emptyList()
                    Log.d("NotificationVM", "No notifications found")
                    return@addSnapshotListener
                }

                val notifications = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Notification::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("NotificationVM", "Error parsing notification ${doc.id}", e)
                        null
                    }
                }

                _notificationState.value = notifications
            }
    }

    /**
     * Add a new notification to Firestore.
     */
    fun addNotification(notification: Notification) {
        auth.currentUser?.uid?.let { uid ->
            val withUser = notification.copy(userId = uid)
            notificationsRef.add(withUser)
                .addOnSuccessListener {
                    Log.d("NotificationVM", "Notification added: ${it.id}")
                }
                .addOnFailureListener {
                    _errorMessage.value = "Error adding notification"
                    Log.e("NotificationVM", "Add error", it)
                }
        } ?: run {
            _errorMessage.value = "User not authenticated"
        }
    }

    /**
     * Trigger a notification using the NotificationUtils, both for immediate and scheduled notifications.
     */
    fun triggerNotification(
        context: Context,
        title: String,
        message: String,
        appointmentTimeMillis: Long? = null,
        read: Boolean = false,
        scheduleTimeMillis: Long? = null,
        isForgotPassword: Boolean = false
    ) {
        NotificationUtils.triggerNotification(
            context,
            title,
            message,
            appointmentTimeMillis,
            read,
            scheduleTimeMillis,
            isForgotPassword
        )
    }

    /**
     * Toggle the read status of a notification.
     */
    fun toggleReadStatus(notification: Notification) {
        updateNotificationField(notification, "read", !notification.read)
    }

    /**
     * Mark a specific notification as read.
     */
    fun markNotificationAsRead(notification: Notification) {
        updateNotificationField(notification, "read", true)
    }

    /**
     * Remove a specific notification.
     */
    fun removeNotification(notification: Notification) {
        updateNotificationIfAuthorized(notification) {
            notificationsRef.document(notification.id).delete()
        }
    }

    /**
     * Clear all notifications for the current user.
     */
    fun clearAllNotifications() {
        val uid = auth.currentUser?.uid ?: return
        notificationsRef.whereEqualTo("userId", uid).get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    notificationsRef.document(doc.id).delete()
                }
            }
            .addOnFailureListener {
                _errorMessage.value = "Error clearing notifications"
                Log.e("NotificationVM", "Clear error", it)
            }
    }

    /**
     * Get the count of unread notifications.
     */
    fun unreadNotificationsCount(): Int = _notificationState.value.count { !it.read }

    /**
     * Refresh notifications by fetching them again from Firestore.
     */
    fun refreshNotifications() = fetchNotifications()

    /**
     * Clear any error messages.
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Update a specific field of a notification (e.g., read status).
     */
    private fun updateNotificationField(notification: Notification, field: String, value: Any) {
        updateNotificationIfAuthorized(notification) {
            notificationsRef.document(notification.id).update(field, value)
        }
    }

    /**
     * Ensure the user is authorized to modify the notification.
     */
    private fun updateNotificationIfAuthorized(notification: Notification, operation: () -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null && notification.userId == uid) {
            operation()
        } else {
            _errorMessage.value = "Not authorized"
        }
    }
}
