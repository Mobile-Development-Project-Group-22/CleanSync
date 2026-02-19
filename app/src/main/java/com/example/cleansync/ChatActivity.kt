package com.example.cleansync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cleansync.ui.theme.CleanSyncTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource


class ChatActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CleanSyncTheme {
                ChatScreen()
            }
        }
    }
}


@Composable
fun ChatScreen() {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser ?: return
    val chatId = currentUser.uid

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }

    LaunchedEffect(Unit) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    messages = it.toObjects(Message::class.java)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // 🔹 Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_company_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "CleanSync Support",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Online",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Divider()

        // 🔹 Chat Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message, chatId)
            }
        }

        // 🔹 Bottom Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") },
                shape = RoundedCornerShape(30.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (messageText.isNotEmpty()) {
                        val message = Message(
                            senderId = chatId,
                            text = messageText,
                            timestamp = System.currentTimeMillis()
                        )

                        db.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .add(message)

                        messageText = ""
                    }
                },
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, currentUserId: String) {

    val isUser = message.senderId == currentUserId

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {

        Box(
            modifier = Modifier
                .padding(6.dp)
                .background(
                    color = if (isUser) Color(0xFF1976D2) else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else Color.Black
            )
        }
    }
}
