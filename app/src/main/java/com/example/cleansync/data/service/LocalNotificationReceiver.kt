package com.example.cleansync.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.cleansync.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LocalNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "You have a scheduled notification"
        val read = intent.getBooleanExtra("read", false)

        Log.d("LocalNotificationReceiver", "Received scheduled notification: $title - $message")

        // ✅ Show the notification directly (don't trigger/schedule again)
        NotificationUtils.sendCustomNotification(context, title, message)

        // Optional: Save the notification to Firestore and mark it as read
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (!userId.isNullOrEmpty()) {
            saveNotificationToFirestore(userId, title, message, read)
        }
    }

    /**
     * Save the notification to Firestore, marking it as read or unread.
     */
    private fun saveNotificationToFirestore(userId: String, title: String, message: String, read: Boolean) {
        val notification = hashMapOf(
            "userId" to userId,
            "title" to title,
            "message" to message,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "read" to read
        )

        FirebaseFirestore.getInstance().collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d("LocalNotificationReceiver", "Notification successfully saved to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e("LocalNotificationReceiver", "Error saving notification to Firestore", e)
            }
    }
}
