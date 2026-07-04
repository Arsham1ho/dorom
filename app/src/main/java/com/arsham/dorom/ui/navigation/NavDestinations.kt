package com.arsham.dorom.ui.navigation

object Routes {
    const val HOME = "home"
    const val PLAN = "plan"
    const val WEEKLY = "weekly"
    const val GOALS = "goals"
    const val TRACK = "track"
    const val TRACK_COURSES = "track_courses"
    const val TRACK_TIME_MARKERS = "track_time_markers"
    const val TRACK_GYM = "track_gym"
    const val TRACK_GYM_DAY = "track_gym_day/{dayId}"
    const val TRACK_FINANCE = "track_finance"
    const val TRACK_GUITAR = "track_guitar"
    const val TRACK_GUITAR_SONG = "track_guitar_song/{songId}"
    const val JOURNAL = "journal"
    const val FEEDBACK = "feedback"
    const val PERSONAL_JOURNAL = "personal_journal"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    fun gymDay(dayId: Long) = "track_gym_day/$dayId"
    fun guitarSong(songId: Long) = "track_guitar_song/$songId"
}

data class BottomDestination(val route: String, val label: String)

val BottomDestinations = listOf(
    BottomDestination(Routes.HOME, "Home"),
    BottomDestination(Routes.PLAN, "Plan"),
    BottomDestination(Routes.TRACK, "Track"),
    BottomDestination(Routes.JOURNAL, "Journal"),
)
