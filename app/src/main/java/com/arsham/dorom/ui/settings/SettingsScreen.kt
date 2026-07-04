package com.arsham.dorom.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.settings.DoromSettings
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.TimeField
import com.arsham.dorom.ui.components.TopBarWithBack
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = DoromSettings())

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Settings", onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Default times", style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            TimeField(
                                label = "Wake up",
                                time = settings.defaultWakeTime,
                                onTimeChange = { scope.launch { container.settingsRepository.setDefaultWakeTime(it) } },
                            )
                            TimeField(
                                label = "Bedtime",
                                time = settings.defaultBedTime,
                                onTimeChange = { scope.launch { container.settingsRepository.setDefaultBedTime(it) } },
                            )
                        }
                    }
                }
            }
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Notifications", style = MaterialTheme.typography.titleMedium)
                            Text("Task reminders, check-ins, end-of-day review", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { scope.launch { container.settingsRepository.setNotificationsEnabled(it) } },
                        )
                    }
                }
            }
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Lock personal journal", style = MaterialTheme.typography.titleMedium)
                            Text("Require fingerprint / device PIN to open it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.biometricLockEnabled,
                            onCheckedChange = { scope.launch { container.settingsRepository.setBiometricLockEnabled(it) } },
                        )
                    }
                }
            }
        }
    }
}
