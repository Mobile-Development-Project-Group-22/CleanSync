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

data class ChatMessage(
    val content: String,
    val isBot: Boolean,
    val showBookNowButton: Boolean = false
)
@Composable
fun ChatbotDialog(
    onDismiss: () -> Unit,
    onNavigateToBooking: () -> Unit,
    onNavigateToMyBookings: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf(ChatMessage("Hi there! 👋 How can I help you today?", true))) }
    var inputText by remember { mutableStateOf("") }
    var isBotTyping by remember { mutableStateOf(false) }
    var pendingBotReply by remember { mutableStateOf<ChatMessage?>(null) }
    var showFAQOptions by remember { mutableStateOf(true) }  // Set to true initially to show FAQ options after greeting

    // To ensure bot messages are shown one at a time, delay the response
    pendingBotReply?.let { replyMessage ->
        LaunchedEffect(replyMessage) {
            isBotTyping = true
            delay(1500)  // Simulating bot typing
            messages = messages + replyMessage
            isBotTyping = false
            pendingBotReply = null  // Clear the pending bot reply after adding it to the messages list
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
                                BotMessageBubble(
                                    message.content,
                                    showBookNow = message.showBookNowButton,
                                    onBookNowClick = onNavigateToBooking
                                )
                            } else {
                                UserMessageBubble(message.content)
                            }
                        }
                    }
                    if (isBotTyping) {
                        item { TypingIndicator() }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Show FAQ options only after the bot's message when the user input is unrecognized
                if (showFAQOptions) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp) // Set a max height for the FAQ options
                            .padding(top = 8.dp)
                    ) {
                        LazyColumn {
                            item {
                                FAQOptions(
                                    onSelect = { question ->
                                        when (question) {
                                            "What is CleanSync?" -> {
                                                pendingBotReply = ChatMessage(
                                                    "🧹 CleanSync connects you to trusted cleaning professionals easily and quickly!",
                                                    isBot = true
                                                )
                                                messages = messages + ChatMessage(question, false)
                                            }
                                            "How to book a cleaning?" -> {
                                                pendingBotReply = ChatMessage(
                                                    "📅 To book a cleaning, just go to your dashboard and pick a date and time. Simple and quick!",
                                                    isBot = true,
                                                    showBookNowButton = true
                                                )
                                                messages = messages + ChatMessage(question, false)
                                            }
                                            "How does booking work?" -> {
                                                pendingBotReply = ChatMessage(
                                                    "🧽 Booking works like magic! Choose a service, pick a date, confirm your address, and you're done! Do you want to try booking now?",
                                                    isBot = true,
                                                    showBookNowButton = true
                                                )
                                                messages = messages + ChatMessage(question, false)
                                            }
                                            "See my upcoming bookings" -> {
                                                onNavigateToMyBookings()
                                            }
                                        }
                                        showFAQOptions = false  // Hide the options once one is selected
                                    }
                                )
                            }
                        }
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
                            // Add the user's message
                            messages = messages + ChatMessage(inputText, false)

                            // Check if the input is recognized as a valid FAQ or not
                            if (inputText !in listOf("What is CleanSync?", "How to book a cleaning?", "How does booking work?", "See my upcoming bookings")) {
                                // Check if we haven't already added a bot reply for unrecognized input
                                if (messages.none { it.content == "🤖 I only answer FAQs. Please select from the options above." }) {
                                    pendingBotReply = ChatMessage(
                                        content = "🤖 I only answer FAQs. Please select from the options above.",
                                        isBot = true
                                    )
                                    showFAQOptions = true  // Show the FAQ options after the bot reply
                                }
                            } else {
                                // If the input matches one of the FAQs, show bot reply directly
                                pendingBotReply = ChatMessage(
                                    content = "🤖 I’m answering your FAQ.",
                                    isBot = true
                                )
                            }

                            inputText = ""  // Clear the input text field
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
fun FAQOptions(onSelect: (String) -> Unit) {
    Column {
        OptionButton("What is CleanSync?") { onSelect("What is CleanSync?") }
        OptionButton("How to book a cleaning?") { onSelect("How to book a cleaning?") }
        OptionButton("How does booking work?") { onSelect("How does booking work?") }
        OptionButton("See my upcoming bookings") { onSelect("See my upcoming bookings") }
    }
}

@Composable
fun BotMessageBubble(
    message: String,
    showBookNow: Boolean = false,
    onBookNowClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
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
        if (showBookNow) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onBookNowClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Book Now")
            }
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
        modifier = Modifier.padding(8.dp),
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
