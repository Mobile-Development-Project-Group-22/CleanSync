package com.example.cleansync.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.cleansync.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth


class LocalNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "You have a scheduled notification"
        val read = intent.getBooleanExtra("read", false)

        Log.d("LocalNotificationReceiver", "Received scheduled notification: $title - $message")

        // ✅ Show the notification directly (don't trigger/schedule again)
        NotificationUtils.sendCustomNotification(context, title, message)

        // Optional: Save again to Firestore as read, if needed
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (!userId.isNullOrEmpty()) {
            NotificationUtils.saveNotificationToFirestore(userId, message)
        }
    }
}

