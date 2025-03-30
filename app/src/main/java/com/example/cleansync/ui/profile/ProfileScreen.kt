package com.example.cleansync.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.cleansync.R

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
navController: NavController
    ) {
    // Collect state from the ViewModel
    val profileState = profileViewModel.profileState.collectAsState().value
    val currentUser = profileViewModel.currentUser
    val context = LocalContext.current

    // Show Loading
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (profileState is ProfileState.Loading) {
            CircularProgressIndicator()
        }

        // Content Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Image(
                painter = rememberAsyncImagePainter(currentUser?.photoUrl ?: Uri.EMPTY),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .padding(16.dp)
                    .clip(CircleShape)
            )

            // Display name and email
            Text(
                text = currentUser?.displayName ?: "No Name",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = currentUser?.email ?: "No Email",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Update Button
            Button(
                onClick = {
                    // Handle Profile Update
                    profileViewModel.updateUserProfile("New Name", Uri.parse("https://example.com/photo.jpg"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Update Profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Update Email Button
            Button(
                onClick = {
                    // Handle Email Update
                    profileViewModel.updateEmail("newemail@example.com")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Update Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Send Verification Email
            Button(
                onClick = {
                    profileViewModel.sendVerificationEmail()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Send Verification Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Account Button
            Button(
                onClick = {
                    profileViewModel.deleteUser()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red, // Set the background color
                    contentColor = Color.White // Set the text color
                ),            ) {
                Text(text = "Delete Account")
            }
        }
    }

    // Handle ProfileState Success/Error
    if (profileState is ProfileState.Error) {
        LaunchedEffect(profileState) {
            Toast.makeText(context, profileState.message, Toast.LENGTH_LONG).show()
        }
    }

    if (profileState is ProfileState.Success) {
        LaunchedEffect(profileState) {
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_LONG).show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen(
        profileViewModel = ProfileViewModel(),
        navController = NavController(LocalContext.current)
    )
}
