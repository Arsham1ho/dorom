package com.arsham.dorom.ui.timemarkers

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timelapse
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.TimeDirection
import com.arsham.dorom.data.entity.TimeMarker
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DateField
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.Tag
import com.arsham.dorom.ui.components.TimeField
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.BadgeGold
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.DangerRed
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.doromClickable
import com.arsham.dorom.ui.theme.rememberPulse
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun TimeMarkersScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val markers by container.timeMarkerRepository.observeMarkers().collectAsStateWithLifecycle(initialValue = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Time Since", onBack = onBack) {
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Add marker") }
        }

        if (markers.isEmpty()) {
            EmptyState(icon = Icons.Filled.Timelapse, title = "No markers yet", subtitle = "Track time left until something, or time since something happened.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(markers) { marker ->
                MarkerCard(marker, onDelete = { scope.launch { container.timeMarkerRepository.deleteMarker(marker) } })
            }
        }
    }

    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            MarkerEditor(
                onSave = { marker -> scope.launch { container.timeMarkerRepository.upsertMarker(marker) }; showAdd = false },
                onCancel = { showAdd = false },
            )
        }
    }
}

@Composable
private fun MarkerEditor(onSave: (TimeMarker) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(TimeDirection.COUNTDOWN) }
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now().plusDays(7)) }
    var time by remember { mutableStateOf("12:00") }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Add a marker", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape, value = title, onValueChange = { title = it }, label = { Text("What is it?") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(text = "Countdown", filled = direction == TimeDirection.COUNTDOWN, modifier = Modifier.doromClickable { direction = TimeDirection.COUNTDOWN })
                Tag(text = "Count-up", filled = direction == TimeDirection.COUNTUP, modifier = Modifier.doromClickable { direction = TimeDirection.COUNTUP })
            }
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DateField(label = "Date", date = date, onDateChange = { date = it })
                TimeField(label = "Time", time = time, onTimeChange = { time = it })
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(onClick = {
                    val d = date ?: return@Button
                    if (title.isBlank()) return@Button
                    val (h, m) = time.split(":").map { it.toInt() }
                    val millis = d.atTime(LocalTime.of(h, m)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onSave(TimeMarker(title = title.trim(), targetEpochMillis = millis, direction = direction))
                }) { Text("Save") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun MarkerCard(marker: TimeMarker, onDelete: () -> Unit) {
    val pulse = rememberPulse()
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                IconBadge(
                    icon = if (marker.direction == TimeDirection.COUNTDOWN) Icons.Filled.Timelapse else Icons.Filled.History,
                    tint = BadgeGold,
                )
                Column {
                    Text(marker.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        elapsedOrRemainingLabel(marker),
                        style = DataText.large,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.graphicsLayer { alpha = 0.7f + 0.3f * pulse },
                    )
                }
            }
        }
    }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false; scope.launch { dismissState.reset() } },
            title = { Text("Remove marker?") },
            text = { Text("Remove \"${marker.title}\"?") },
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

private fun elapsedOrRemainingLabel(marker: TimeMarker): String {
    val now = System.currentTimeMillis()
    val diffMillis = marker.targetEpochMillis - now
    val future = diffMillis > 0
    val duration = Duration.ofMillis(kotlin.math.abs(diffMillis))
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val core = if (days > 0) "${days}d ${hours}h" else "${duration.toHours()}h ${duration.toMinutes() % 60}m"
    return when (marker.direction) {
        TimeDirection.COUNTDOWN -> if (future) "$core left" else "Time since ${marker.title}: $core"
        TimeDirection.COUNTUP -> if (future) "starts in $core" else "Time since ${marker.title}: $core"
    }
}
