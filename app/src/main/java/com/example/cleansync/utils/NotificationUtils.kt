package com.example.cleansync.utils

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
        appointmentTimeMillis: Long? = null, // Optional, for scheduled notifications
        read: Boolean = false,
        scheduleTimeMillis: Long? = null,
        email: String? = null,
        isForgotPassword: Boolean = false
    ) {
        // Save to Firestore only for persistent notifications (not for temporary ones like "forgot password")
        if (!isForgotPassword) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (!userId.isNullOrEmpty()) {
                saveNotificationToFirestore(userId, message)
            } else {
                Log.w(TAG, "User not authenticated; skipping Firestore save for notification.")
            }
        }

        val notificationTimeMillis = appointmentTimeMillis?.let { it - 3600000L }

        if (scheduleTimeMillis != null) {
            Log.d(TAG, "Scheduling notification for future: $scheduleTimeMillis")
            scheduleLocalNotification(context, title, message, scheduleTimeMillis)
        } else {
            Log.d(TAG, "Sending notification immediately")
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

        // Re-use a consistent request code for the notification to avoid unnecessary new instances
        val pendingIntent = PendingIntent.getActivity(
            context, title.hashCode(), intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setSound(Uri.parse("android.resource://${context.packageName}/raw/notification_alert_269289"))

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannelIfNeeded(manager)

        manager.notify(title.hashCode(), builder.build())  // Use title.hashCode() to create a consistent notification ID
        Log.d(TAG, "Notification sent.")
    }

    /**
     * Schedule a notification for a specific time.
     */
    fun scheduleLocalNotification(context: Context, title: String, message: String, timeInMillis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                Log.w(TAG, "Cannot schedule exact alarms. Prompting user for permission.")
                return
            }
        }

        val intent = Intent(context, LocalNotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("userId", FirebaseAuth.getInstance().currentUser?.uid)
        }

        val requestCode = title.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
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
            val existingChannel = manager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for CleanSync notifications"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created.")
            } else {
                Log.d(TAG, "Notification channel already exists, no need to recreate.")
            }
        }
    }


}