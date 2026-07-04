package com.arsham.dorom.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dorom_settings")

data class DoromSettings(
    val defaultWakeTime: String = "07:00",
    val defaultBedTime: String = "23:00",
    val notificationsEnabled: Boolean = true,
    val biometricLockEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val WAKE = stringPreferencesKey("default_wake_time")
        val BED = stringPreferencesKey("default_bed_time")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val BIOMETRIC = booleanPreferencesKey("biometric_lock_enabled")
        val ONBOARDED = booleanPreferencesKey("has_completed_onboarding")
    }

    val settings: Flow<DoromSettings> = context.dataStore.data.map { prefs ->
        DoromSettings(
            defaultWakeTime = prefs[Keys.WAKE] ?: "07:00",
            defaultBedTime = prefs[Keys.BED] ?: "23:00",
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            biometricLockEnabled = prefs[Keys.BIOMETRIC] ?: true,
            hasCompletedOnboarding = prefs[Keys.ONBOARDED] ?: false,
        )
    }

    suspend fun setDefaultWakeTime(time: String) = context.dataStore.edit { it[Keys.WAKE] = time }
    suspend fun setDefaultBedTime(time: String) = context.dataStore.edit { it[Keys.BED] = time }
    suspend fun setNotificationsEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    suspend fun setBiometricLockEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.BIOMETRIC] = enabled }
    suspend fun setHasCompletedOnboarding(done: Boolean) = context.dataStore.edit { it[Keys.ONBOARDED] = done }
}
