package com.arsham.dorom.data.repository

import android.content.Context
import com.arsham.dorom.data.dao.ReviewDao
import com.arsham.dorom.data.entity.DailyReview
import kotlinx.coroutines.flow.Flow
import java.io.File

class ReviewRepository(
    private val dao: ReviewDao,
    private val context: Context,
) {
    fun observeReview(date: String): Flow<DailyReview?> = dao.observeReview(date)
    suspend fun getReview(date: String): DailyReview? = dao.getReview(date)
    fun observeAllReviews(): Flow<List<DailyReview>> = dao.observeAllReviews()
    fun observeRecentReviews(limit: Int): Flow<List<DailyReview>> = dao.observeRecentReviews(limit)

    private fun blank(date: String) = DailyReview(
        date = date, plannedCount = 0, completedCount = 0, percentage = 0, score = 0,
    )

    suspend fun recordActualWakeTime(date: String, time: String) {
        val current = dao.getReview(date) ?: blank(date)
        dao.upsertReview(current.copy(actualWakeTime = time))
    }

    suspend fun recordActualSleepTime(date: String, time: String) {
        val current = dao.getReview(date) ?: blank(date)
        dao.upsertReview(current.copy(actualSleepTime = time))
    }

    suspend fun saveFeedback(date: String, text: String, audioFile: File?) {
        val current = dao.getReview(date) ?: blank(date)
        dao.upsertReview(
            current.copy(
                feedbackText = text,
                feedbackAudioPath = audioFile?.absolutePath ?: current.feedbackAudioPath,
            )
        )
    }

    fun newFeedbackAudioFile(date: String): File {
        val dir = File(context.filesDir, "feedback_audio").apply { mkdirs() }
        return File(dir, "feedback_$date.m4a")
    }
}
