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
    val currentUser = auth.currentUser

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }

    val chatId = currentUser?.uid ?: return

    // Listen for messages
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(messages) { message ->
                if (message.senderId == chatId) {
                    Text(
                        text = "You: ${message.text}",
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    Text(
                        text = "Agent: ${message.text}",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
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
            }) {
                Text("Send")
            }
        }
    }
}
