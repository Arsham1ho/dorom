package com.arsham.dorom.data.repository

import android.content.Context
import com.arsham.dorom.data.dao.JournalDao
import com.arsham.dorom.data.entity.JournalEntry
import com.arsham.dorom.security.JournalCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

/** Decrypted, in-memory-only view of a journal entry. The text is never persisted in plain text;
 * audio/video attachments are local files, same as everywhere else media is recorded in this app —
 * the lock screen (biometric) is the privacy boundary for those, not per-file encryption. */
data class JournalEntryPlain(
    val id: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val text: String,
    val audioPath: String? = null,
    val videoPath: String? = null,
)

class JournalRepository(private val dao: JournalDao, private val context: Context) {

    fun observeEntries(): Flow<List<JournalEntryPlain>> = dao.observeEntries().map { entries ->
        entries.map {
            JournalEntryPlain(
                id = it.id,
                createdAtEpochMillis = it.createdAtEpochMillis,
                updatedAtEpochMillis = it.updatedAtEpochMillis,
                text = runCatching { JournalCrypto.decrypt(it.encryptedContent) }.getOrDefault(""),
                audioPath = it.audioPath,
                videoPath = it.videoPath,
            )
        }
    }

    suspend fun saveEntry(
        id: Long?,
        text: String,
        audioPath: String? = null,
        videoPath: String? = null,
        createdAtEpochMillis: Long? = null,
    ) {
        val now = System.currentTimeMillis()
        dao.upsertEntry(
            JournalEntry(
                id = id ?: 0,
                createdAtEpochMillis = createdAtEpochMillis ?: now,
                updatedAtEpochMillis = now,
                encryptedContent = JournalCrypto.encrypt(text),
                audioPath = audioPath,
                videoPath = videoPath,
            )
        )
    }

    suspend fun deleteEntry(entry: JournalEntryPlain) {
        entry.audioPath?.let { File(it).delete() }
        entry.videoPath?.let { File(it).delete() }
        dao.deleteEntry(
            JournalEntry(
                id = entry.id,
                createdAtEpochMillis = entry.createdAtEpochMillis,
                updatedAtEpochMillis = entry.updatedAtEpochMillis,
                encryptedContent = "",
            )
        )
    }

    fun newAudioFile(): File {
        val dir = File(context.filesDir, "journal_audio").apply { mkdirs() }
        return File(dir, "journal_${System.currentTimeMillis()}.m4a")
    }

    fun newVideoFile(): File {
        val dir = File(context.filesDir, "journal_video").apply { mkdirs() }
        return File(dir, "journal_${System.currentTimeMillis()}.mp4")
    }
}
