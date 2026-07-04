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
import com.arsham.dorom.data.entity.JournalEntry
import com.arsham.dorom.data.entity.LongTermGoal
import com.arsham.dorom.data.entity.MoneyTransaction
import com.arsham.dorom.data.entity.PlanTask
import com.arsham.dorom.data.entity.TimeMarker
import com.arsham.dorom.data.entity.WeeklyPlan
import com.arsham.dorom.data.entity.WeeklyPlanItem
import com.arsham.dorom.data.entity.WorkoutDay
import com.arsham.dorom.data.entity.WorkoutExercise
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
    @Query("SELECT * FROM long_term_goal WHERE isArchived = 0 ORDER BY createdAtEpochMillis DESC")
    fun observeGoals(): Flow<List<LongTermGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: LongTermGoal): Long

    @Delete
    suspend fun deleteGoal(goal: LongTermGoal)
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
interface WorkoutDao {
    @Query("SELECT * FROM workout_day ORDER BY orderIndex ASC")
    fun observeDays(): Flow<List<WorkoutDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: WorkoutDay): Long

    @Delete
    suspend fun deleteDay(day: WorkoutDay)

    @Query("SELECT * FROM workout_exercise WHERE workoutDayId = :dayId ORDER BY orderIndex ASC")
    fun observeExercises(dayId: Long): Flow<List<WorkoutExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercise: WorkoutExercise): Long

    @Delete
    suspend fun deleteExercise(exercise: WorkoutExercise)
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
