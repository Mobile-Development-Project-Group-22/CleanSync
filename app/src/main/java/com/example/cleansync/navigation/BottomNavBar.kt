
package com.example.cleansync.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cleansync.ui.notifications.NotificationViewModel


@Composable
fun BottomNavBar(navController: NavController, unreadCount: Int) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home, "home_screen"),
        NavigationItem("My Bookings", Icons.Default.CalendarToday, "my_bookings_screen"),
        NavigationItem("Notifications", Icons.Default.Notifications, "notification_screen"),
        NavigationItem("Profile", Icons.Default.Person, "profile_screen")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val notificationViewModel: NotificationViewModel = remember { NotificationViewModel() }
    var unreadCount = notificationViewModel.unreadNotificationsCount()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    if (item.label == "Notifications" && unreadCount > 0) {
                        BadgedBox(badge = {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(text = unreadCount.toString())
                            }
                        }) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary, // Selected icon color
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, // Unselected icon color
                    selectedTextColor = MaterialTheme.colorScheme.primary, // Selected text color
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant, // Unselected text color
                    indicatorColor = MaterialTheme.colorScheme.onSecondary // Indicator color
                )
            )
        }
    }
}
