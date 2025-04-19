package com.example.cleansync.utils

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.cleansync.MainActivity
import com.example.cleansync.R
import com.example.cleansync.data.model.Notification
import com.example.cleansync.data.service.LocalNotificationReceiver
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*



object NotificationUtils {

    private const val CHANNEL_ID = "cleansync_notifications"
    private const val CHANNEL_NAME = "CleanSync Notifications"
    private const val TAG = "NotificationUtils"

    /**
     * Trigger a notification and optionally schedule it. Saves to Firestore if the user is authenticated.
     */
    fun triggerNotification(
        context: Context,
        title: String,
        message: String,
        read: Boolean = false,
        scheduleTimeMillis: Long? = null,
        email: String? = null,
        isForgotPassword: Boolean = false  // added flag for password reset
    ) {
        // If it's a forgot password notification, save it to Firestore without checking auth state
        if (isForgotPassword) {
            // Save to Firestore for tracking purposes without checking auth state
            saveNotificationToFirestore(null, message)  // No userId needed for this case
        } else {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Only save to Firestore if user is authenticated
            if (!userId.isNullOrEmpty()) {
                saveNotificationToFirestore(userId, message)
            } else {
                Log.w(TAG, "User not authenticated; skipping Firestore save for regular notification.")
            }
        }

        if (scheduleTimeMillis != null) {
            scheduleLocalNotification(context, title, message, scheduleTimeMillis)
        } else {
            sendCustomNotification(context, title, message)
        }
    }

    /**
     * Send a custom notification immediately.
     */
    fun sendCustomNotification(context: Context, title: String, message: String) {
        Log.d(TAG, "Sending notification: $title - $message")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setSound(Uri.parse("android.resource://${context.packageName}/raw/custom_sound"))

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannelIfNeeded(manager)

        manager.notify(UUID.randomUUID().hashCode(), builder.build())
        Log.d(TAG, "Notification sent.")
    }

    /**
     * Schedule a notification for a specific time.
     */
    fun scheduleLocalNotification(context: Context, title: String, message: String, timeInMillis: Long) {
        Log.d(TAG, "Scheduling notification: $title at $timeInMillis")

        val intent = Intent(context, LocalNotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("userId", FirebaseAuth.getInstance().currentUser?.uid)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            UUID.randomUUID().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        Log.d(TAG, "Notification scheduled.")
    }

    /**
     * Save the notification to Firestore for a user.
     */
    fun saveNotificationToFirestore(userId: String?, message: String) {
        // If userId is null, it might be a password reset, so we need to check
        val email = FirebaseAuth.getInstance().currentUser?.email

        // If both userId and email are null, don't save the notification
        if (userId.isNullOrEmpty() && email.isNullOrEmpty()) {
            Log.w(TAG, "Both user ID and email are null or empty. Notification not saved.")
            return
        }

        val notificationUserId = userId ?: email // Use email as a fallback if user is not authenticated

        Log.d(TAG, "Saving notification to Firestore for user: $notificationUserId")

        val notification = Notification(
            userId = notificationUserId ?: "",
            message = message,
            read = false,
            timestamp = Timestamp.now()
        )

        FirebaseFirestore.getInstance().collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d(TAG, "Notification successfully saved to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving notification to Firestore", e)
            }
    }

    /**
     * Create the notification channel (Android O+).
     */
    private fun createChannelIfNeeded(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for CleanSync notifications"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created.")
        }
    }
}