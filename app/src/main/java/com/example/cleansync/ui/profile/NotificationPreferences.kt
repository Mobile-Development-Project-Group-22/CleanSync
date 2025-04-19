package com.example.cleansync.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cleansync.ui.notifications.NotificationPreferences

@Composable
fun NotificationPreferencesScreen(
    preferences: NotificationPreferences,
    onEmailChange: (Boolean) -> Unit,
    onPushChange: (Boolean) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Email Notifications", modifier = Modifier.weight(1f))
            Switch(
                checked = preferences.email,
                onCheckedChange = onEmailChange
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Push Notifications", modifier = Modifier.weight(1f))
            Switch(
                checked = preferences.push,
                onCheckedChange = onPushChange
            )
        }
    }
}
