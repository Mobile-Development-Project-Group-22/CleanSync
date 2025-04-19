package com.example.cleansync.ui.notifications

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

    fun toggleReadStatus(notification: Notification) {
        updateNotificationField(notification, "read", !notification.read)
    }

    fun markNotificationAsRead(notification: Notification) {
        updateNotificationField(notification, "read", true)
    }

    fun removeNotification(notification: Notification) {
        updateNotificationIfAuthorized(notification) {
            notificationsRef.document(notification.id).delete()
        }
    }

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

    fun unreadNotificationsCount(): Int = _notificationState.value.count { !it.read }

    fun refreshNotifications() = fetchNotifications()

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun updateNotificationField(notification: Notification, field: String, value: Any) {
        updateNotificationIfAuthorized(notification) {
            notificationsRef.document(notification.id).update(field, value)
        }
    }

    private fun updateNotificationIfAuthorized(notification: Notification, operation: () -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null && notification.userId == uid) {
            operation()
        } else {
            _errorMessage.value = "Not authorized"
        }
    }
}
