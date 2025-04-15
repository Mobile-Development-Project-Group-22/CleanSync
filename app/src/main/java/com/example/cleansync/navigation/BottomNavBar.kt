package com.example.cleansync.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController, unreadCount: Int) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home, "home_screen"),
        NavigationItem("Booking", Icons.Default.Home, "booking_screen"),
        NavigationItem("Notifications", Icons.Default.Notifications, "notification_screen"),
        NavigationItem("Profile", Icons.Default.Person, "profile_screen")
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        var unreadCount by remember { mutableStateOf(unreadCount) }

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.label == "Notifications" && unreadCount > 0) {
                        // Show badge on the notifications icon if there are unread notifications
                        BadgedBox(badge = {
                            Badge {
                                Text(text = unreadCount.toString())
                            }
                        }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
