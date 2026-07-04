package com.arsham.dorom.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.PlanTask
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.Tag
import com.arsham.dorom.ui.components.TimeField
import com.arsham.dorom.ui.navigation.Routes
import com.arsham.dorom.ui.theme.doromClickable
import com.arsham.dorom.util.todayString
import kotlinx.coroutines.launch
import java.time.LocalDate

val TASK_CATEGORIES = listOf(
    "Breakfast", "Lunch", "Dinner", "Podcast", "Work", "Workout", "Course",
    "Personal Project", "Guitar", "Chores", "Break", "Movie", "Class", "Reading", "Sleep",
)

private enum class PlanTab { TODAY, TOMORROW }

@Composable
fun PlanScreen(onNavigate: (String) -> Unit) {
    var tab by remember { mutableStateOf(PlanTab.TODAY) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Plan", style = MaterialTheme.typography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(text = "Weekly", modifier = Modifier.doromClickable { onNavigate(Routes.WEEKLY) })
                Tag(text = "Goals", modifier = Modifier.doromClickable { onNavigate(Routes.GOALS) })
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag(text = "Today", filled = tab == PlanTab.TODAY, modifier = Modifier.doromClickable { tab = PlanTab.TODAY })
            Tag(text = "Plan tomorrow night", filled = tab == PlanTab.TOMORROW, modifier = Modifier.doromClickable { tab = PlanTab.TOMORROW })
        }

        Spacer(modifier = Modifier.padding(4.dp))

        when (tab) {
            PlanTab.TODAY -> TodayPlanContent(modifier = Modifier.weight(1f))
            PlanTab.TOMORROW -> TomorrowPlanEditor(modifier = Modifier.weight(1f), onSaved = { tab = PlanTab.TODAY })
        }
    }
}

@Composable
private fun TodayPlanContent(modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val today = todayString()

    val plan by container.planRepository.observePlan(today).collectAsStateWithLifecycle(initialValue = null)
    val tasks by container.planRepository.observeTasks(today).collectAsStateWithLifecycle(initialValue = emptyList())
    val review by container.reviewRepository.observeReview(today).collectAsStateWithLifecycle(initialValue = null)

    if (plan == null) {
        EmptyState(
            icon = Icons.Filled.WbSunny,
            title = "No plan for today",
            subtitle = "Use \"Plan tomorrow night\" before bed so it's ready when you wake up.",
            modifier = modifier,
        )
        return
    }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DoromCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    TimeField(
                        label = "Actual wake time",
                        time = review?.actualWakeTime ?: plan!!.wakeTargetTime,
                        onTimeChange = { scope.launch { container.reviewRepository.recordActualWakeTime(today, it) } },
                    )
                    TimeField(
                        label = "Actual sleep time",
                        time = review?.actualSleepTime ?: plan!!.bedTargetTime,
                        onTimeChange = { scope.launch { container.reviewRepository.recordActualSleepTime(today, it) } },
                    )
                }
            }
        }
        item { SectionHeader(title = "Today's tasks") }
        items(tasks) { task ->
            TaskRow(task) { scope.launch { container.planRepository.toggleTaskDone(task) } }
        }
        if (tasks.isEmpty()) {
            item {
                Text(
                    "No tasks in tonight's plan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TaskRow(task: PlanTask, onToggle: () -> Unit) {
    DoromCard(modifier = Modifier.fillMaxWidth(), onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.startTime, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(task.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private data class TaskDraft(val title: String, val category: String, val startTime: String)

@Composable
private fun TomorrowPlanEditor(modifier: Modifier = Modifier, onSaved: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val tomorrow = remember { LocalDate.now().plusDays(1).toString() }
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = com.arsham.dorom.data.settings.DoromSettings(),
    )

    var wakeTime by remember { mutableStateOf("") }
    var bedTime by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    if (!initialized && settings.defaultWakeTime.isNotEmpty()) {
        wakeTime = settings.defaultWakeTime
        bedTime = settings.defaultBedTime
        initialized = true
    }

    val tasks = remember { mutableStateListOf<TaskDraft>() }
    var newTitle by remember { mutableStateOf("") }
    var newTime by remember { mutableStateOf("09:00") }
    var newCategory by remember { mutableStateOf(TASK_CATEGORIES.first()) }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DoromCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    TimeField(label = "Wake up at", time = wakeTime.ifEmpty { "07:00" }, onTimeChange = { wakeTime = it })
                    TimeField(label = "Bedtime", time = bedTime.ifEmpty { "23:00" }, onTimeChange = { bedTime = it })
                }
            }
        }

        item { SectionHeader(title = "Tasks, in order") }

        itemsIndexed(tasks) { index, draft ->
            DoromCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(draft.title, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(draft.startTime, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(draft.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { if (index > 0) tasks.add(index - 1, tasks.removeAt(index)) }) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                    }
                    IconButton(onClick = { if (index < tasks.lastIndex) tasks.add(index + 1, tasks.removeAt(index)) }) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                    }
                    IconButton(onClick = { tasks.removeAt(index) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove")
                    }
                }
            }
        }

        item {
            DoromCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Add a task", style = MaterialTheme.typography.titleMedium)
                    LazyRow(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TASK_CATEGORIES) { category ->
                            Tag(
                                text = category,
                                filled = category == newCategory,
                                modifier = Modifier.doromClickable {
                                    newCategory = category
                                    if (newTitle.isEmpty()) newTitle = category
                                },
                            )
                        }
                    }
                    OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape, 
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TimeField(label = "Start time", time = newTime, onTimeChange = { newTime = it })
                        IconButton(onClick = {
                            if (newTitle.isNotBlank()) {
                                tasks.add(TaskDraft(newTitle.trim(), newCategory, newTime))
                                newTitle = ""
                            }
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add task")
                        }
                    }
                }
            }
        }

        item {
            androidx.compose.material3.Button(
                onClick = {
                    scope.launch {
                        container.planRepository.savePlan(
                            date = tomorrow,
                            wakeTime = wakeTime.ifEmpty { "07:00" },
                            bedTime = bedTime.ifEmpty { "23:00" },
                            tasks = tasks.mapIndexed { index, draft ->
                                PlanTask(date = tomorrow, title = draft.title, category = draft.category, startTime = draft.startTime, orderIndex = index)
                            },
                        )
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Text(" Save tomorrow's plan", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}
