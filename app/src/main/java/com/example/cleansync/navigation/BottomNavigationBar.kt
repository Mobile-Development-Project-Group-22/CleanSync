package com.example.cleansync.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    // Get the current backstack entry to check the selected tab
    val backStackEntry by navController.currentBackStackEntryAsState()

    // List of bottom navigation items
    val tabs = listOf(
        BottomNavItem.Home,
        BottomNavItem.Booking,
        BottomNavItem.Profile
    )

    // Bottom Navigation Bar
    BottomAppBar  {
        tabs.forEach { tab ->
            val selected = tab.route == backStackEntry?.destination?.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        // Pop up to the start destination to avoid stacking duplicate destinations
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.HomeScreen.route, "Home", Icons.Filled.Home)
    object Booking : BottomNavItem(Screen.BookingScreen.route, "Booking", Icons.Filled.Add)
    object Profile : BottomNavItem(Screen.ProfileScreen.route, "Profile", Icons.Filled.Person)
}
