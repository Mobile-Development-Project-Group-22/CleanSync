package com.example.cleansync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

data class ChatMessage(val content: String, val isBot: Boolean)

@Composable
fun ChatbotDialog(onDismiss: () -> Unit) {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("Bot: Hi there! How can I help you today?", true)
            )
        )
    }
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("CleanSync Assistant") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(min = 100.dp, max = 300.dp)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp), // Add padding for the input area
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        if (message.isBot) {
                            BotMessageBubble(message.content)
                        } else {
                            UserMessageBubble(message.content)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Options for the user to click
                if (messages.size == 1) {
                    OptionButton("What is CleanSync?") {
                        messages = messages + ChatMessage("CleanSync is a service that...", true)
                    }
                    OptionButton("How does the booking work?") {
                        messages = messages + ChatMessage("Booking works by selecting a date and time...", true)
                    }
                    OptionButton("See my bookings") {
                        messages = messages + ChatMessage("Here are your upcoming bookings...", true)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Text Field for the user
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask something...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMessage = "You: $inputText"
                            val botResponse = "Bot: ${getBotResponse(inputText)}"
                            messages = messages + ChatMessage(userMessage, false) + ChatMessage(botResponse, true)
                            inputText = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Send")
                }
            }
        }
    )
}

@Composable
fun BotMessageBubble(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentWidth(Alignment.Start) // Ensures message takes only as much width as needed
    ) {
        // Left-aligned bubble for the bot
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
        ) {
            Text(message, color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
fun UserMessageBubble(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.End // This aligns the user message to the right
    ) {
        // Right-aligned bubble for the user
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
                .wrapContentWidth(Alignment.End) // Ensures message takes only as much width as needed
        ) {
            Text(message, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
fun OptionButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

fun getBotResponse(input: String): String {
    return when {
        input.contains("book", ignoreCase = true) -> "You can book the cleaning by clicking on empty dates in calendar or 'Book Now' button on the dashboard!"
        input.contains("hello", ignoreCase = true) -> "Hello! 👋 How can I assist you today?"
        input.contains("points", ignoreCase = true) -> "You can check your loyalty points in your profile."
        input.contains("clean", ignoreCase = true) -> "We offer carpet and sofa cleaning services. Would you like to book one?"
        else -> "I'm still learning! Try asking about bookings or points."
    }
}
