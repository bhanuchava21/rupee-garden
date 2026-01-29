package com.bhanu.rupeegarden.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Session : Screen("session")
    object EndDayCheck : Screen("end_day_check")
    object Completion : Screen("completion/{saved}/{xpEarned}") {
        fun createRoute(saved: Boolean, xpEarned: Int): String {
            return "completion/$saved/$xpEarned"
        }
    }
    object Garden : Screen("garden")
    object Insights : Screen("insights")
    object Settings : Screen("settings")
    object Achievements : Screen("achievements")
}
