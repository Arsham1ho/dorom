package com.arsham.dorom.notifications.receivers

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.arsham.dorom.MainActivity
import com.arsham.dorom.R
import com.arsham.dorom.data.db.AppDatabase
import com.arsham.dorom.data.entity.DailyReview
import com.arsham.dorom.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class EndOfDayReviewReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val date = intent.getStringExtra(NotificationHelper.EXTRA_DATE) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                finalizeReview(context, date)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun finalizeReview(context: Context, date: String) {
        val db = AppDatabase.get(context)
        val tasks = db.planDao().getTasks(date)
        val planned = tasks.size
        val completed = tasks.count { it.isDone }
        val percentage = if (planned == 0) 0 else ((completed.toFloat() / planned) * 100).roundToInt()
        val existing = db.reviewDao().getReview(date)

        db.reviewDao().upsertReview(
            DailyReview(
                date = date,
                plannedCount = planned,
                completedCount = completed,
                percentage = percentage,
                score = percentage,
                feedbackText = existing?.feedbackText ?: "",
                feedbackAudioPath = existing?.feedbackAudioPath,
                actualWakeTime = existing?.actualWakeTime,
                actualSleepTime = existing?.actualSleepTime,
                isFinalized = true,
            )
        )

        postSummaryNotification(context, date, completed, planned, percentage)
    }

    private fun postSummaryNotification(context: Context, date: String, completed: Int, planned: Int, percentage: Int) {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_route", "history")
        }
        val pi = PendingIntent.getActivity(
            context, NotificationHelper.REQUEST_END_OF_DAY, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_REVIEW)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Today's score: $percentage")
            .setContentText("$completed of $planned tasks done. Tap to add feedback.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        NotificationManagerCompat.from(context).notify(NotificationHelper.REQUEST_END_OF_DAY, notification)
    }
}
