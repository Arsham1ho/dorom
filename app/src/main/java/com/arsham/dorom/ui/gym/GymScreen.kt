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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.WorkoutDay
import com.arsham.dorom.data.entity.WorkoutExercise
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.TopBarWithBack
import kotlinx.coroutines.launch

@Composable
fun GymScreen(onBack: () -> Unit, onOpenDay: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val days by container.workoutRepository.observeDays().collectAsStateWithLifecycle(initialValue = emptyList())
    var newLabel by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Gym plan", onBack = onBack)

        if (days.isEmpty()) {
            EmptyState(icon = Icons.Filled.FitnessCenter, title = "No workout days yet", subtitle = "Add a day (e.g. Push, Pull, Legs, Cardio) and fill in exercises.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(days) { day ->
                DoromCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenDay(day.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(day.label, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { scope.launch { container.workoutRepository.deleteDay(day) } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete day")
                        }
                    }
                }
            }
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newLabel, onValueChange = { newLabel = it }, label = { Text("New day (e.g. Push)") },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {
                            if (newLabel.isNotBlank()) {
                                scope.launch { container.workoutRepository.upsertDay(WorkoutDay(label = newLabel.trim(), orderIndex = days.size)) }
                                newLabel = ""
                            }
                        }) { Icon(Icons.Filled.Add, contentDescription = "Add day") }
                    }
                }
            }
        }
    }
}

@Composable
fun GymDayScreen(dayId: Long, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val exercises by container.workoutRepository.observeExercises(dayId).collectAsStateWithLifecycle(initialValue = emptyList())

    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Exercises", onBack = onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(exercises) { exercise ->
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                            Text("${exercise.sets} sets × ${exercise.reps} reps", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { scope.launch { container.workoutRepository.deleteExercise(exercise) } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete exercise")
                        }
                    }
                }
            }
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Exercise") }, modifier = Modifier.fillMaxWidth())
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = sets, onValueChange = { sets = it.filter(Char::isDigit) }, label = { Text("Sets") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = reps, onValueChange = { reps = it.filter(Char::isDigit) }, label = { Text("Reps") }, modifier = Modifier.weight(1f))
                        }
                        Button(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = {
                                if (name.isNotBlank()) {
                                    scope.launch {
                                        container.workoutRepository.upsertExercise(
                                            WorkoutExercise(
                                                workoutDayId = dayId, name = name.trim(),
                                                sets = sets.toIntOrNull() ?: 0, reps = reps.toIntOrNull() ?: 0,
                                                orderIndex = exercises.size,
                                            )
                                        )
                                    }
                                    name = ""
                                }
                            },
                        ) { Text("Add exercise") }
                    }
                }
            }
        }
    }
}
