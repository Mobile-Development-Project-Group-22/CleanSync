package com.example.cleansync.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cleansync.navigation.Screen

@Composable
fun BookingScreen(
    bookingViewModel: BookingViewModel,
    onBookingConfirmed: () -> Unit,
    onBookingCancelled: () -> Unit
) {
    Scaffold(
        topBar = {
            BookingTopAppBar() // TopAppBar with text button
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Booking screen content goes here (can be dynamic content based on bookingState)

                // Text or Booking Details (Example Placeholder)
                Text(
                    text = "Booking Details: [Add your booking details here]",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons for Confirm and Cancel booking
                BookingActionButtons(
                    onBookingConfirmed = onBookingConfirmed,
                    onBookingCancelled = onBookingCancelled
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTopAppBar() {
    SmallTopAppBar(
        title = { Text("Booking") },
        actions = {
            // You can add more actions here if necessary (e.g. additional buttons)
            TextButton(
                onClick = {
                    // This button could be used for actions like "Help", "Settings", etc.
                }
            ) {
                Text(
                    text = "Action",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun BookingActionButtons(
    onBookingConfirmed: () -> Unit,
    onBookingCancelled: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = {
                onBookingConfirmed() // This will trigger the confirmation logic
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Booking")
        }

        OutlinedButton(
            onClick = {
                onBookingCancelled() // This will trigger the cancel logic
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel Booking")
        }
    }
}


