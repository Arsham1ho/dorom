package com.arsham.dorom.data.repository

import android.content.Context
import android.net.Uri
import com.arsham.dorom.data.dao.GoalDao
import com.arsham.dorom.data.dao.WeeklyPlanDao
import com.arsham.dorom.data.entity.LongTermGoal
import com.arsham.dorom.data.entity.WeeklyPlan
import com.arsham.dorom.data.entity.WeeklyPlanItem
import kotlinx.coroutines.flow.Flow
import java.io.File

class GoalsRepository(private val dao: GoalDao, private val context: Context) {
    fun observeGoals(): Flow<List<LongTermGoal>> = dao.observeGoals()

    // Every local write marks the row dirty (updatedAt + pendingSync) so the sync engine knows to
    // push it next round — see data/sync/GoalSyncAdapter.kt.
    suspend fun upsertGoal(goal: LongTermGoal) =
        dao.upsertGoal(goal.copy(updatedAtEpochMillis = System.currentTimeMillis(), pendingSync = true))

    suspend fun saveGoal(goal: LongTermGoal, imageUri: Uri?) {
        val imagePath = if (imageUri != null) {
            goal.imagePath?.let { File(it).delete() }
            copyImage(imageUri)
        } else {
            goal.imagePath
        }
        upsertGoal(goal.copy(imagePath = imagePath))
    }

    // Soft-delete: keep the row (as a tombstone) until the sync engine confirms the deletion
    // reached the server, otherwise a device that's offline right now would never learn the goal
    // was removed. hardDeleteLocally() below is what the sync adapter calls once that's done.
    suspend fun deleteGoal(goal: LongTermGoal) {
        goal.imagePath?.let { File(it).delete() }
        dao.upsertGoal(goal.copy(deletedAtEpochMillis = System.currentTimeMillis(), pendingSync = true))
    }

    private fun copyImage(uri: Uri): String? {
        val dir = File(context.filesDir, "goal_images").apply { mkdirs() }
        val dest = File(dir, "goal_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        return dest.absolutePath
    }
}

class WeeklyPlanRepository(private val dao: WeeklyPlanDao) {
    fun observePlan(weekStartDate: String): Flow<WeeklyPlan?> = dao.observeWeeklyPlan(weekStartDate)
    fun observeItems(weekStartDate: String): Flow<List<WeeklyPlanItem>> = dao.observeItems(weekStartDate)
    suspend fun upsertPlan(plan: WeeklyPlan) = dao.upsertWeeklyPlan(plan)
    suspend fun upsertItem(item: WeeklyPlanItem) = dao.upsertItem(item)
    suspend fun toggleItem(item: WeeklyPlanItem) = dao.updateItem(item.copy(isDone = !item.isDone))
    suspend fun deleteItem(item: WeeklyPlanItem) = dao.deleteItem(item)
}
