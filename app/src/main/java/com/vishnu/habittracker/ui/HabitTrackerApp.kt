package com.vishnu.habittracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vishnu.habittracker.data.repository.AuthState
import com.vishnu.habittracker.ui.auth.AuthViewModel
import com.vishnu.habittracker.ui.auth.LoginScreen
import com.vishnu.habittracker.ui.navigation.Screen

/**
 * Bottom navigation item data class.
 */
data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Root app composable with auth guard.
 *
 * Auth flow (ported from auth.js):
 * - Loading → Splash/spinner
 * - Unauthenticated → LoginScreen
 * - Authenticated → Main app with bottom nav
 */
@Composable
fun HabitTrackerApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Loading -> {
            // Loading — show centered spinner
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is AuthState.Unauthenticated, is AuthState.Error -> {
            // Not logged in — show login screen
            LoginScreen(viewModel = authViewModel)
        }
        is AuthState.Authenticated -> {
            // Logged in — show main app
            MainAppContent(authViewModel = authViewModel)
        }
    }
}

/**
 * Main app content with bottom navigation.
 * Only shown when authenticated.
 */
@Composable
private fun MainAppContent(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Dashboard", Screen.Dashboard.route, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        BottomNavItem("Habits", Screen.Habits.route, Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges),
        BottomNavItem("Goals", Screen.Goals.route, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
        BottomNavItem("Events", Screen.Events.route, Icons.Filled.Event, Icons.Outlined.Event),
        BottomNavItem("Calendar", Screen.Calendar.route, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem("Analytics", Screen.Analytics.route, Icons.Filled.Analytics, Icons.Outlined.Analytics),
        BottomNavItem("Expenses", Screen.Expenses.route, Icons.Filled.Payments, Icons.Outlined.Payments),
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Hide bottom bar in full-screen modes (focus mode)
            val showBottomBar = currentDestination?.route?.let { route ->
                !route.startsWith("focus/")
            } ?: true

            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(text = item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Placeholder screens — will be replaced with full implementations
            composable(Screen.Dashboard.route) {
                PlaceholderScreen(
                    "Dashboard",
                    "📋 Welcome, ${authViewModel.getCurrentUserEmail() ?: "User"}!"
                )
            }
            composable(Screen.Habits.route) {
                PlaceholderScreen("Habits", "⭐ Daily Habit Tracking")
            }
            composable(Screen.Goals.route) {
                PlaceholderScreen("Goals", "🏆 Long-term Objectives")
            }
            composable(Screen.Events.route) {
                PlaceholderScreen("Events", "📅 Event Countdowns")
            }
            composable(Screen.Calendar.route) {
                PlaceholderScreen("Calendar", "📆 Monthly View")
            }
            composable(Screen.Analytics.route) {
                PlaceholderScreen("Analytics", "📊 Productivity Insights")
            }
            composable(Screen.Expenses.route) {
                PlaceholderScreen("Expenses", "💰 Expense Tracking")
            }
        }
    }
}
