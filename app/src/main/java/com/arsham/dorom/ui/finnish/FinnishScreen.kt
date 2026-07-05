package com.arsham.dorom.ui.finnish

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.FinnishPracticeEntry
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DateField
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.DangerRed
import com.arsham.dorom.ui.theme.DataText
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private fun computePracticeStreak(dates: Set<String>): Int {
    var streak = 0
    var day = LocalDate.now()
    while (dates.contains(day.toString())) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}

@Composable
fun FinnishScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val entries by container.finnishPracticeRepository.observeEntries().collectAsStateWithLifecycle(initialValue = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    val streak = remember(entries) { computePracticeStreak(entries.map { it.date }.toSet()) }
    val totalMinutes = remember(entries) { entries.sumOf { it.minutes } }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Finnish", onBack = onBack) {
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Log practice") }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("$streak", style = DataText.large, color = MaterialTheme.colorScheme.primary)
                            Text(if (streak == 1) "day streak" else "day streak", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column {
                            Text("$totalMinutes", style = DataText.large, color = MaterialTheme.colorScheme.primary)
                            Text("minutes total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Translate,
                        title = "No practice logged yet",
                        subtitle = "Log a few minutes of Finnish practice whenever you do it.",
                    )
                }
            }

            items(entries) { entry ->
                EntryRow(entry = entry, onDelete = { scope.launch { container.finnishPracticeRepository.deleteEntry(entry) } })
            }
        }
    }

    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            PracticeEditor(
                onSave = { date, minutes, notes ->
                    scope.launch {
                        container.finnishPracticeRepository.upsertEntry(
                            FinnishPracticeEntry(date = date.toString(), minutes = minutes, notes = notes, createdAtEpochMillis = System.currentTimeMillis())
                        )
                    }
                    showAdd = false
                },
                onCancel = { showAdd = false },
            )
        }
    }
}

@Composable
private fun PracticeEditor(onSave: (LocalDate, Int, String) -> Unit, onCancel: () -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var minutes by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Log practice", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            DateField(label = "Date", date = date, onDateChange = { if (it != null) date = it }, modifier = Modifier.padding(top = 8.dp))
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = minutes, onValueChange = { minutes = it.filter(Char::isDigit) }, label = { Text("Minutes practiced") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(onClick = {
                    val m = minutes.toIntOrNull() ?: 0
                    if (m > 0) onSave(date, m, notes.trim())
                }) { Text("Save") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EntryRow(entry: FinnishPracticeEntry, onDelete: () -> Unit) {
    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }
    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd) showConfirm = true
            false
        },
    )

    androidx.compose.material3.SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardShape)
                    .background(DangerRed)
                    .padding(horizontal = 20.dp),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
            }
        },
    ) {
        DoromCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (entry.notes.isNotBlank()) {
                        Text(entry.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("${entry.minutes} min", style = DataText.medium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false; scope.launch { dismissState.reset() } },
            title = { Text("Remove entry?") },
            text = { Text("Remove this practice log?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showConfirm = false
                    onDelete()
                }) { Text("Remove") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showConfirm = false; scope.launch { dismissState.reset() } }) { Text("Cancel") }
            },
        )
    }
}
