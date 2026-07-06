package com.arsham.dorom

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.arsham.dorom.data.db.AppDatabase
import com.arsham.dorom.data.remote.AuthRepository
import com.arsham.dorom.data.remote.SupabaseModule
import com.arsham.dorom.data.repository.CourseRepository
import com.arsham.dorom.data.repository.FinanceRepository
import com.arsham.dorom.data.repository.GoalsRepository
import com.arsham.dorom.data.repository.GuitarRepository
import com.arsham.dorom.data.repository.GymRepository
import com.arsham.dorom.data.repository.FinnishPracticeRepository
import com.arsham.dorom.data.repository.JournalRepository
import com.arsham.dorom.data.repository.MoodRepository
import com.arsham.dorom.data.repository.PersonalProjectRepository
import com.arsham.dorom.data.repository.PlanRepository
import com.arsham.dorom.data.repository.ReviewRepository
import com.arsham.dorom.data.repository.TimeMarkerRepository
import com.arsham.dorom.data.repository.WeeklyPlanRepository
import com.arsham.dorom.data.settings.SettingsRepository
import com.arsham.dorom.data.sync.GoalSyncAdapter
import com.arsham.dorom.data.sync.SyncCursorStore
import com.arsham.dorom.data.sync.SyncEngine
import com.arsham.dorom.data.sync.SyncWorker
import com.arsham.dorom.notifications.AlarmScheduler
import com.arsham.dorom.notifications.NotificationHelper
import java.util.concurrent.TimeUnit

/** Simple hand-rolled service locator — this is a single-user app, no DI framework needed. */
class DoromApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        container = AppContainer(this)

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dorom-sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest,
        )
    }
}

class AppContainer(app: Application) {
    private val db = AppDatabase.get(app)
    val alarmScheduler = AlarmScheduler(app)
    val settingsRepository = SettingsRepository(app)

    val planRepository = PlanRepository(db.planDao(), alarmScheduler)
    val goalsRepository = GoalsRepository(db.goalDao(), app)
    val personalProjectRepository = PersonalProjectRepository(db.personalProjectDao())
    val finnishPracticeRepository = FinnishPracticeRepository(db.finnishPracticeDao())
    val weeklyPlanRepository = WeeklyPlanRepository(db.weeklyPlanDao())
    val courseRepository = CourseRepository(db.courseDao())
    val timeMarkerRepository = TimeMarkerRepository(db.timeMarkerDao())
    val gymRepository = GymRepository(db.gymDao(), app)
    val guitarRepository = GuitarRepository(db.guitarDao(), app)
    val reviewRepository = ReviewRepository(db.reviewDao(), app)
    val journalRepository = JournalRepository(db.journalDao(), app)
    val financeRepository = FinanceRepository(db.financeDao())
    val moodRepository = MoodRepository(db.moodDao())

    val supabaseClient = SupabaseModule.client
    val authRepository = AuthRepository(supabaseClient)
    private val syncCursorStore = SyncCursorStore(app)
    val syncEngine = SyncEngine(
        client = supabaseClient,
        authRepository = authRepository,
        cursorStore = syncCursorStore,
        adapters = listOf(GoalSyncAdapter(db.goalDao())),
    )
}
