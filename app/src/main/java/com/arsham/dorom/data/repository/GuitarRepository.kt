package com.arsham.dorom.data.repository

import android.content.Context
import android.net.Uri
import com.arsham.dorom.data.dao.GuitarDao
import com.arsham.dorom.data.entity.GuitarRecording
import com.arsham.dorom.data.entity.GuitarSong
import com.arsham.dorom.data.entity.GuitarTabImage
import kotlinx.coroutines.flow.Flow
import java.io.File

class GuitarRepository(
    private val dao: GuitarDao,
    private val context: Context,
) {
    fun observeSongs(): Flow<List<GuitarSong>> = dao.observeSongs()
    suspend fun upsertSong(song: GuitarSong) = dao.upsertSong(song)
    suspend fun deleteSong(song: GuitarSong) = dao.deleteSong(song)

    fun observeTabImages(songId: Long): Flow<List<GuitarTabImage>> = dao.observeTabImages(songId)
    fun observeRecordings(songId: Long): Flow<List<GuitarRecording>> = dao.observeRecordings(songId)

    /** Copies a picked image (e.g. from the Photo Picker) into private storage, then records it. */
    suspend fun importTabImage(songId: Long, sourceUri: Uri) {
        val dir = File(context.filesDir, "guitar_tabs").apply { mkdirs() }
        val dest = File(dir, "tab_${songId}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        dao.insertTabImage(GuitarTabImage(songId = songId, imagePath = dest.absolutePath))
    }

    suspend fun deleteTabImage(image: GuitarTabImage) {
        File(image.imagePath).delete()
        dao.deleteTabImage(image)
    }

    fun newRecordingFile(songId: Long): File {
        val dir = File(context.filesDir, "guitar_recordings").apply { mkdirs() }
        return File(dir, "rec_${songId}_${System.currentTimeMillis()}.m4a")
    }

    suspend fun saveRecording(songId: Long, file: File, note: String = "") {
        dao.insertRecording(
            GuitarRecording(
                songId = songId,
                audioPath = file.absolutePath,
                recordedAtEpochMillis = System.currentTimeMillis(),
                note = note,
            )
        )
    }

    suspend fun deleteRecording(recording: GuitarRecording) {
        File(recording.audioPath).delete()
        dao.deleteRecording(recording)
    }
}
