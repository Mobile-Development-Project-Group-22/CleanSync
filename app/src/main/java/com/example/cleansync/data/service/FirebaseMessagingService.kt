import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cleansync.MainActivity
import com.example.cleansync.R
import com.example.cleansync.data.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val db = FirebaseFirestore.getInstance()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: "You have a new message"

        // Log the message for debugging purposes
        Log.d("MyFirebaseService", "Message received: Message=$message")

        // Save the notificatxion to Firestore
        saveNotificationToFirestore(message,
            message,

        )

        // Broadcast the notification data to update the ViewModel (if required in your architecture)
        val intent = Intent("com.example.cleansync.NOTIFICATION").apply {

            putExtra("message", message)
        }
        sendBroadcast(intent)

        // Send the notification directly to the system tray
        sendNotification(message,
            message)
    }

    // Implement onNewToken to observe token changes
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Log the new token for debugging purposes
        Log.d("MyFirebaseService", "New FCM Token: $token")

        // Optionally, you can send this token to your server to associate it with the user/device
        sendTokenToServer(token)
    }

    // Function to send the new token to your server (if required)
    private fun sendTokenToServer(token: String) {
        // Implement your logic to save the token to Firestore or a server
        Log.d("MyFirebaseService", "Token sent to the server: $token")
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "cleansync_notifications"
        val notificationId = UUID.randomUUID().toString()

        // Create the Intent to open MainActivity when tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Create PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set custom vibration pattern
        var vibrationPattern = longArrayOf(0, 200, 100, 200)

        // Build the notification with custom sound and vibration
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(vibrationPattern)
            .setSound(Uri.parse("android.resource://$packageName/raw/custom_sound"))  // Set a custom sound

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android O+, create NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "CleanSync Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for CleanSync"
                enableVibration(true)
                vibrationPattern = vibrationPattern
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Issue the notification
        notificationManager.notify(notificationId.hashCode(), notificationBuilder.build())
    }

    private fun saveNotificationToFirestore(title: String, message: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.w("MyFirebaseService", "User not authenticated, skipping notification save")
            return
        }

        val notification = Notification(
            userId = userId,
            message = message,
            read = false,
            timestamp = Timestamp.now()
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