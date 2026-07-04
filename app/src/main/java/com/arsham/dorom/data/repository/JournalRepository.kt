package com.arsham.dorom.data.repository

import com.arsham.dorom.data.dao.JournalDao
import com.arsham.dorom.data.entity.JournalEntry
import com.arsham.dorom.security.JournalCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Decrypted, in-memory-only view of a journal entry. Never persisted in plain text. */
data class JournalEntryPlain(val id: Long, val createdAtEpochMillis: Long, val updatedAtEpochMillis: Long, val text: String)

class JournalRepository(private val dao: JournalDao) {

    fun observeEntries(): Flow<List<JournalEntryPlain>> = dao.observeEntries().map { entries ->
        entries.map {
            JournalEntryPlain(
                id = it.id,
                createdAtEpochMillis = it.createdAtEpochMillis,
                updatedAtEpochMillis = it.updatedAtEpochMillis,
                text = runCatching { JournalCrypto.decrypt(it.encryptedContent) }.getOrDefault(""),
            )
        }
    }

    suspend fun saveEntry(id: Long?, text: String, createdAtEpochMillis: Long? = null) {
        val now = System.currentTimeMillis()
        dao.upsertEntry(
            JournalEntry(
                id = id ?: 0,
                createdAtEpochMillis = createdAtEpochMillis ?: now,
                updatedAtEpochMillis = now,
                encryptedContent = JournalCrypto.encrypt(text),
            )
        )
    }

    suspend fun deleteEntry(entry: JournalEntryPlain) {
        dao.deleteEntry(
            JournalEntry(
                id = entry.id,
                createdAtEpochMillis = entry.createdAtEpochMillis,
                updatedAtEpochMillis = entry.updatedAtEpochMillis,
                encryptedContent = "",
            )
        )
    }
}
