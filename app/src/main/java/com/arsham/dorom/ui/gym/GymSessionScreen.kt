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
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.data.entity.GymSession
import com.arsham.dorom.data.entity.GymSessionSet
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.util.todayString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter

@Composable
fun GymSessionScreen(location: GymLocation, onBack: () -> Unit) {
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
        TopBarWithBack(title = "Workout", onBack = onBack)

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

        if (!isActive) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { SectionHeader(title = "Today's exercises, in order") }
                items(exercises) { ex -> DoromCard(modifier = Modifier.fillMaxWidth()) { Text(ex.name, style = MaterialTheme.typography.titleMedium) } }
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        onClick = {
                            scope.launch {
                                val s = container.gymRepository.startSession(today, location)
                                session = s
                                lastEventMillis = s.startEpochMillis
                                isActive = true
                            }
                        },
                    ) { Text("Let's GO! 💪") }
                }
            }
            return@Column
        }

        // Active session
        val exercise = exercises.getOrNull(currentIndex)
        var weightText by remember(currentIndex) { mutableStateOf("") }
        var repsText by remember(currentIndex) { mutableStateOf("") }
        val setsForExercise = exercise?.let { ex -> sets.filter { it.exerciseId == ex.id } } ?: emptyList()

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(sessionElapsedSec), style = DataText.large, color = MaterialTheme.colorScheme.primary)
                    Text("${currentIndex + 1}/${exercises.size}", style = DataText.medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (exercise != null) {
                item {
                    DoromCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(exercise.name, style = MaterialTheme.typography.headlineMedium)
                            Text("Set $setNumber", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    val w = weightText.toDoubleOrNull() ?: 0.0
                                    val r = repsText.toIntOrNull() ?: 0
                                    val now = System.currentTimeMillis()
                                    val rest = ((now - lastEventMillis) / 1000).toInt()
                                    val s = session
                                    if (s != null) {
                                        scope.launch { container.gymRepository.logSet(s.id, exercise.id, exercise.name, setNumber, w, r, rest) }
                                    }
                                    lastEventMillis = now
                                    setNumber++
                                    weightText = ""; repsText = ""
                                },
                            ) { Text("Log set") }
                        }
                    }
                }
                if (setsForExercise.isNotEmpty()) {
                    item {
                        Text(
                            setsForExercise.joinToString("   ") { "${it.weight}×${it.reps}" },
                            style = DataText.small,
                            color = Sage,
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (currentIndex < exercises.lastIndex) {
                            OutlinedButton(onClick = { currentIndex++; setNumber = 1 }) { Text("Next exercise") }
                        }
                        Button(onClick = {
                            val s = session
                            if (s != null) scope.launch { container.gymRepository.finishSession(s) }
                            isActive = false
                            isFinished = true
                        }) { Text("Finish") }
                    }
                }
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
