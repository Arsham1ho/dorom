package com.arsham.dorom.data.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.syncDataStore by preferencesDataStore(name = "dorom_sync")

/** Per-table "last successfully pulled up to" timestamp, so pull() only asks for what changed. */
class SyncCursorStore(private val context: Context) {
    suspend fun get(table: String): Long =
        context.syncDataStore.data.first()[longPreferencesKey("cursor_$table")] ?: 0L

    suspend fun set(table: String, value: Long) {
        context.syncDataStore.edit { it[longPreferencesKey("cursor_$table")] = value }
    }
}
