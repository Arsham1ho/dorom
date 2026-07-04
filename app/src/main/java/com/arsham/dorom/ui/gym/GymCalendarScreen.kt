package com.arsham.dorom.ui.gym

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.GYM_CATEGORIES
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DateField
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.LocalFileImage
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.Terracotta
import com.arsham.dorom.ui.theme.doromClickable
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun GymCalendarScreen(location: GymLocation, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf(LocalDate.now()) }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(date) {
        saved = false
        val scheduled = container.gymRepository.getScheduledExercises(date.toString())
        selectedIds.clear()
        selectedIds.addAll(scheduled.map { it.id })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Plan on calendar", onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Which day?", style = MaterialTheme.typography.titleMedium)
                        DateField(
                            label = "Date",
                            date = date,
                            onDateChange = { it?.let { picked -> date = picked } },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }

            item {
                Column {
                    SectionHeader(title = "Pick exercises for $date")
                    Text(
                        "Tap a card to add or remove it from this day's plan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(GYM_CATEGORIES) { category ->
                CategoryPickerRow(
                    location = location,
                    category = category,
                    selectedIds = selectedIds,
                    onToggle = { id -> if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id) },
                )
            }

            item {
                Column {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            scope.launch {
                                container.gymRepository.setSchedule(date.toString(), selectedIds.toList())
                                saved = true
                            }
                        },
                    ) { Text("Save plan for this day (${selectedIds.size} selected)") }
                    if (saved) {
                        Text(
                            "Saved ✓",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPickerRow(
    location: GymLocation,
    category: String,
    selectedIds: List<Long>,
    onToggle: (Long) -> Unit,
) {
    val container = LocalAppContainer.current
    val exercises by container.gymRepository.observeExercises(location, category).collectAsStateWithLifecycle(initialValue = emptyList())
    if (exercises.isEmpty()) return

    Column {
        Text(category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(exercises) { exercise ->
                SelectableExerciseCard(
                    exercise = exercise,
                    selected = selectedIds.contains(exercise.id),
                    onClick = { onToggle(exercise.id) },
                )
            }
        }
    }
}

@Composable
private fun SelectableExerciseCard(exercise: GymExercise, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.5.dp else 1.dp

    Column(modifier = Modifier.width(104.dp).doromClickable(onClick)) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(borderWidth, borderColor), CardShape),
        ) {
            if (exercise.imagePath != null) {
                LocalFileImage(path = exercise.imagePath, contentDescription = exercise.name, modifier = Modifier.size(104.dp).clip(CardShape))
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    IconBadge(icon = Icons.AutoMirrored.Filled.DirectionsRun, tint = Terracotta, size = 44.dp)
                }
            }
            if (selected) {
                SelectedBadge(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
            }
        }
        Text(
            exercise.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp).width(104.dp),
        )
    }
}

@Composable
private fun SelectedBadge(modifier: Modifier = Modifier, icon: ImageVector = Icons.Filled.Check) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(14.dp))
    }
}
