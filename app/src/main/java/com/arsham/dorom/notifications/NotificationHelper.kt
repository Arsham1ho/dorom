package com.arsham.dorom.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_TASKS = "dorom_tasks"
    const val CHANNEL_CHECKINS = "dorom_checkins"
    const val CHANNEL_REVIEW = "dorom_review"

    const val REQUEST_TASK_BASE = 10_000
    const val REQUEST_WAKE = 1
    const val REQUEST_SLEEP = 2
    const val REQUEST_END_OF_DAY = 3

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_DATE = "extra_date"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_TASKS, "Task reminders", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_CHECKINS, "Wake & sleep check-ins", NotificationManager.IMPORTANCE_HIGH)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REVIEW, "End-of-day review", NotificationManager.IMPORTANCE_HIGH)
        )
    }
}
