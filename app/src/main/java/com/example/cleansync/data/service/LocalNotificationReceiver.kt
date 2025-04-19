package com.example.cleansync.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.cleansync.utils.NotificationUtils

class LocalNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "You have a scheduled notification"
        val read = intent.getBooleanExtra("read", false)

        Log.d("LocalNotificationReceiver", "Received scheduled notification: $title - $message")

        NotificationUtils.triggerNotification(
            context = context,
            title = title,
            message = message,
            read = read
        )
    }
}
