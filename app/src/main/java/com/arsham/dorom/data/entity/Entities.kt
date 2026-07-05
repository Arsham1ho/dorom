package com.arsham.dorom.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Dates are stored as ISO-8601 strings (yyyy-MM-dd) and times as "HH:mm" so they sort/compare
// as plain text without needing custom Room type converters for date math we don't do in SQL.

@Entity(tableName = "daily_plan")
data class DailyPlan(
    @PrimaryKey val date: String, // yyyy-MM-dd, the day this plan is FOR
    val wakeTargetTime: String, // HH:mm
    val bedTargetTime: String, // HH:mm
    val createdAtEpochMillis: Long,
)

@Entity(tableName = "plan_task")
data class PlanTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // FK -> DailyPlan.date
    val title: String,
    val category: String,
    val startTime: String, // HH:mm
    val endTime: String? = null, // HH:mm, optional
    val orderIndex: Int,
    val isDone: Boolean = false,
    val completedAtEpochMillis: Long? = null,
)

@Entity(tableName = "long_term_goal")
data class LongTermGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val targetDate: String? = null, // yyyy-MM-dd
    val progressPercent: Int = 0,
    val isArchived: Boolean = false,
    val createdAtEpochMillis: Long,
)

@Entity(tableName = "weekly_plan")
data class WeeklyPlan(
    @PrimaryKey val weekStartDate: String, // yyyy-MM-dd, Monday of that week
    val theme: String,
    val notes: String,
)

@Entity(tableName = "weekly_plan_item")
data class WeeklyPlanItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartDate: String, // FK -> WeeklyPlan.weekStartDate
    val dayOfWeek: Int, // 1=Mon .. 7=Sun
    val description: String,
    val isDone: Boolean = false,
)

@Entity(tableName = "course")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val totalLessons: Int,
    val completedLessons: Int = 0,
    val notes: String = "",
    val isArchived: Boolean = false,
)

enum class TimeDirection { COUNTDOWN, COUNTUP }

@Entity(tableName = "time_marker")
data class TimeMarker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetEpochMillis: Long,
    val direction: TimeDirection,
    val notes: String = "",
)

enum class GymLocation { GYM, HOME_GYM }

val GYM_CATEGORIES = listOf("Chest", "Back", "Shoulder", "Bicep", "Triceps", "Forearm", "Leg", "Abs", "Cardio")

@Entity(tableName = "gym_exercise")
data class GymExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val location: GymLocation,
    val category: String,
    val name: String,
    val imagePath: String? = null,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val defaultWeight: Double = 0.0,
    val createdAtEpochMillis: Long,
)

/** One exercise assigned to a calendar date — a day's plan is just the set of entries sharing a date. */
@Entity(tableName = "gym_schedule_entry")
data class GymScheduleEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val exerciseId: Long,
    val orderIndex: Int = 0,
)

@Entity(tableName = "gym_session")
data class GymSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val location: GymLocation,
    val startEpochMillis: Long,
    val endEpochMillis: Long? = null,
)

@Entity(tableName = "gym_session_set")
data class GymSessionSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val restSeconds: Int, // measured break since the previous set finished
    val loggedAtEpochMillis: Long,
)

enum class SongStatus { LEARNING, LEARNED }

@Entity(tableName = "guitar_song")
data class GuitarSong(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String = "",
    val status: SongStatus = SongStatus.LEARNING,
    val notes: String = "",
    val createdAtEpochMillis: Long,
)

@Entity(tableName = "guitar_tab_image")
data class GuitarTabImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val imagePath: String,
)

@Entity(tableName = "guitar_recording")
data class GuitarRecording(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val audioPath: String,
    val recordedAtEpochMillis: Long,
    val note: String = "",
)

@Entity(tableName = "daily_review")
data class DailyReview(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val plannedCount: Int,
    val completedCount: Int,
    val percentage: Int,
    val score: Int,
    val feedbackText: String = "",
    val feedbackAudioPath: String? = null,
    val actualWakeTime: String? = null, // HH:mm
    val actualSleepTime: String? = null, // HH:mm
    val isFinalized: Boolean = false,
)

@Entity(tableName = "journal_entry")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val encryptedContent: String, // Base64 ciphertext, decrypted only in-memory via JournalCrypto
)

enum class TransactionType { INCOME, EXPENSE, SAVING }

@Entity(tableName = "money_transaction")
data class MoneyTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val note: String = "",
)

@Entity(tableName = "mood_entry")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emoji: String,
    val timestampEpochMillis: Long,
    val note: String = "",
)
