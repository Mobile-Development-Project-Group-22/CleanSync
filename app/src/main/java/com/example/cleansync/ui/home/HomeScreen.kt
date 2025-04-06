package com.example.cleansync.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cleansync.navigation.Screen
import com.example.cleansync.ui.auth.AuthState
import com.example.cleansync.ui.auth.AuthViewModel
import com.example.cleansync.ui.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = ProfileViewModel() // Assuming you have a ProfileViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = profileViewModel.currentUser

    LaunchedEffect(authState) {

        when (authState) {
            is AuthState.Success -> {
                if (navController.currentBackStackEntry?.destination?.route == Screen.LoginScreen.route) {
                    navController.popBackStack()
                }
            }
            is AuthState.Error -> {
                // Handle error state
                val errorMessage = (authState as AuthState.Error).message
                // Show a Snackbar or Toast with the error message
            }
            is AuthState.Loading -> {
                // Show loading indicator if needed
            }

            else -> {
                // User is not logged in, navigate to LoginScreen
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CleanSync",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Welcome message with dynamic name
                Text(
                    text = "Welcome ${currentUser?.displayName ?: "Guest"}!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Navigate to Booking Screen
                Button(
                    onClick = {
                        navController.navigate(Screen.BookingScreen.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp), // Consistent button height
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Go to Booking", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button with accessible design
                OutlinedButton(
                    onClick = {
                        authViewModel.signOut()
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(Screen.HomeScreen.route) { inclusive = true }
                            launchSingleTop = true
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp), // Consistent button height
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navController = rememberNavController(),
        authViewModel = AuthViewModel()
    )
}
