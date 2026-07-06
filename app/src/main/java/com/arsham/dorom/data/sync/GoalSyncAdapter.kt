package com.arsham.dorom.data.sync

import com.arsham.dorom.data.dao.GoalDao
import com.arsham.dorom.data.entity.LongTermGoal
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class RemoteGoal(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("deadline_date") val deadlineDate: String? = null,
    val tag: String? = null,
    @SerialName("progress_percent") val progressPercent: Int = 0,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("created_at_epoch_millis") val createdAtEpochMillis: Long,
    @SerialName("updated_at_epoch_millis") val updatedAtEpochMillis: Long,
    @SerialName("deleted_at_epoch_millis") val deletedAtEpochMillis: Long? = null,
)

// The cover photo is a local file path — meaningless on another device until Phase 2 (media
// sync) uploads it to Supabase Storage, so it's deliberately left out of the remote row.
private fun LongTermGoal.toRemote() = RemoteGoal(
    id = id,
    title = title,
    description = description,
    startDate = startDate,
    deadlineDate = deadlineDate,
    tag = tag,
    progressPercent = progressPercent,
    isArchived = isArchived,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    deletedAtEpochMillis = deletedAtEpochMillis,
)

private fun RemoteGoal.toLocal(existingImagePath: String?) = LongTermGoal(
    id = id,
    title = title,
    description = description,
    startDate = startDate,
    deadlineDate = deadlineDate,
    tag = tag,
    imagePath = existingImagePath,
    progressPercent = progressPercent,
    isArchived = isArchived,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    deletedAtEpochMillis = deletedAtEpochMillis,
    pendingSync = false,
)

class GoalSyncAdapter(private val dao: GoalDao) : EntitySyncAdapter {
    override val tableName = "long_term_goals"

    override suspend fun push(client: SupabaseClient) {
        val pending = dao.getPendingSync()
        if (pending.isEmpty()) return
        client.postgrest[tableName].upsert(pending.map { it.toRemote() })
        pending.forEach { dao.markSynced(it.id) }
    }

    override suspend fun pull(client: SupabaseClient, sinceEpochMillis: Long): Long {
        val remoteRows = client.postgrest[tableName].select {
            filter { gt("updated_at_epoch_millis", sinceEpochMillis) }
        }.decodeList<RemoteGoal>()

        var maxSeen = sinceEpochMillis
        for (remote in remoteRows) {
            if (remote.deletedAtEpochMillis != null) {
                dao.hardDeleteById(remote.id)
            } else {
                val local = dao.getById(remote.id)
                // A local edit that hasn't pushed yet wins for now — the next push sends it, and
                // this remote version will come back around on a later pull once it's stale.
                if (local == null || !local.pendingSync) {
                    dao.upsertGoal(remote.toLocal(existingImagePath = local?.imagePath))
                }
            }
            if (remote.updatedAtEpochMillis > maxSeen) maxSeen = remote.updatedAtEpochMillis
        }
        return maxSeen
    }
}
