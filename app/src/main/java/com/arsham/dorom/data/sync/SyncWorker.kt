package com.arsham.dorom.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arsham.dorom.DoromApp

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            (applicationContext as DoromApp).container.syncEngine.syncAll()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
