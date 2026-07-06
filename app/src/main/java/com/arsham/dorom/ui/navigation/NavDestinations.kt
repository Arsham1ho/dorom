package com.arsham.dorom.ui.navigation

object Routes {
    const val HOME = "home"
    const val PLAN = "plan"
    const val WEEKLY = "weekly"
    const val GOALS = "goals"
    const val TRACK = "track"
    const val TRACK_COURSES = "track_courses"
    const val TRACK_PERSONAL_PROJECTS = "track_personal_projects"
    const val TRACK_FINNISH = "track_finnish"
    const val TRACK_TIME_MARKERS = "track_time_markers"
    const val TRACK_GYM = "track_gym"
    const val GYM_CATEGORY = "gym_category/{location}/{category}"
    const val GYM_CALENDAR = "gym_calendar/{location}"
    const val GYM_SESSION = "gym_session/{location}"
    const val GYM_HISTORY = "gym_history"
    const val TRACK_FINANCE = "track_finance"
    const val TRACK_GUITAR = "track_guitar"
    const val TRACK_GUITAR_SONG = "track_guitar_song/{songId}"
    const val JOURNAL = "journal"
    const val FEEDBACK = "feedback"
    const val PERSONAL_JOURNAL = "personal_journal"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val MOOD_REPORT = "mood_report"
    const val SIGN_IN = "sign_in"

    fun guitarSong(songId: Long) = "track_guitar_song/$songId"
    fun gymCategory(location: String, category: String) = "gym_category/$location/$category"
    fun gymCalendar(location: String) = "gym_calendar/$location"
    fun gymSession(location: String) = "gym_session/$location"
}

data class BottomDestination(val route: String, val label: String)

val BottomDestinations = listOf(
    BottomDestination(Routes.TRACK, "Track"),
    BottomDestination(Routes.JOURNAL, "Journal"),
    BottomDestination(Routes.HOME, "Home"),
    BottomDestination(Routes.PLAN, "Plan"),
    BottomDestination(Routes.MOOD_REPORT, "Mood"),
)
