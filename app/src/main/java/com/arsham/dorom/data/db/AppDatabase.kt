package com.arsham.dorom.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.arsham.dorom.data.dao.CourseDao
import com.arsham.dorom.data.dao.FinanceDao
import com.arsham.dorom.data.dao.FinnishPracticeDao
import com.arsham.dorom.data.dao.GoalDao
import com.arsham.dorom.data.dao.GuitarDao
import com.arsham.dorom.data.dao.GymDao
import com.arsham.dorom.data.dao.JournalDao
import com.arsham.dorom.data.dao.MoodDao
import com.arsham.dorom.data.dao.PersonalProjectDao
import com.arsham.dorom.data.dao.PlanDao
import com.arsham.dorom.data.dao.ReviewDao
import com.arsham.dorom.data.dao.TimeMarkerDao
import com.arsham.dorom.data.dao.WeeklyPlanDao
import com.arsham.dorom.data.entity.Course
import com.arsham.dorom.data.entity.DailyPlan
import com.arsham.dorom.data.entity.DailyReview
import com.arsham.dorom.data.entity.FinnishPracticeEntry
import com.arsham.dorom.data.entity.GuitarRecording
import com.arsham.dorom.data.entity.GuitarSong
import com.arsham.dorom.data.entity.GuitarTabImage
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.data.entity.GymScheduleEntry
import com.arsham.dorom.data.entity.GymSession
import com.arsham.dorom.data.entity.GymSessionSet
import com.arsham.dorom.data.entity.JournalEntry
import com.arsham.dorom.data.entity.LongTermGoal
import com.arsham.dorom.data.entity.MoneyTransaction
import com.arsham.dorom.data.entity.MoodEntry
import com.arsham.dorom.data.entity.PersonalProject
import com.arsham.dorom.data.entity.PlanTask
import com.arsham.dorom.data.entity.SongStatus
import com.arsham.dorom.data.entity.TimeDirection
import com.arsham.dorom.data.entity.TimeMarker
import com.arsham.dorom.data.entity.TransactionType
import com.arsham.dorom.data.entity.WeeklyPlan
import com.arsham.dorom.data.entity.WeeklyPlanItem

class Converters {
    @TypeConverter
    fun fromTimeDirection(value: TimeDirection): String = value.name

    @TypeConverter
    fun toTimeDirection(value: String): TimeDirection = TimeDirection.valueOf(value)

    @TypeConverter
    fun fromSongStatus(value: SongStatus): String = value.name

    @TypeConverter
    fun toSongStatus(value: String): SongStatus = SongStatus.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromGymLocation(value: GymLocation): String = value.name

    @TypeConverter
    fun toGymLocation(value: String): GymLocation = GymLocation.valueOf(value)
}

@Database(
    entities = [
        DailyPlan::class, PlanTask::class,
        LongTermGoal::class,
        WeeklyPlan::class, WeeklyPlanItem::class,
        Course::class,
        TimeMarker::class,
        GymExercise::class, GymScheduleEntry::class, GymSession::class, GymSessionSet::class,
        GuitarSong::class, GuitarTabImage::class, GuitarRecording::class,
        DailyReview::class,
        JournalEntry::class,
        MoneyTransaction::class,
        MoodEntry::class,
        PersonalProject::class,
        FinnishPracticeEntry::class,
    ],
    version = 12,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao
    abstract fun goalDao(): GoalDao
    abstract fun personalProjectDao(): PersonalProjectDao
    abstract fun finnishPracticeDao(): FinnishPracticeDao
    abstract fun weeklyPlanDao(): WeeklyPlanDao
    abstract fun courseDao(): CourseDao
    abstract fun timeMarkerDao(): TimeMarkerDao
    abstract fun gymDao(): GymDao
    abstract fun guitarDao(): GuitarDao
    abstract fun reviewDao(): ReviewDao
    abstract fun journalDao(): JournalDao
    abstract fun financeDao(): FinanceDao
    abstract fun moodDao(): MoodDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context, AppDatabase::class.java, "dorom.db")
                .fallbackToDestructiveMigration(true)
                .build()
                .also { instance = it }
        }
    }
}
