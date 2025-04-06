package com.example.cleansync.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun NotificationPreferences(
    emailNotificationsEnabled: Boolean,
    pushNotificationsEnabled: Boolean,
    onEmailNotificationsChanged: (Boolean) -> Unit,
    onPushNotificationsChanged: (Boolean) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Email Notifications", modifier = Modifier.weight(1f))
            Switch(
                checked = emailNotificationsEnabled,
                onCheckedChange = onEmailNotificationsChanged
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Push Notifications", modifier = Modifier.weight(1f))
            Switch(
                checked = pushNotificationsEnabled,
                onCheckedChange = onPushNotificationsChanged
            )
        }
    }
}