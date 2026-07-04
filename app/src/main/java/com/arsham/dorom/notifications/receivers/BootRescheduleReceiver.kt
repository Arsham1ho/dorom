package com.arsham.dorom.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arsham.dorom.data.db.AppDatabase
import com.arsham.dorom.notifications.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Re-arms today's remaining alarms after a device reboot clears AlarmManager state. */
class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reschedule(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun reschedule(context: Context) {
        val db = AppDatabase.get(context)
        val scheduler = AlarmScheduler(context)
        val today = LocalDate.now().toString()

        val plan = db.planDao().getPlan(today) ?: return
        scheduler.scheduleWakeCheckIn(today, plan.wakeTargetTime)
        scheduler.scheduleSleepCheckIn(today, plan.bedTargetTime)
        scheduler.scheduleEndOfDayReview(today, plan.bedTargetTime)

        db.planDao().getTasks(today)
            .filter { !it.isDone }
            .forEach { scheduler.scheduleTaskReminder(today, it) }
    }
}
