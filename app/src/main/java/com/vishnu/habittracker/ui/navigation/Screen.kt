package com.vishnu.habittracker.ui.navigation

/**
 * Navigation routes for the app — maps to the webapp's 7 main views.
 */
sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Dashboard : Screen("dashboard")
    data object Habits : Screen("habits")
    data object HabitProgress : Screen("habits/{habitId}/progress") {
        fun createRoute(habitId: String) = "habits/$habitId/progress"
    }
    data object Goals : Screen("goals")
    data object Events : Screen("events")
    data object Calendar : Screen("calendar")
    data object Analytics : Screen("analytics")
    data object Expenses : Screen("expenses")
    data object FocusMode : Screen("focus/{taskId}") {
        fun createRoute(taskId: String) = "focus/$taskId"
    }
    data object Settings : Screen("settings")
}
