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
import com.arsham.dorom.notifications.NotificationHelper

private fun postCheckInNotification(
    context: Context,
    requestCode: Int,
    title: String,
    text: String,
    route: String,
) {
    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

    val contentIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("open_route", route)
    }
    val pi = PendingIntent.getActivity(
        context, requestCode, contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_CHECKINS)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pi)
        .build()
    NotificationManagerCompat.from(context).notify(requestCode, notification)
}

class WakeCheckInReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        postCheckInNotification(
            context,
            NotificationHelper.REQUEST_WAKE,
            "Good morning",
            "Awake? Tap to log your wake-up time and see today's plan.",
            "review",
        )
    }
}

class SleepCheckInReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        postCheckInNotification(
            context,
            NotificationHelper.REQUEST_SLEEP,
            "Heading to bed?",
            "Tap to log your actual bedtime before you sleep.",
            "review",
        )
    }
}
