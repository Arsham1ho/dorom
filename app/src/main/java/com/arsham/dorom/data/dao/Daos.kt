package com.arsham.dorom.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.arsham.dorom.data.entity.Course
import com.arsham.dorom.data.entity.DailyPlan
import com.arsham.dorom.data.entity.DailyReview
import com.arsham.dorom.data.entity.GuitarRecording
import com.arsham.dorom.data.entity.GuitarSong
import com.arsham.dorom.data.entity.GuitarTabImage
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.data.entity.GymScheduleEntry
import com.arsham.dorom.data.entity.GymSession
import com.arsham.dorom.data.entity.GymSessionSet
import com.arsham.dorom.data.entity.FinnishPracticeEntry
import com.arsham.dorom.data.entity.JournalEntry
import com.arsham.dorom.data.entity.LongTermGoal
import com.arsham.dorom.data.entity.MoneyTransaction
import com.arsham.dorom.data.entity.MoodEntry
import com.arsham.dorom.data.entity.PersonalProject
import com.arsham.dorom.data.entity.PlanTask
import com.arsham.dorom.data.entity.TimeMarker
import com.arsham.dorom.data.entity.WeeklyPlan
import com.arsham.dorom.data.entity.WeeklyPlanItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM daily_plan WHERE date = :date")
    fun observePlan(date: String): Flow<DailyPlan?>

    @Query("SELECT * FROM daily_plan WHERE date = :date")
    suspend fun getPlan(date: String): DailyPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(plan: DailyPlan)

    @Query("SELECT * FROM plan_task WHERE date = :date ORDER BY orderIndex ASC")
    fun observeTasks(date: String): Flow<List<PlanTask>>

    @Query("SELECT * FROM plan_task WHERE date = :date ORDER BY orderIndex ASC")
    suspend fun getTasks(date: String): List<PlanTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: PlanTask): Long

    @Update
    suspend fun updateTask(task: PlanTask)

    @Delete
    suspend fun deleteTask(task: PlanTask)

    @Query("DELETE FROM plan_task WHERE date = :date")
    suspend fun deleteTasksForDate(date: String)

    @Query("SELECT DISTINCT date FROM plan_task ORDER BY date DESC")
    suspend fun getAllPlannedDates(): List<String>
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM long_term_goal WHERE isArchived = 0 AND deletedAtEpochMillis IS NULL ORDER BY createdAtEpochMillis DESC")
    fun observeGoals(): Flow<List<LongTermGoal>>

    @androidx.room.Upsert
    suspend fun upsertGoal(goal: LongTermGoal)

    @Query("SELECT * FROM long_term_goal WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<LongTermGoal>

    @Query("UPDATE long_term_goal SET pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("SELECT * FROM long_term_goal WHERE id = :id")
    suspend fun getById(id: String): LongTermGoal?

    @Query("DELETE FROM long_term_goal WHERE id = :id")
    suspend fun hardDeleteById(id: String)
}

@Dao
interface PersonalProjectDao {
    @Query("SELECT * FROM personal_project WHERE isArchived = 0 ORDER BY createdAtEpochMillis DESC")
    fun observeProjects(): Flow<List<PersonalProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(project: PersonalProject): Long

    @Delete
    suspend fun deleteProject(project: PersonalProject)
}

@Dao
interface FinnishPracticeDao {
    @Query("SELECT * FROM finnish_practice_entry ORDER BY date DESC")
    fun observeEntries(): Flow<List<FinnishPracticeEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: FinnishPracticeEntry): Long

    @Delete
    suspend fun deleteEntry(entry: FinnishPracticeEntry)
}

@Dao
interface WeeklyPlanDao {
    @Query("SELECT * FROM weekly_plan WHERE weekStartDate = :weekStartDate")
    fun observeWeeklyPlan(weekStartDate: String): Flow<WeeklyPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeeklyPlan(plan: WeeklyPlan)

    @Query("SELECT * FROM weekly_plan_item WHERE weekStartDate = :weekStartDate ORDER BY dayOfWeek ASC, id ASC")
    fun observeItems(weekStartDate: String): Flow<List<WeeklyPlanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: WeeklyPlanItem): Long

    @Update
    suspend fun updateItem(item: WeeklyPlanItem)

    @Delete
    suspend fun deleteItem(item: WeeklyPlanItem)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM course WHERE isArchived = 0 ORDER BY id DESC")
    fun observeCourses(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourse(course: Course): Long

    @Delete
    suspend fun deleteCourse(course: Course)
}

@Dao
interface TimeMarkerDao {
    @Query("SELECT * FROM time_marker ORDER BY targetEpochMillis ASC")
    fun observeMarkers(): Flow<List<TimeMarker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMarker(marker: TimeMarker): Long

    @Delete
    suspend fun deleteMarker(marker: TimeMarker)
}

@Dao
interface GymDao {
    @Query("SELECT * FROM gym_exercise WHERE location = :location AND category = :category ORDER BY createdAtEpochMillis DESC")
    fun observeExercises(location: GymLocation, category: String): Flow<List<GymExercise>>

    @Query("SELECT * FROM gym_exercise WHERE id IN (:ids)")
    suspend fun getExercisesByIds(ids: List<Long>): List<GymExercise>

    @Insert
    suspend fun insertExercise(exercise: GymExercise): Long

    @Update
    suspend fun updateExercise(exercise: GymExercise)

    @Delete
    suspend fun deleteExercise(exercise: GymExercise)

    @Query("SELECT * FROM gym_schedule_entry WHERE date = :date ORDER BY orderIndex ASC")
    fun observeSchedule(date: String): Flow<List<GymScheduleEntry>>

    @Query("SELECT DISTINCT date FROM gym_schedule_entry")
    fun observePlannedDates(): Flow<List<String>>

    @Query("DELETE FROM gym_schedule_entry WHERE date = :date")
    suspend fun clearSchedule(date: String)

    @Insert
    suspend fun insertScheduleEntry(entry: GymScheduleEntry): Long

    @Query("SELECT * FROM gym_session ORDER BY startEpochMillis DESC")
    fun observeSessions(): Flow<List<GymSession>>

    @Query("SELECT * FROM gym_session WHERE date = :date AND endEpochMillis IS NULL LIMIT 1")
    suspend fun getActiveSessionForDate(date: String): GymSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: GymSession): Long

    @Query("SELECT * FROM gym_session_set WHERE sessionId = :sessionId ORDER BY loggedAtEpochMillis ASC")
    fun observeSets(sessionId: Long): Flow<List<GymSessionSet>>

    @Insert
    suspend fun insertSet(set: GymSessionSet): Long
}

@Dao
interface GuitarDao {
    @Query("SELECT * FROM guitar_song ORDER BY createdAtEpochMillis DESC")
    fun observeSongs(): Flow<List<GuitarSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSong(song: GuitarSong): Long

    @Delete
    suspend fun deleteSong(song: GuitarSong)

    @Query("SELECT * FROM guitar_tab_image WHERE songId = :songId")
    fun observeTabImages(songId: Long): Flow<List<GuitarTabImage>>

    @Insert
    suspend fun insertTabImage(image: GuitarTabImage): Long

    @Delete
    suspend fun deleteTabImage(image: GuitarTabImage)

    @Query("SELECT * FROM guitar_recording WHERE songId = :songId ORDER BY recordedAtEpochMillis DESC")
    fun observeRecordings(songId: Long): Flow<List<GuitarRecording>>

    @Insert
    suspend fun insertRecording(recording: GuitarRecording): Long

    @Delete
    suspend fun deleteRecording(recording: GuitarRecording)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM daily_review WHERE date = :date")
    fun observeReview(date: String): Flow<DailyReview?>

    @Query("SELECT * FROM daily_review WHERE date = :date")
    suspend fun getReview(date: String): DailyReview?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(review: DailyReview)

    @Query("SELECT * FROM daily_review ORDER BY date DESC")
    fun observeAllReviews(): Flow<List<DailyReview>>

    @Query("SELECT * FROM daily_review ORDER BY date DESC LIMIT :limit")
    fun observeRecentReviews(limit: Int): Flow<List<DailyReview>>
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entry ORDER BY createdAtEpochMillis DESC")
    fun observeEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: JournalEntry): Long

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM money_transaction WHERE date LIKE :yearMonth || '%' ORDER BY date DESC, id DESC")
    fun observeForMonth(yearMonth: String): Flow<List<MoneyTransaction>>

    @Query("SELECT * FROM money_transaction ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<MoneyTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(transaction: MoneyTransaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: MoneyTransaction)
}

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entry ORDER BY timestampEpochMillis DESC")
    fun observeAll(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entry WHERE timestampEpochMillis >= :sinceEpochMillis ORDER BY timestampEpochMillis DESC")
    fun observeSince(sinceEpochMillis: Long): Flow<List<MoodEntry>>

    @Insert
    suspend fun insert(entry: MoodEntry): Long

    @Delete
    suspend fun delete(entry: MoodEntry)
}
