package com.arsham.dorom.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.arsham.dorom.ui.theme.Terracotta
import com.arsham.dorom.ui.theme.doromClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val TASK_CATEGORIES = listOf(
    "Breakfast", "Lunch", "Dinner", "Podcast", "Work", "Workout", "Course",
    "Personal Project", "Guitar", "Chores", "Break", "Movie", "Class", "Reading", "Sleep",
)

private fun dateLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    }
}

private fun addHour(time: String): String {
    val (h, m) = time.split(":").map { it.toInt() }
    return "%02d:%02d".format((h + 1) % 24, m)
}

@Composable
fun PlanScreen(onNavigate: (String) -> Unit) {
    var viewedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Plan", style = MaterialTheme.typography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(text = "Weekly", modifier = Modifier.doromClickable { onNavigate(Routes.WEEKLY) })
                Tag(text = "Goals", modifier = Modifier.doromClickable { onNavigate(Routes.GOALS) })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewedDate = viewedDate.minusDays(1) }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(dateLabel(viewedDate), style = MaterialTheme.typography.titleLarge)
                Text(
                    viewedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { viewedDate = viewedDate.plusDays(1) }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next day")
            }
        }

        DayPlanContent(date = viewedDate, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DayPlanContent(date: LocalDate, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val dateString = remember(date) { date.toString() }
    val today = remember { LocalDate.now() }
    val isToday = date == today
    val isPast = date < today

    val plan by container.planRepository.observePlan(dateString).collectAsStateWithLifecycle(initialValue = null)
    val tasks by container.planRepository.observeTasks(dateString).collectAsStateWithLifecycle(initialValue = emptyList())
    val review by container.reviewRepository.observeReview(dateString).collectAsStateWithLifecycle(initialValue = null)
    var showAddTask by remember(date) { mutableStateOf(false) }

    var nowTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(isToday) {
        while (isToday) {
            nowTime = LocalTime.now()
            delay(30_000)
        }
    }
    val nowLabel = remember(nowTime) { "%02d:%02d".format(nowTime.hour, nowTime.minute) }

    if (plan == null && isPast) {
        EmptyState(
            icon = Icons.Filled.WbSunny,
            title = "No plan was set for this day",
            subtitle = "There's nothing recorded here.",
            modifier = modifier,
        )
        return
    }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DoromCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    TimeField(
                        label = if (isToday || isPast) "Actual wake time" else "Wake up at",
                        time = review?.actualWakeTime ?: plan?.wakeTargetTime ?: "07:00",
                        onTimeChange = { time ->
                            scope.launch {
                                if (plan == null) {
                                    container.planRepository.savePlan(dateString, time, plan?.bedTargetTime ?: "23:00", emptyList())
                                } else if (isToday || isPast) {
                                    container.reviewRepository.recordActualWakeTime(dateString, time)
                                } else {
                                    container.planRepository.savePlan(dateString, time, plan!!.bedTargetTime, container.planRepository.getTasks(dateString))
                                }
                            }
                        },
                    )
                    TimeField(
                        label = if (isToday || isPast) "Actual sleep time" else "Bedtime",
                        time = review?.actualSleepTime ?: plan?.bedTargetTime ?: "23:00",
                        onTimeChange = { time ->
                            scope.launch {
                                if (plan == null) {
                                    container.planRepository.savePlan(dateString, plan?.wakeTargetTime ?: "07:00", time, emptyList())
                                } else if (isToday || isPast) {
                                    container.reviewRepository.recordActualSleepTime(dateString, time)
                                } else {
                                    container.planRepository.savePlan(dateString, plan!!.wakeTargetTime, time, container.planRepository.getTasks(dateString))
                                }
                            }
                        },
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = if (tasks.isEmpty()) "No tasks yet" else "${tasks.size} tasks",
                action = {
                    IconButton(onClick = { showAddTask = !showAddTask }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add task")
                    }
                },
            )
        }

        val nowInsertIndex = if (isToday) tasks.indexOfFirst { it.startTime > nowLabel }.let { if (it == -1) tasks.size else it } else -1

        tasks.forEachIndexed { index, task ->
            if (index == nowInsertIndex) {
                item(key = "now") { NowMarker(nowLabel) }
            }
            item(key = task.id) {
                TaskRow(task, onToggle = { scope.launch { container.planRepository.toggleTaskDone(task) } }, onDelete = { scope.launch { container.planRepository.deleteTask(task) } })
            }
        }
        if (nowInsertIndex == tasks.size && isToday) {
            item(key = "now-end") { NowMarker(nowLabel) }
        }

        if (showAddTask) {
            item {
                AddTaskCard(date = dateString, onAdd = { draft ->
                    scope.launch {
                        if (plan == null) container.planRepository.savePlan(dateString, "07:00", "23:00", emptyList())
                        container.planRepository.addTask(
                            dateString,
                            PlanTask(date = dateString, title = draft.title, category = draft.category, startTime = draft.startTime, endTime = draft.endTime, orderIndex = 0),
                        )
                    }
                    showAddTask = false
                })
            }
        }
    }
}

@Composable
private fun NowMarker(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(8.dp).background(Terracotta, CircleShape))
        Box(modifier = Modifier.weight(1f).height(2.dp).background(Terracotta))
        Text("Now $label", style = MaterialTheme.typography.labelMedium, color = Terracotta)
    }
}

@Composable
private fun TaskRow(task: PlanTask, onToggle: () -> Unit, onDelete: () -> Unit) {
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val timeLabel = if (task.endTime != null) "${task.startTime}–${task.endTime}" else task.startTime
                    Text(timeLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(task.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
        }
    }
}

private data class TaskDraft(val title: String, val category: String, val startTime: String, val endTime: String?)

@Composable
private fun AddTaskCard(date: String, onAdd: (TaskDraft) -> Unit) {
    val container = LocalAppContainer.current
    var newTitle by remember { mutableStateOf("") }
    var newTime by remember { mutableStateOf("09:00") }
    var newEndTime by remember { mutableStateOf<String?>(null) }
    var newCategory by remember { mutableStateOf(TASK_CATEGORIES.first()) }

    var gymHint by remember(date, newCategory) { mutableStateOf<String?>(null) }
    LaunchedEffect(date, newCategory) {
        gymHint = null
        if (newCategory == "Workout") {
            val gymExercises = container.gymRepository.getScheduledExercises(date)
            if (gymExercises.isNotEmpty()) {
                val categories = gymExercises.map { it.category }.distinct().joinToString(", ")
                gymHint = "Gym plan for this day: $categories (${gymExercises.size} exercise${if (gymExercises.size == 1) "" else "s"})"
            }
        }
    }

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
            gymHint?.let { hint ->
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = Terracotta,
                    modifier = Modifier.padding(bottom = 6.dp).doromClickable { newTitle = "Gym: ${hint.removePrefix("Gym plan for this day: ").substringBefore(" (")}" },
                )
            }
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                TimeField(label = "Start time", time = newTime, onTimeChange = { newTime = it })
                if (newEndTime != null) {
                    TimeField(label = "End time", time = newEndTime!!, onTimeChange = { newEndTime = it })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Tag(
                    text = if (newEndTime != null) "Remove end time" else "+ End time",
                    modifier = Modifier.doromClickable {
                        newEndTime = if (newEndTime != null) null else addHour(newTime)
                    },
                )
                IconButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        onAdd(TaskDraft(newTitle.trim(), newCategory, newTime, newEndTime))
                        newTitle = ""
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add task")
                }
            }
        }
    }
}
