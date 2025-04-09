// BookingConfirmationScreen.kt

package com.example.cleansync.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter

@Composable
fun BookingConfirmationScreen(
    bookingViewModel: BookingViewModel,
    onReturnHome: () -> Unit
) {
    val selectedDateTime = bookingViewModel.selectedDateTime
    val formattedDateTime = selectedDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
    val estimatedPrice = bookingViewModel.estimatedPrice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Booking Confirmed!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        formattedDateTime?.let {
            Text(text = "📅 Date & Time: $it", fontSize = 16.sp)
        }

        estimatedPrice?.let {
            Text(text = "💰 Estimated Price: €$it", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onReturnHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}
