package com.arsham.dorom.ui.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.data.entity.GymSession
import com.arsham.dorom.data.entity.GymSessionSet
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.PulsingRecDot
import com.arsham.dorom.ui.navigation.Routes
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.ui.theme.Terracotta
import com.arsham.dorom.ui.theme.doromClickable
import com.arsham.dorom.util.todayString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun GymSessionScreen(location: GymLocation, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val today = remember { todayString() }

    var exercises by remember { mutableStateOf<List<GymExercise>>(emptyList()) }
    var session by remember { mutableStateOf<GymSession?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }
    var setNumber by remember { mutableStateOf(1) }
    var lastEventMillis by remember { mutableLongStateOf(0L) }
    var sessionElapsedSec by remember { mutableLongStateOf(0L) }

    val setsFlow = remember { MutableStateFlow<List<GymSessionSet>>(emptyList()) }
    val sets by setsFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        exercises = container.gymRepository.getScheduledExercises(today)
        val active = container.gymRepository.getActiveSessionForDate(today)
        if (active != null) {
            session = active
            isActive = true
            lastEventMillis = active.startEpochMillis
        }
        loaded = true
    }

    LaunchedEffect(session) {
        val s = session ?: return@LaunchedEffect
        container.gymRepository.observeSets(s.id).collect { setsFlow.value = it }
    }

    LaunchedEffect(isActive) {
        while (isActive) {
            val s = session
            if (s != null) sessionElapsedSec = (System.currentTimeMillis() - s.startEpochMillis) / 1000
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp, 16.dp, 12.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            IconButton(onClick = { onNavigate(Routes.GYM_HISTORY) }) { Icon(Icons.Filled.History, contentDescription = "History") }
        }

        if (!loaded) return@Column

        if (exercises.isEmpty()) {
            EmptyState(icon = Icons.Filled.FitnessCenter, title = "Nothing scheduled today", subtitle = "Plan today on the calendar first.")
            return@Column
        }

        if (isFinished) {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                item {
                    DoromCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Workout complete 💪", style = MaterialTheme.typography.headlineMedium)
                            Text(formatDuration(sessionElapsedSec), style = DataText.hero, color = MaterialTheme.colorScheme.primary)
                            Text("${sets.size} sets logged", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(modifier = Modifier.padding(top = 12.dp), onClick = onBack) { Text("Done") }
                        }
                    }
                }
            }
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp, 8.dp, 20.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isActive) PulsingRecDot()
            Text(formatDuration(sessionElapsedSec), style = DataText.hero, color = MaterialTheme.colorScheme.primary)
        }

        val tint = categoryTint(location.ordinal)

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { GymSectionHeader(title = "${exercises.size} exercises") }

                itemsIndexed(exercises) { index, exercise ->
                    val setsForExercise = sets.filter { it.exerciseId == exercise.id }
                    when {
                        index < currentIndex || (index == currentIndex && !isActive && setsForExercise.isNotEmpty() && setNumber > exercise.defaultSets) -> {
                            CompletedExerciseRow(exercise = exercise, tint = tint, loggedSets = setsForExercise.size)
                        }
                        index == currentIndex && isActive -> {
                            ActiveExerciseCard(
                                exercise = exercise,
                                tint = tint,
                                setNumber = setNumber,
                                setsForExercise = setsForExercise,
                                hasNext = index < exercises.lastIndex,
                                onLogSet = { weight, reps ->
                                    val now = System.currentTimeMillis()
                                    val rest = ((now - lastEventMillis) / 1000).toInt()
                                    val s = session
                                    if (s != null) {
                                        scope.launch { container.gymRepository.logSet(s.id, exercise.id, exercise.name, setNumber, weight, reps, rest) }
                                    }
                                    lastEventMillis = now
                                    setNumber++
                                },
                                onNext = { currentIndex++; setNumber = 1 },
                            )
                        }
                        else -> UpcomingExerciseRow(exercise = exercise, tint = tint)
                    }
                }
            }

            if (!isActive) {
                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(20.dp),
                    onClick = {
                        scope.launch {
                            val s = container.gymRepository.startSession(today, location)
                            session = s
                            lastEventMillis = s.startEpochMillis
                            isActive = true
                        }
                    },
                ) { Text("Let's GO! 💪") }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .doromClickable {
                            val s = session
                            if (s != null) scope.launch { container.gymRepository.finishSession(s) }
                            isActive = false
                            isFinished = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Finish", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun CompletedExerciseRow(exercise: GymExercise, tint: androidx.compose.ui.graphics.Color, loggedSets: Int) {
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ExerciseThumb(exercise = exercise, tint = Sage)
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Text("$loggedSets sets logged", style = MaterialTheme.typography.bodyMedium, color = Sage)
            }
            Icon(Icons.Filled.Check, contentDescription = "Done", tint = Sage)
        }
    }
}

@Composable
private fun UpcomingExerciseRow(exercise: GymExercise, tint: androidx.compose.ui.graphics.Color) {
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ExerciseThumb(exercise = exercise, tint = tint)
            Column {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    prescriptionLabel(exercise.defaultSets, exercise.defaultReps, exercise.defaultWeight),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ActiveExerciseCard(
    exercise: GymExercise,
    tint: androidx.compose.ui.graphics.Color,
    setNumber: Int,
    setsForExercise: List<GymSessionSet>,
    hasNext: Boolean,
    onLogSet: (Double, Int) -> Unit,
    onNext: () -> Unit,
) {
    var weightText by remember(exercise.id, setNumber) { mutableStateOf(if (exercise.defaultWeight > 0) exercise.defaultWeight.toString() else "") }
    var repsText by remember(exercise.id, setNumber) { mutableStateOf(exercise.defaultReps.toString()) }

    DoromCard(modifier = Modifier.fillMaxWidth(), borderColor = Terracotta) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ExerciseThumb(exercise = exercise, tint = tint)
                Column {
                    Text(exercise.name, style = MaterialTheme.typography.titleLarge)
                    Text("Set $setNumber / ${exercise.defaultSets}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    shape = com.arsham.dorom.ui.theme.InputShape,
                    value = weightText, onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Weight") }, modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    shape = com.arsham.dorom.ui.theme.InputShape,
                    value = repsText, onValueChange = { repsText = it.filter(Char::isDigit) },
                    label = { Text("Reps") }, modifier = Modifier.weight(1f),
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                onClick = {
                    onLogSet(weightText.toDoubleOrNull() ?: 0.0, repsText.toIntOrNull() ?: 0)
                },
            ) { Text("Log set") }
            if (setsForExercise.isNotEmpty()) {
                Text(
                    setsForExercise.joinToString("   ") { "${it.weight}×${it.reps}" },
                    style = DataText.small,
                    color = Sage,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (hasNext) {
                OutlinedButton(modifier = Modifier.padding(top = 10.dp), onClick = onNext) { Text("Next exercise") }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
