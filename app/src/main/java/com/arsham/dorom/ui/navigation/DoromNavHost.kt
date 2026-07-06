package com.arsham.dorom.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.arsham.dorom.ui.feedback.FeedbackScreen
import com.arsham.dorom.ui.finance.FinanceScreen
import com.arsham.dorom.ui.goals.GoalsScreen
import com.arsham.dorom.ui.guitar.GuitarSongScreen
import com.arsham.dorom.ui.guitar.GuitarScreen
import com.arsham.dorom.ui.gym.GymCalendarScreen
import com.arsham.dorom.ui.gym.GymCategoryScreen
import com.arsham.dorom.ui.gym.GymHistoryScreen
import com.arsham.dorom.ui.gym.GymScreen
import com.arsham.dorom.ui.gym.GymSessionScreen
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.ui.history.HistoryScreen
import com.arsham.dorom.ui.home.HomeScreen
import com.arsham.dorom.ui.journal.JournalHubScreen
import com.arsham.dorom.ui.journal.PersonalJournalScreen
import com.arsham.dorom.ui.mood.MoodReportScreen
import com.arsham.dorom.ui.plan.PlanScreen
import com.arsham.dorom.ui.settings.SettingsScreen
import com.arsham.dorom.ui.timemarkers.TimeMarkersScreen
import com.arsham.dorom.ui.courses.CoursesScreen
import com.arsham.dorom.ui.track.TrackHubScreen
import com.arsham.dorom.ui.weekly.WeeklyPlanScreen

private const val ANIM_MS = 260

@Composable
fun DoromNavHost(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(tween(ANIM_MS)) { it / 4 } + fadeIn(tween(ANIM_MS)) },
        exitTransition = { fadeOut(tween(ANIM_MS)) },
        popEnterTransition = { fadeIn(tween(ANIM_MS)) },
        popExitTransition = { slideOutHorizontally(tween(ANIM_MS)) { it / 4 } + fadeOut(tween(ANIM_MS)) },
    ) {
        composable(Routes.HOME) {
            HomeScreen(onNavigate = { navController.navigate(it) })
        }
        composable(Routes.PLAN) {
            PlanScreen(onNavigate = { navController.navigate(it) })
        }
        composable(Routes.WEEKLY) {
            WeeklyPlanScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.GOALS) {
            GoalsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRACK) {
            TrackHubScreen(onNavigate = { navController.navigate(it) })
        }
        composable(Routes.TRACK_COURSES) {
            CoursesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRACK_PERSONAL_PROJECTS) {
            com.arsham.dorom.ui.projects.PersonalProjectsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRACK_FINNISH) {
            com.arsham.dorom.ui.finnish.FinnishScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRACK_TIME_MARKERS) {
            TimeMarkersScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRACK_GYM) {
            GymScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) },
            )
        }
        composable(
            Routes.GYM_CATEGORY,
            arguments = listOf(navArgument("location") { type = NavType.StringType }, navArgument("category") { type = NavType.StringType }),
        ) { entry ->
            val location = GymLocation.valueOf(entry.arguments?.getString("location") ?: GymLocation.GYM.name)
            val category = entry.arguments?.getString("category") ?: ""
            GymCategoryScreen(location = location, category = category, onBack = { navController.popBackStack() })
        }
        composable(
            Routes.GYM_CALENDAR,
            arguments = listOf(navArgument("location") { type = NavType.StringType }),
        ) { entry ->
            val location = GymLocation.valueOf(entry.arguments?.getString("location") ?: GymLocation.GYM.name)
            GymCalendarScreen(location = location, onBack = { navController.popBackStack() })
        }
        composable(
            Routes.GYM_SESSION,
            arguments = listOf(navArgument("location") { type = NavType.StringType }),
        ) { entry ->
            val location = GymLocation.valueOf(entry.arguments?.getString("location") ?: GymLocation.GYM.name)
            GymSessionScreen(location = location, onBack = { navController.popBackStack() }, onNavigate = { navController.navigate(it) })
        }
        composable(Routes.GYM_HISTORY) {
            GymHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRACK_FINANCE) {
            FinanceScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TRACK_GUITAR) {
            GuitarScreen(
                onBack = { navController.popBackStack() },
                onOpenSong = { songId -> navController.navigate(Routes.guitarSong(songId)) },
            )
        }
        composable(
            Routes.TRACK_GUITAR_SONG,
            arguments = listOf(navArgument("songId") { type = NavType.LongType }),
        ) { entry ->
            val songId = entry.arguments?.getLong("songId") ?: 0L
            GuitarSongScreen(songId = songId, onBack = { navController.popBackStack() })
        }
        composable(Routes.JOURNAL) {
            JournalHubScreen(onNavigate = { navController.navigate(it) })
        }
        composable(Routes.FEEDBACK) {
            FeedbackScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PERSONAL_JOURNAL) {
            PersonalJournalScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() }, onNavigate = { navController.navigate(it) })
        }
        composable(Routes.SIGN_IN) {
            com.arsham.dorom.ui.auth.SignInScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MOOD_REPORT) {
            MoodReportScreen()
        }
    }
}
