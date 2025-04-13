package com.example.cleansync.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cleansync.MainActivity
import com.example.cleansync.R
import com.example.cleansync.data.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

object NotificationUtils {
    private const val CHANNEL_ID = "cleansync_notifications"

    // Function to send a custom notification to
    fun sendCustomNotification(
        context: Context,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "CleanSync Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(UUID.randomUUID().hashCode(), notificationBuilder.build())

        // Save the notification to Firestore
        saveNotificationToFirestore(message)
    }

    private fun saveNotificationToFirestore(message: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("NotificationUtils", "User not authenticated")
            return
        }

        val notification = Notification(
            id = UUID.randomUUID().toString(),
            userId = currentUserId, // Set the actual user ID
            message = message,
            read = false,
            timestamp = Timestamp.now()
        )

        // Rest of the function remains the same
        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d("NotificationUtils", "Notification saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationUtils", "Error saving notification to Firestore", e)
            }
    }
}