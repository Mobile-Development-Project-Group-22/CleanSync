package com.example.cleansync.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController
) {
    // Get current screen title dynamically
    val screenTitle = "Booking" // You can hardcode it or use dynamic logic here

    Scaffold(

        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to the Booking Screen!",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    // Handle button click to navigate or perform an action
                    navController.navigate("some_route")  // Example navigation
                }) {
                    Text("Book Now")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    // Handle cancellation or navigate back
                    navController.popBackStack()  // Go back to the previous screen
                }) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    // Preview the BookingScreen
    BookingScreen(navController = rememberNavController())
}
