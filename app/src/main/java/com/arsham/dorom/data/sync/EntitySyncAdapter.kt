package com.arsham.dorom.data.sync

import io.github.jan.supabase.SupabaseClient

/**
 * One adapter per synced entity. `push` sends locally-dirty rows; `pull` fetches remote rows
 * updated since the last cursor and returns the new cursor value (the caller persists it).
 */
interface EntitySyncAdapter {
    val tableName: String
    suspend fun push(client: SupabaseClient)
    suspend fun pull(client: SupabaseClient, sinceEpochMillis: Long): Long
}
