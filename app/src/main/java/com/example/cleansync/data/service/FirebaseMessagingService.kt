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
import com.example.cleansync.data.model.NotificationState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val db = FirebaseFirestore.getInstance()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "New Notification"
        val message = remoteMessage.notification?.body ?: "You have a new message"

        // Log the message
        Log.d("MyFirebaseService", "Message received: Title=$title, Message=$message")

        // Save the notification to Firestore
        saveNotificationToFirestore(title, message)

        // Broadcast the notification data to update the ViewModel
        val intent = Intent("com.example.cleansync.NOTIFICATION").apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        sendBroadcast(intent)

        // Send the notification directly to the system tray
        sendNotification(title, message)
    }

    // Implement onNewToken to observe token changes
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Log the new token for debugging purposes
        Log.d("MyFirebaseService", "New FCM Token: $token")

        // Optionally, you can send this token to your server to associate it with the user/device
        // For example, save it to Firestore or your backend for targeted messaging
        sendTokenToServer(token)
    }

    // Function to send the new token to your server (if required)
    private fun sendTokenToServer(token: String) {
        // Implement your logic to save the token to Firestore or a server
        Log.d("MyFirebaseService", "Token sent to the server: $token")
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "cleansync_notifications"

        // Generate a unique notification ID using UUID
        val notificationId = UUID.randomUUID().toString().hashCode()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "CleanSync Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun saveNotificationToFirestore(title: String, message: String) {
        val notification = NotificationState(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = Timestamp.now(),
            isRead = false
        )

        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d("MyFirebaseService", "Notification saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("MyFirebaseService", "Error saving notification to Firestore", e)
            }
    }
}