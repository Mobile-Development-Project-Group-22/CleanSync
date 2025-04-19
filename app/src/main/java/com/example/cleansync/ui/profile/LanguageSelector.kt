package com.example.cleansync.ui.profile

import android.R.id.message
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cleansync.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onButtonClicked: () -> Unit // Added a lambda for the button click event
) {
    val languageOptions = listOf("English", "Spanish", "French")
    val languageMap = mapOf("English" to "en", "Spanish" to "es", "French" to "fr")
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Column {
        // Row to place dropdown text on the left and button on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween, // Space out elements
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) } // Toggle dropdown visibility
                .padding(8.dp)
        ) {
            // Text showing selected language
            Text(
                text = languageOptions.find { languageMap[it] == selectedLanguage }
                    ?: "Select Language",
                modifier = Modifier.weight(1f) // Take available space
            )

            Log.d("LanguageSelector", "Selected language: $selectedLanguage")

            Button(
                onClick = {
                    val newLanguage = languageOptions.find { languageMap[it] == selectedLanguage } ?: "English"

                    // Log before sending notification
                    Log.d("LanguageSelector", "Sending notification for language change: $newLanguage")

                    NotificationUtils.sendCustomNotification(
                        context,
                        "Language Changed",
                        "You have successfully changed the language to $newLanguage"
                    )

                    NotificationUtils.saveNotificationToFirestore(userId, "You have successfully changed the language to English")
                    onButtonClicked() // Call the button click event
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = "Change")
            }

        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            languageOptions.forEach { language ->
                DropdownMenuItem(
                    onClick = {
                        // Update selected language and close the dropdown
                        onLanguageSelected(languageMap[language] ?: "en")
                        onExpandedChange(false) // Close dropdown after selection
                    },
                    text = { Text(language) },
                    trailingIcon = {
                        if (selectedLanguage == languageMap[language]) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}
