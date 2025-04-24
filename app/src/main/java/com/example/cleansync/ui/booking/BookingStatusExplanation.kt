package com.example.cleansync.ui.booking
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween


@Composable
fun BookingStatusExplanationAnimation(onDismiss: () -> Unit) {
    val stages = listOf("Booked", "Collected", "Cleaned", "Returned")
    val colors = listOf(Color.Green, Color.Green, Color.Green, Color.Green)

    var currentStage by remember { mutableStateOf(-1) }

    // Animation control
    LaunchedEffect(Unit) {
        for (i in stages.indices) {
            currentStage = i
            delay(1000) // wait before moving to next
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        },
        title = { Text("Booking Stages") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    stages.forEachIndexed { index, stage ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (index <= currentStage) colors[index] else Color.Gray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stage.first().toString(),
                                    color = Color.White
                                )
                            }
                            Text(
                                stage,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (index < stages.size - 1) {
                            // Progressive line
                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .weight(1f)
                            ) {
                                // Background line
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray)
                                )
                                // Foreground line grows based on progress
                                if (index < currentStage) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth()
                                            .background(Color.Green)
                                    )
                                } else if (index == currentStage) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(1f) // Half-filled as it’s in progress
                                            .background(Color.Green)
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    )
}
