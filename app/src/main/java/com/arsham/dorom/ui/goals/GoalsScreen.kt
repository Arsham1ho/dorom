package com.arsham.dorom.ui.goals

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import com.arsham.dorom.data.entity.LongTermGoal
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.TopBarWithBack
import kotlinx.coroutines.launch

@Composable
fun GoalsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val goals by container.goalsRepository.observeGoals().collectAsStateWithLifecycle(initialValue = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Long-term goals", onBack = onBack) {
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Add goal") }
        }

        if (goals.isEmpty() && !showAdd) {
            EmptyState(icon = Icons.Filled.Flag, title = "No goals yet", subtitle = "Add something you're working toward over months, not days.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showAdd) {
                item {
                    GoalEditor(
                        onSave = { title, desc ->
                            scope.launch {
                                container.goalsRepository.upsertGoal(
                                    LongTermGoal(title = title, description = desc, createdAtEpochMillis = System.currentTimeMillis())
                                )
                            }
                            showAdd = false
                        },
                        onCancel = { showAdd = false },
                    )
                }
            }
            items(goals) { goal ->
                GoalCard(
                    goal = goal,
                    onProgressChange = { pct -> scope.launch { container.goalsRepository.upsertGoal(goal.copy(progressPercent = pct)) } },
                    onDelete = { scope.launch { container.goalsRepository.deleteGoal(goal) } },
                )
            }
        }
    }
}

@Composable
private fun GoalEditor(onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape, value = title, onValueChange = { title = it }, label = { Text("Goal") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape, 
                value = desc, onValueChange = { desc = it }, label = { Text("Why it matters") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { if (title.isNotBlank()) onSave(title.trim(), desc.trim()) }) { Text("Save") }
                Button(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: LongTermGoal, onProgressChange: (Int) -> Unit, onDelete: () -> Unit) {
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProgressRing(percent = goal.progressPercent / 100f, size = 56.dp, strokeWidth = 6.dp)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(goal.title, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                }
                if (goal.description.isNotBlank()) {
                    Text(goal.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Slider(
                    value = goal.progressPercent.toFloat(),
                    onValueChange = { onProgressChange(it.toInt()) },
                    valueRange = 0f..100f,
                )
            }
        }
    }
}
