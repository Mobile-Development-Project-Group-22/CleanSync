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
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

object NotificationUtils {

    private const val CHANNEL_ID = "cleansync_notifications"
    private const val CHANNEL_NAME = "CleanSync Notifications"

    fun sendCustomNotification(context: Context, title: String, message: String) {
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
    }

    fun scheduleLocalNotification(context: Context, title: String, message: String, timeInMillis: Long?) {
        if (timeInMillis == null) {
            Log.e("NotificationUtils", "Time in millis is null, cannot schedule notification.")
            return
        }

        val intent = Intent(context, LocalNotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            UUID.randomUUID().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
    fun saveNotificationToFirestore(userId: String?, message: String) {
        if (userId.isNullOrEmpty()) {
            Log.w("NotificationUtils", "User not authenticated")
            return
        }

        val notification = Notification(
            userId = userId,
            message = message,
            read = false,
            timestamp = Timestamp.now()
        )

        FirebaseFirestore.getInstance().collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d("NotificationUtils", "Notification saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationUtils", "Error saving to Firestore", e)
            }
    }

    private fun createChannelIfNeeded(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for CleanSync notifications"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}