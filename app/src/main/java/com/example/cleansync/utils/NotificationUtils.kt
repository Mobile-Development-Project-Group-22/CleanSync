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

    private const val TAG = "NotificationUtils" // Log tag for easier identification in Logcat

    /**
     * Trigger a notification, either immediately or scheduled.
     *
     * @param context The context from which the notification is triggered.
     * @param title The title of the notification.
     * @param message The message of the notification.
     * @param scheduleTimeMillis Optional. If provided, the notification will be scheduled for this time.
     */
    fun triggerNotification(
        context: Context,
        title: String,
        message: String,
        read: Boolean = false,
        scheduleTimeMillis: Long? = null
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Save to Firestore
        saveNotificationToFirestore(userId, message)

        // Send or Schedule
        if (scheduleTimeMillis != null) {
            scheduleLocalNotification(context, title, message, scheduleTimeMillis)
        } else {
            sendCustomNotification(context, title, message)
        }
    }

    /**
     * Send a custom push notification.
     */
    fun sendCustomNotification(context: Context, title: String, message: String) {
        Log.d(TAG, "Sending custom notification with title: $title, message: $message")

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

        // Issue the notification with a unique ID
        val notificationId = UUID.randomUUID().hashCode()
        manager.notify(notificationId, builder.build())

        Log.d(TAG, "Notification sent with ID: $notificationId")
    }

    /**
     * Schedule a local notification at a specific time.
     */
    fun scheduleLocalNotification(context: Context, title: String, message: String, timeInMillis: Long?) {
        if (timeInMillis == null) {
            Log.e(TAG, "Time in millis is null, cannot schedule notification.")
            return
        }

        Log.d(TAG, "Scheduling local notification with title: $title, message: $message, at time: $timeInMillis")

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

        Log.d(TAG, "Notification scheduled at time: $timeInMillis")
    }

    /**
     * Save the notification to Firestore.
     */
    fun saveNotificationToFirestore(userId: String?, message: String) {
        if (userId.isNullOrEmpty()) {
            Log.w(TAG, "User ID is null or empty. Notification not saved.")
            return
        }

        Log.d(TAG, "Saving notification to Firestore for user: $userId")

        val notification = Notification(
            userId = userId,
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
     * Create a notification channel if it doesn't exist (required for Android O and above).
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
            Log.d(TAG, "Notification channel created or already exists.")
        }
    }
}
