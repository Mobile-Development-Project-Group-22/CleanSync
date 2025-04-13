package com.example.cleansync.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.cleansync.data.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationReceiver : BroadcastReceiver() {

    private val db = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "No Title"
        val message = intent.getStringExtra("message") ?: "No Message"

        // Log the received notification data
        Log.d("NotificationReceiver", "Received Notification - Title: $title, Message: $message")

        // Save the notification directly to Firestore
        saveNotificationToFirestore(title, message)
    }

    // Function to save notification to Firestore
    private fun saveNotificationToFirestore(title: String, message: String) {
        val notification = Notification(
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            message = message,
            read = false,
            timestamp = Timestamp.now()
        )

        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d("NotificationReceiver", "Notification saved to Firestore successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationReceiver", "Error saving notification to Firestore", e)
            }
    }
}