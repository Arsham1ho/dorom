package com.arsham.dorom.ui.goals

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.LongTermGoal
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DateField
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.LocalFileImage
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.Tag
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.DangerRed
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val GoalDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

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

        if (goals.isEmpty()) {
            EmptyState(icon = Icons.Filled.Flag, title = "No goals yet", subtitle = "Add something you're working toward over months, not days.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(goals) { goal ->
                GoalCard(
                    goal = goal,
                    onProgressChange = { pct -> scope.launch { container.goalsRepository.upsertGoal(goal.copy(progressPercent = pct)) } },
                    onDelete = { scope.launch { container.goalsRepository.deleteGoal(goal) } },
                )
            }
        }
    }

    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            GoalEditor(
                onSave = { goal, imageUri ->
                    scope.launch { container.goalsRepository.saveGoal(goal, imageUri) }
                    showAdd = false
                },
                onCancel = { showAdd = false },
            )
        }
    }
}

@Composable
private fun GoalEditor(onSave: (LongTermGoal, android.net.Uri?) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var deadlineDate by remember { mutableStateOf<LocalDate?>(null) }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> imageUri = uri }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Add a goal", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (imageUri != null) "Image attached ✓" else "Add a photo (optional)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (imageUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Pick") }
            }
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = title, onValueChange = { title = it }, label = { Text("Goal") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = desc, onValueChange = { desc = it }, label = { Text("Why it matters") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = tag, onValueChange = { tag = it }, label = { Text("Tag (e.g. Career, Health)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                DateField(label = "When to start", date = startDate, onDateChange = { startDate = it })
                DateField(label = "Deadline", date = deadlineDate, onDateChange = { deadlineDate = it })
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            LongTermGoal(
                                title = title.trim(),
                                description = desc.trim(),
                                tag = tag.trim().ifEmpty { null },
                                startDate = startDate?.toString(),
                                deadlineDate = deadlineDate?.toString(),
                                createdAtEpochMillis = System.currentTimeMillis(),
                            ),
                            imageUri,
                        )
                    }
                }) { Text("Save") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun GoalCard(goal: LongTermGoal, onProgressChange: (Int) -> Unit, onDelete: () -> Unit) {
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
        DoromCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
            Column {
                goal.imagePath?.let { path ->
                    LocalFileImage(
                        path = path,
                        contentDescription = goal.title,
                        modifier = Modifier.fillMaxWidth().height(110.dp).clip(CardShape),
                    )
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProgressRing(percent = goal.progressPercent / 100f, size = 56.dp, strokeWidth = 6.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(goal.title, style = MaterialTheme.typography.titleMedium)
                            if (goal.description.isNotBlank()) {
                                Text(goal.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    val startLabel = goal.startDate?.let { LocalDate.parse(it).format(GoalDateFormat) }
                    val deadlineLabel = goal.deadlineDate?.let { LocalDate.parse(it).format(GoalDateFormat) }
                    if (goal.tag != null || startLabel != null || deadlineLabel != null) {
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            goal.tag?.let { Tag(text = it, filled = true) }
                            val dateLine = listOfNotNull(
                                startLabel?.let { "Starts $it" },
                                deadlineLabel?.let { "Due $it" },
                            ).joinToString(" · ")
                            if (dateLine.isNotEmpty()) {
                                Text(dateLine, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Slider(
                        value = goal.progressPercent.toFloat(),
                        onValueChange = { onProgressChange(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false; scope.launch { dismissState.reset() } },
            title = { Text("Remove goal?") },
            text = { Text("Remove \"${goal.title}\"?") },
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
