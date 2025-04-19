
package com.example.cleansync.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cleansync.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth

class LocalNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "You have a scheduled notification"

        NotificationUtils.sendCustomNotification(context, title, message, )
        NotificationUtils.saveNotificationToFirestore(FirebaseAuth.getInstance().currentUser?.uid, message)
    }
}