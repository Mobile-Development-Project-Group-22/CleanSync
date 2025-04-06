package com.example.cleansync.ui.profile

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
import androidx.compose.ui.unit.dp

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

            // Button on the right side of the row
            Button(
                onClick = onButtonClicked, // Handle button click
                modifier = Modifier.padding(start = 8.dp) // Optional padding
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