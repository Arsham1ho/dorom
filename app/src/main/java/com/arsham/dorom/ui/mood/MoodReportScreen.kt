package com.arsham.dorom.ui.mood

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
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.MoodEntry
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.BarChart
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.theme.DataText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoodReportScreen() {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val entries by container.moodRepository.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())

    val grouped = entries.groupBy { dateKey(it.timestampEpochMillis) }
    val counts = MOOD_OPTIONS.map { emoji -> entries.count { it.emoji == emoji }.toFloat() }
    val topMood = MOOD_OPTIONS.zip(counts).filter { it.second > 0 }.maxByOrNull { it.second }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Mood", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(16.dp, 20.dp, 16.dp, 8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("How are you feeling right now?", style = MaterialTheme.typography.titleMedium)
                        MoodPickerRow(
                            modifier = Modifier.padding(top = 14.dp),
                            onLog = { emoji, note -> scope.launch { container.moodRepository.logMood(emoji, note) } },
                        )
                    }
                }
            }

            if (entries.isNotEmpty()) {
                item {
                    DoromCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            SectionHeader(title = "Mood breakdown") {
                                Text("${entries.size} logged", style = DataText.small, color = MaterialTheme.colorScheme.primary)
                            }
                            BarChart(values = counts, labels = MOOD_OPTIONS)
                            topMood?.let { (emoji, count) ->
                                val n = count.toInt()
                                Text(
                                    "Most common: $emoji · logged $n ${if (n == 1) "time" else "times"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }

                item { SectionHeader(title = "History") }
                grouped.forEach { (date, dayEntries) ->
                    item {
                        Text(
                            date,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                        )
                    }
                    items(dayEntries) { entry -> MoodEntryRow(entry) }
                }
            } else {
                item {
                    EmptyState(
                        icon = Icons.Filled.Mood,
                        title = "No moods logged yet",
                        subtitle = "Tap an emoji above whenever you want to log how you're feeling.",
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodEntryRow(entry: MoodEntry) {
    DoromCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(entry.emoji, style = MaterialTheme.typography.headlineMedium)
            Column {
                Text(timeKey(entry.timestampEpochMillis), style = DataText.medium)
                if (entry.note.isNotBlank()) {
                    Text(
                        entry.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun dateKey(epochMillis: Long): String =
    SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH).format(Date(epochMillis))

private fun timeKey(epochMillis: Long): String =
    SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Date(epochMillis))
