package com.arsham.dorom.data.repository

import com.arsham.dorom.data.dao.MoodDao
import com.arsham.dorom.data.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

class MoodRepository(private val dao: MoodDao) {
    fun observeAll(): Flow<List<MoodEntry>> = dao.observeAll()
    fun observeSince(sinceEpochMillis: Long): Flow<List<MoodEntry>> = dao.observeSince(sinceEpochMillis)

    suspend fun logMood(emoji: String, note: String = "") {
        dao.insert(MoodEntry(emoji = emoji, timestampEpochMillis = System.currentTimeMillis(), note = note.trim()))
    }

    suspend fun delete(entry: MoodEntry) = dao.delete(entry)
}
