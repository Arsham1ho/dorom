package com.arsham.dorom.data.repository

import android.content.Context
import android.net.Uri
import com.arsham.dorom.data.dao.GymDao
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.data.entity.GymScheduleEntry
import com.arsham.dorom.data.entity.GymSession
import com.arsham.dorom.data.entity.GymSessionSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File

class GymRepository(
    private val dao: GymDao,
    private val context: Context,
) {
    fun observeExercises(location: GymLocation, category: String): Flow<List<GymExercise>> =
        dao.observeExercises(location, category)

    suspend fun addExercise(
        location: GymLocation,
        category: String,
        name: String,
        imageUri: Uri?,
        defaultSets: Int,
        defaultReps: Int,
        defaultWeight: Double,
    ) {
        val imagePath = imageUri?.let { copyImage(it) }
        dao.insertExercise(
            GymExercise(
                location = location,
                category = category,
                name = name,
                imagePath = imagePath,
                defaultSets = defaultSets,
                defaultReps = defaultReps,
                defaultWeight = defaultWeight,
                createdAtEpochMillis = System.currentTimeMillis(),
            )
        )
    }

    suspend fun updateExercise(
        exercise: GymExercise,
        name: String,
        imageUri: Uri?,
        defaultSets: Int,
        defaultReps: Int,
        defaultWeight: Double,
    ) {
        val newImagePath = if (imageUri != null) {
            exercise.imagePath?.let { File(it).delete() }
            copyImage(imageUri)
        } else {
            exercise.imagePath
        }
        dao.updateExercise(
            exercise.copy(
                name = name,
                imagePath = newImagePath,
                defaultSets = defaultSets,
                defaultReps = defaultReps,
                defaultWeight = defaultWeight,
            )
        )
    }

    suspend fun deleteExercise(exercise: GymExercise) {
        exercise.imagePath?.let { File(it).delete() }
        dao.deleteExercise(exercise)
    }

    private fun copyImage(uri: Uri): String? {
        val dir = File(context.filesDir, "gym_exercise_images").apply { mkdirs() }
        val dest = File(dir, "ex_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        return dest.absolutePath
    }

    fun observeSchedule(date: String): Flow<List<GymScheduleEntry>> = dao.observeSchedule(date)

    suspend fun getScheduledExercises(date: String): List<GymExercise> {
        val entries = dao.observeSchedule(date).first().sortedBy { it.orderIndex }
        if (entries.isEmpty()) return emptyList()
        val exercises = dao.getExercisesByIds(entries.map { it.exerciseId })
        val byId = exercises.associateBy { it.id }
        return entries.mapNotNull { byId[it.exerciseId] }
    }

    suspend fun setSchedule(date: String, exerciseIds: List<Long>) {
        dao.clearSchedule(date)
        exerciseIds.forEachIndexed { index, exerciseId ->
            dao.insertScheduleEntry(GymScheduleEntry(date = date, exerciseId = exerciseId, orderIndex = index))
        }
    }

    fun observeSessions(): Flow<List<GymSession>> = dao.observeSessions()

    suspend fun getActiveSessionForDate(date: String): GymSession? = dao.getActiveSessionForDate(date)

    suspend fun startSession(date: String, location: GymLocation): GymSession {
        val id = dao.upsertSession(GymSession(date = date, location = location, startEpochMillis = System.currentTimeMillis()))
        return GymSession(id = id, date = date, location = location, startEpochMillis = System.currentTimeMillis())
    }

    suspend fun finishSession(session: GymSession) {
        dao.upsertSession(session.copy(endEpochMillis = System.currentTimeMillis()))
    }

    fun observeSets(sessionId: Long): Flow<List<GymSessionSet>> = dao.observeSets(sessionId)

    suspend fun logSet(sessionId: Long, exerciseId: Long, exerciseName: String, setNumber: Int, weight: Double, reps: Int, restSeconds: Int) {
        dao.insertSet(
            GymSessionSet(
                sessionId = sessionId,
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                setNumber = setNumber,
                weight = weight,
                reps = reps,
                restSeconds = restSeconds,
                loggedAtEpochMillis = System.currentTimeMillis(),
            )
        )
    }
}
