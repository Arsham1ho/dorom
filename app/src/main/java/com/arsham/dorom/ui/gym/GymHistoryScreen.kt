package com.arsham.dorom.ui.gym

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.data.entity.GymSession
import com.arsham.dorom.data.entity.GymSessionSet
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.BarChart
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.Tag
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.BadgeBlue
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.doromClickable
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private enum class HistoryRange(val label: String) { WEEK("7d"), MONTH("30d"), QUARTER("3m"), YEAR("1y"), ALL("All") }

private fun sessionDate(session: GymSession): LocalDate = runCatching { LocalDate.parse(session.date) }.getOrDefault(LocalDate.now())

private fun sessionMinutes(session: GymSession): Long {
    val end = session.endEpochMillis ?: session.startEpochMillis
    return ((end - session.startEpochMillis) / 60000).coerceAtLeast(0)
}

/** Buckets finished sessions into a small number of bars sized to the selected range. */
private fun bucketSessions(sessions: List<GymSession>, range: HistoryRange, today: LocalDate): Pair<List<Float>, List<String>> {
    return when (range) {
        HistoryRange.WEEK -> {
            val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
            val values = days.map { d -> sessions.filter { sessionDate(it) == d }.sumOf { sessionMinutes(it) }.toFloat() }
            val labels = days.map { it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(1) }
            values to labels
        }
        HistoryRange.MONTH -> {
            val days = (29 downTo 0).map { today.minusDays(it.toLong()) }
            val values = days.map { d -> sessions.filter { sessionDate(it) == d }.sumOf { sessionMinutes(it) }.toFloat() }
            val labels = days.mapIndexed { i, d -> if (i % 5 == 0) d.dayOfMonth.toString() else "" }
            values to labels
        }
        HistoryRange.QUARTER -> {
            val weekStarts = (11 downTo 0).map { today.minusWeeks(it.toLong()).let { d -> d.minusDays((d.dayOfWeek.value - 1).toLong()) } }
            val values = weekStarts.map { start ->
                val end = start.plusDays(6)
                sessions.filter { val d = sessionDate(it); !d.isBefore(start) && !d.isAfter(end) }.sumOf { sessionMinutes(it) }.toFloat()
            }
            val labels = weekStarts.mapIndexed { i, d -> if (i % 2 == 0) d.format(DateTimeFormatter.ofPattern("d MMM")) else "" }
            values to labels
        }
        HistoryRange.YEAR -> {
            val months = (11 downTo 0).map { YearMonth.from(today).minusMonths(it.toLong()) }
            val values = months.map { m -> sessions.filter { YearMonth.from(sessionDate(it)) == m }.sumOf { sessionMinutes(it) }.toFloat() }
            val labels = months.map { it.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(1) }
            values to labels
        }
        HistoryRange.ALL -> {
            val earliest = sessions.minOfOrNull { YearMonth.from(sessionDate(it)) } ?: YearMonth.from(today)
            val span = java.time.temporal.ChronoUnit.MONTHS.between(earliest, YearMonth.from(today)).toInt().coerceIn(0, 35)
            val months = (span downTo 0).map { YearMonth.from(today).minusMonths(it.toLong()) }
            val values = months.map { m -> sessions.filter { YearMonth.from(sessionDate(it)) == m }.sumOf { sessionMinutes(it) }.toFloat() }
            val labels = months.mapIndexed { i, m -> if (i % 3 == 0) m.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(1) else "" }
            values to labels
        }
    }
}

@Composable
fun GymHistoryScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val sessions by container.gymRepository.observeSessions().collectAsStateWithLifecycle(initialValue = emptyList())
    val finished = remember(sessions) { sessions.filter { it.endEpochMillis != null } }
    var range by remember { mutableStateOf(HistoryRange.MONTH) }
    var detailsSession by remember { mutableStateOf<GymSession?>(null) }

    val today = remember { LocalDate.now() }
    val thisMonthCount = remember(finished) { finished.count { YearMonth.from(sessionDate(it)) == YearMonth.from(today) } }
    val totalMinutes = remember(finished) { finished.sumOf { sessionMinutes(it) } }
    val (values, labels) = remember(finished, range) { bucketSessions(finished, range, today) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Gym history", onBack = onBack)

        if (finished.isEmpty()) {
            EmptyState(icon = Icons.Filled.History, title = "No sessions yet", subtitle = "Finish a workout and it'll show up here.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (finished.isNotEmpty()) {
                item {
                    DoromCard(modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("$thisMonthCount", style = DataText.large, color = MaterialTheme.colorScheme.primary)
                                Text("workouts this month", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column {
                                Text(formatDuration(totalMinutes), style = DataText.large, color = MaterialTheme.colorScheme.primary)
                                Text("total time in the gym", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    Column {
                        SectionHeader(title = "Performance")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            HistoryRange.entries.forEach { r ->
                                Tag(text = r.label, filled = range == r, modifier = Modifier.doromClickable { range = r })
                            }
                        }
                        DoromCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    "Minutes trained per period",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                BarChart(values = values, labels = labels, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
                item { SectionHeader(title = "Sessions") }
            }
            items(finished.sortedByDescending { it.startEpochMillis }) { session ->
                SessionRow(session, onClick = { detailsSession = session })
            }
        }
    }

    detailsSession?.let { session ->
        Dialog(onDismissRequest = { detailsSession = null }) {
            SessionDetails(session = session, onDismiss = { detailsSession = null })
        }
    }
}

private fun formatDuration(totalMinutes: Long): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
private fun SessionRow(session: GymSession, onClick: () -> Unit) {
    val durationLabel = formatDuration(sessionMinutes(session))
    val locationLabel = if (session.location == GymLocation.GYM) "Gym" else "Home Gym"
    val timeLabel = remember(session.startEpochMillis) {
        Instant.ofEpochMilli(session.startEpochMillis).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    DoromCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBadge(icon = Icons.Filled.History, tint = BadgeBlue)
            Column {
                Text(sessionDate(session).toString(), style = MaterialTheme.typography.titleMedium)
                Text("$locationLabel · $timeLabel · $durationLabel", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SessionDetails(session: GymSession, onDismiss: () -> Unit) {
    val container = LocalAppContainer.current
    val sets by container.gymRepository.observeSets(session.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val byExercise = remember(sets) { sets.groupBy { it.exerciseName } }
    val startTime = remember(session.startEpochMillis) {
        Instant.ofEpochMilli(session.startEpochMillis).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    val endTime = remember(session.endEpochMillis) {
        session.endEpochMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) }
    }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(sessionDate(session).toString(), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            Text(
                "${if (session.location == GymLocation.GYM) "Gym" else "Home Gym"} · $startTime${if (endTime != null) "–$endTime" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (byExercise.isEmpty()) {
                Text(
                    "No sets logged for this session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp),
                )
            } else {
                byExercise.forEach { (name, exerciseSets) ->
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        Text(name, style = MaterialTheme.typography.titleSmall)
                        exerciseSets.sortedBy { it.setNumber }.forEach { s ->
                            Text(
                                "Set ${s.setNumber} · ${s.weight}kg × ${s.reps}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
