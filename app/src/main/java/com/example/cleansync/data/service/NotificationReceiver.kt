package com.example.cleansync.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cleansync.data.model.NotificationState
import com.example.cleansync.ui.notifications.NotificationViewModel

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "No Title"
        val message = intent.getStringExtra("message") ?: "No Message"

        // You can now update your ViewModel with the notification data
        val notificationViewModel = NotificationViewModel()
        notificationViewModel.addNotification(
            NotificationState(
                title = title,
                message = message,
                isRead = false
            )
        )
    }
}