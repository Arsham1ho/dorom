package com.arsham.dorom.data.sync

import com.arsham.dorom.data.remote.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus

/** Loops every registered [EntitySyncAdapter], push then pull. No-ops entirely when signed out. */
class SyncEngine(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository,
    private val cursorStore: SyncCursorStore,
    private val adapters: List<EntitySyncAdapter>,
) {
    suspend fun syncAll() {
        if (authRepository.sessionStatus.value !is SessionStatus.Authenticated) return
        for (adapter in adapters) {
            runCatching { adapter.push(client) }
            val since = cursorStore.get(adapter.tableName)
            val newCursor = runCatching { adapter.pull(client, since) }.getOrDefault(since)
            cursorStore.set(adapter.tableName, newCursor)
        }
    }
}
