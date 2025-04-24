package com.example.cleansync.ui.booking

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

@Composable
fun BookingStatusExplanationAnimation(onDismiss: () -> Unit) {
    val stages = listOf("Booked", "Collected", "Cleaned", "Returned")
    val primaryColor = MaterialTheme.colorScheme.primary

    var currentStage by remember { mutableStateOf(-1) }

    // Trigger animation
    LaunchedEffect(Unit) {
        for (i in stages.indices) {
            currentStage = i
            delay(1000L)
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

                        val isActive = index <= currentStage
                        val isCurrent = index == currentStage

                        val animatedOffsetY by animateDpAsState(
                            targetValue = if (isActive) 0.dp else 30.dp,
                            animationSpec = tween(durationMillis = 500),
                            label = "offsetY"
                        )

                        val animatedAlpha by animateFloatAsState(
                            targetValue = if (isActive) 1f else 0f,
                            animationSpec = tween(durationMillis = 500),
                            label = "alpha"
                        )

                        Column(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(y = animatedOffsetY)
                                    .size(40.dp)
                                    .background(
                                        color = if (isActive) primaryColor else Color.Gray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stage.first().toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            AnimatedVisibility(
                                visible = isActive,
                                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                                    initialOffsetY = { it / 2 }
                                ),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = stage,
                                    modifier = Modifier.padding(top = 4.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (index < stages.size - 1) {
                            val lineProgress by animateFloatAsState(
                                targetValue = when {
                                    currentStage > index -> 1f
                                    currentStage == index -> 0.5f
                                    else -> 0f
                                },
                                animationSpec = tween(durationMillis = 500),
                                label = "lineProgress"
                            )

                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(lineProgress)
                                        .background(primaryColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
