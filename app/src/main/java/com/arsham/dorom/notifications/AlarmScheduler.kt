package com.arsham.dorom.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.arsham.dorom.data.entity.PlanTask
import com.arsham.dorom.notifications.receivers.EndOfDayReviewReceiver
import com.arsham.dorom.notifications.receivers.SleepCheckInReceiver
import com.arsham.dorom.notifications.receivers.TaskReminderReceiver
import com.arsham.dorom.notifications.receivers.WakeCheckInReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/** Centralizes scheduling/cancelling every exact alarm the app posts. */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    private fun epochMillisFor(date: String, time: String): Long {
        val (h, m) = time.split(":").map { it.toInt() }
        val localDate = LocalDate.parse(date)
        return LocalDateTime.of(localDate, LocalTime.of(h, m))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun schedule(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun scheduleTaskReminder(date: String, task: PlanTask) {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_TASK_ID, task.id)
            putExtra(NotificationHelper.EXTRA_TASK_TITLE, task.title)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            (NotificationHelper.REQUEST_TASK_BASE + task.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        schedule(epochMillisFor(date, task.startTime), pi)
    }

    fun cancelTaskReminder(taskId: Long) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            (NotificationHelper.REQUEST_TASK_BASE + taskId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pi)
    }

    fun scheduleWakeCheckIn(date: String, wakeTime: String) {
        val intent = Intent(context, WakeCheckInReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_DATE, date)
        }
        val pi = PendingIntent.getBroadcast(
            context, NotificationHelper.REQUEST_WAKE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        schedule(epochMillisFor(date, wakeTime), pi)
    }

    fun scheduleSleepCheckIn(date: String, bedTime: String) {
        val intent = Intent(context, SleepCheckInReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_DATE, date)
        }
        val pi = PendingIntent.getBroadcast(
            context, NotificationHelper.REQUEST_SLEEP, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        schedule(epochMillisFor(date, bedTime), pi)
    }

    /** Fires 30 minutes after bedtime to compute the end-of-day review. */
    fun scheduleEndOfDayReview(date: String, bedTime: String) {
        val intent = Intent(context, EndOfDayReviewReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_DATE, date)
        }
        val pi = PendingIntent.getBroadcast(
            context, NotificationHelper.REQUEST_END_OF_DAY, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        schedule(epochMillisFor(date, bedTime) + 30 * 60 * 1000L, pi)
    }
}
