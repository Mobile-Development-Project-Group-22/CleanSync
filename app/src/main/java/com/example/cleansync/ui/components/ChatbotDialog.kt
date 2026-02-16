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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onNavigateToMyBookings: () -> Unit,
    onNavigateToAgentChat: () -> Unit   // 🔥 NEW CALLBACK
) {

    var messages by remember {
        mutableStateOf(
            listOf(ChatMessage("Hi there! 👋 How can I help you today?", true))
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isBotTyping by remember { mutableStateOf(false) }
    var pendingBotReply by remember { mutableStateOf<ChatMessage?>(null) }
    var showFAQOptions by remember { mutableStateOf(true) }

    // Simulated bot typing delay
    pendingBotReply?.let { replyMessage ->
        LaunchedEffect(replyMessage) {
            isBotTyping = true
            delay(1200)
            messages = messages + replyMessage
            isBotTyping = false
            pendingBotReply = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                "CleanSync Assistant 🤖",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {

                // Messages List
                LazyColumn(
                    modifier = Modifier
                        .heightIn(min = 100.dp, max = 400.dp)
                        .fillMaxWidth(),
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

                // FAQ OPTIONS
                if (showFAQOptions) {
                    FAQOptions(
                        onSelect = { question ->

                            messages = messages + ChatMessage(question, false)

                            when (question) {
                                "What is CleanSync?" -> {
                                    pendingBotReply = ChatMessage(
                                        "🧹 CleanSync connects you to trusted cleaning professionals easily and quickly!",
                                        true
                                    )
                                }

                                "How to book a cleaning?" -> {
                                    pendingBotReply = ChatMessage(
                                        "📅 To book a cleaning, click the 'Book Now' button below to get started.",
                                        true,
                                        showBookNowButton = true
                                    )
                                }

                                "How does booking work?" -> {
                                    pendingBotReply = ChatMessage(
                                        "🧽 Choose service → Pick date → Confirm address → Done! Want to try booking now?",
                                        true,
                                        showBookNowButton = true
                                    )
                                }

                                "See my upcoming bookings" -> {
                                    onNavigateToMyBookings()
                                }
                            }

                            showFAQOptions = false
                        },

                        // 🔥 TALK TO AGENT
                        onTalkToAgent = {
                            messages = messages + ChatMessage("Talk to an Agent", false)

                            pendingBotReply = ChatMessage(
                                "🔗 Connecting you to a live agent...",
                                true
                            )

                            showFAQOptions = false

                            // Small delay before opening real chat
                            onNavigateToAgentChat()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // INPUT FIELD
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

                            messages = messages + ChatMessage(inputText, false)

                            pendingBotReply = ChatMessage(
                                "🤖 I currently answer FAQs. Please select from the options above or talk to an agent.",
                                true
                            )

                            showFAQOptions = true
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
fun FAQOptions(
    onSelect: (String) -> Unit,
    onTalkToAgent: () -> Unit
) {
    Column {
        OptionButton("What is CleanSync?") { onSelect("What is CleanSync?") }
        OptionButton("How to book a cleaning?") { onSelect("How to book a cleaning?") }
        OptionButton("How does booking work?") { onSelect("How does booking work?") }
        OptionButton("See my upcoming bookings") { onSelect("See my upcoming bookings") }

        Spacer(modifier = Modifier.height(4.dp))

        // 🔥 NEW OPTION
        OptionButton("Talk to an Agent") { onTalkToAgent() }
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
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Text(
        text = "Connecting...",
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
