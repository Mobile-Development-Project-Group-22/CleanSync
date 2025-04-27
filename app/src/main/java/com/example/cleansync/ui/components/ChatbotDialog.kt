package com.example.cleansync.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

data class ChatMessage(val content: String, val isBot: Boolean)

@Composable
fun ChatbotDialog(onDismiss: () -> Unit) {
    var messages by remember { mutableStateOf(listOf(ChatMessage("Hi there! 👋 How can I help you today?", true))) }
    var inputText by remember { mutableStateOf("") }
    var isBotTyping by remember { mutableStateOf(false) }
    var pendingBotReply by remember { mutableStateOf<String?>(null) }

    pendingBotReply?.let { reply ->
        LaunchedEffect(reply) {
            isBotTyping = true
            delay(1500)
            messages = messages + ChatMessage(reply, true)
            isBotTyping = false
            pendingBotReply = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("CleanSync Assistant 🤖", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(min = 100.dp, max = 400.dp)
                        .fillMaxWidth()
                        .weight(1f, false),
                    reverseLayout = true
                ) {
                    itemsIndexed(messages.reversed()) { _, message ->
                        AnimatedVisibility(
                            visible = true,
                            enter = expandVertically() + fadeIn()
                        ) {
                            if (message.isBot) {
                                BotMessageBubble(message.content)
                            } else {
                                UserMessageBubble(message.content)
                            }
                        }
                    }
                    if (isBotTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (messages.size == 1) {
                    OptionButton("What is CleanSync?") {
                        pendingBotReply = "CleanSync is a platform offering professional cleaning services. 🧹✨"
                    }
                    OptionButton("How does booking work?") {
                        pendingBotReply = "Booking is easy! Pick your date and time, and you're set! 📅✅"
                    }
                    OptionButton("See my bookings") {
                        pendingBotReply = "Here's a summary of your upcoming bookings 📅."
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            val userMessage = ChatMessage(inputText, false)
                            val botReply = getBotResponse(inputText)
                            messages = messages + userMessage
                            inputText = ""
                            pendingBotReply = botReply
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
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFFF0F0F0),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
            )
        }
    }
}

@Composable
fun UserMessageBubble(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Text(
        text = "Bot is typing...",
        modifier = Modifier
            .padding(8.dp),
        color = Color.Gray,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun OptionButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

fun getBotResponse(input: String): String {
    return when {
        input.contains("book", ignoreCase = true) -> "You can book a cleaning from your dashboard 📅!"
        input.contains("hello", ignoreCase = true) -> "Hello! 👋 How can I assist you today?"
        input.contains("points", ignoreCase = true) -> "Your loyalty points are available under Profile > Rewards 🏆."
        input.contains("clean", ignoreCase = true) -> "We clean carpets, sofas, and more! 🧼✨"
        else -> "I'm still learning! Try asking about bookings, services, or points! 🙌"
    }
}
