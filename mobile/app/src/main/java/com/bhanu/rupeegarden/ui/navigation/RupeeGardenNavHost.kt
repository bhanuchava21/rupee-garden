package com.bhanu.rupeegarden.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bhanu.rupeegarden.ui.screens.achievements.AchievementsScreen
import com.bhanu.rupeegarden.ui.screens.completion.CompletionScreen
import com.bhanu.rupeegarden.ui.screens.enddaycheck.EndDayCheckScreen
import com.bhanu.rupeegarden.ui.screens.garden.GardenScreen
import com.bhanu.rupeegarden.ui.screens.home.HomeScreen
import com.bhanu.rupeegarden.ui.screens.impulse.ImpulseCheckScreen
import com.bhanu.rupeegarden.ui.screens.insights.InsightsScreen
import com.bhanu.rupeegarden.ui.screens.session.SaveSessionScreen
import com.bhanu.rupeegarden.ui.screens.settings.SettingsScreen

@Composable
fun RupeeGardenNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartSession = {
                    navController.navigate(Screen.Session.route)
                },
                onNavigateToGarden = {
                    navController.navigate(Screen.Garden.route)
                },
                onNavigateToInsights = {
                    navController.navigate(Screen.Insights.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onResumeSession = {
                    navController.navigate(Screen.Session.route)
                },
                onNavigateToPauseSpend = {
                    navController.navigate(Screen.ImpulseCheck.route)
                }
            )
        }

        composable(Screen.Session.route) {
            SaveSessionScreen(
                onEndDay = {
                    navController.navigate(Screen.EndDayCheck.route) {
                        popUpTo(Screen.Session.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EndDayCheck.route) {
            EndDayCheckScreen(
                onComplete = { saved, xpEarned ->
                    navController.navigate(Screen.Completion.createRoute(saved, xpEarned)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Completion.route,
            arguments = listOf(
                navArgument("saved") { type = NavType.BoolType },
                navArgument("xpEarned") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val saved = backStackEntry.arguments?.getBoolean("saved") ?: true
            val xpEarned = backStackEntry.arguments?.getInt("xpEarned") ?: 0

            CompletionScreen(
                saved = saved,
                xpEarned = xpEarned,
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onViewGarden = {
                    navController.navigate(Screen.Garden.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.Garden.route) {
            GardenScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToPauseSpend = {
                    navController.navigate(Screen.ImpulseCheck.route)
                }
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToAchievements = {
                    navController.navigate(Screen.Achievements.route)
                }
            )
        }

        composable(Screen.Achievements.route) {
            AchievementsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ImpulseCheck.route) {
            ImpulseCheckScreen(
                onComplete = { _, _, _ ->
                    // Silent completion - just go back home
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
