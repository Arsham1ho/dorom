package com.arsham.dorom.ui.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.PlanTask
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.Tag
import com.arsham.dorom.ui.components.TimeField
import com.arsham.dorom.ui.navigation.Routes
import com.arsham.dorom.ui.theme.BadgeBlue
import com.arsham.dorom.ui.theme.BadgeGold
import com.arsham.dorom.ui.theme.BadgeViolet
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.ChipShape
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.ui.theme.Terracotta
import com.arsham.dorom.ui.theme.WarnAmber
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

private val categoryPalette = listOf(Terracotta, Sage, BadgeBlue, BadgeViolet, BadgeGold, WarnAmber)

private fun categoryColor(category: String): Color {
    val index = TASK_CATEGORIES.indexOf(category).let { if (it < 0) 0 else it }
    return categoryPalette[index % categoryPalette.size]
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Breakfast" -> Icons.Filled.FreeBreakfast
    "Lunch" -> Icons.Filled.LunchDining
    "Dinner" -> Icons.Filled.DinnerDining
    "Podcast" -> Icons.Filled.Podcasts
    "Work" -> Icons.Filled.Work
    "Workout" -> Icons.Filled.FitnessCenter
    "Course" -> Icons.Filled.School
    "Personal Project" -> Icons.Filled.Lightbulb
    "Guitar" -> Icons.Filled.MusicNote
    "Chores" -> Icons.Filled.CleaningServices
    "Break" -> Icons.Filled.SelfImprovement
    "Movie" -> Icons.Filled.Theaters
    "Class" -> Icons.Filled.MenuBook
    "Reading" -> Icons.Filled.AutoStories
    "Sleep" -> Icons.Filled.Bedtime
    else -> Icons.Filled.Work
}

@Composable
private fun CategoryChip(category: String, selected: Boolean, onClick: () -> Unit) {
    val color = categoryColor(category)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(ChipShape)
            .background(if (selected) color else Color.Transparent)
            .border(BorderStroke(1.dp, color.copy(alpha = if (selected) 0f else 0.5f)), ChipShape)
            .doromClickable(onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Icon(
            categoryIcon(category),
            contentDescription = null,
            tint = if (selected) Color.White else color,
            modifier = Modifier.size(16.dp),
        )
        Text(
            category,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else color,
            maxLines = 1,
        )
    }
}

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

        DayPlanContent(date = viewedDate, onNavigate = onNavigate, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DayPlanContent(date: LocalDate, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
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
    var detailsTask by remember(date) { mutableStateOf<PlanTask?>(null) }

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
                title = if (tasks.isEmpty()) "No tasks yet" else "${tasks.size} ${if (tasks.size == 1) "task" else "tasks"}",
                action = {
                    IconButton(onClick = { showAddTask = !showAddTask }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add task")
                    }
                },
            )
        }

        // A task with an end time is still "current" until that end time passes, so the marker
        // must not slide past it just because its start time has already begun.
        val nowInsertIndex = if (isToday) {
            tasks.indexOfFirst { nowLabel < (it.endTime ?: it.startTime) }.let { if (it == -1) tasks.size else it }
        } else -1

        tasks.forEachIndexed { index, task ->
            if (index == nowInsertIndex) {
                item(key = "now") { NowMarker(nowLabel) }
            }
            item(key = task.id) {
                TaskRow(
                    task,
                    onToggle = { scope.launch { container.planRepository.toggleTaskDone(task) } },
                    onDelete = { scope.launch { container.planRepository.deleteTask(task) } },
                    onClick = {
                        if (task.category == "Workout") onNavigate(Routes.TRACK_GYM) else detailsTask = task
                    },
                )
            }
        }
        if (nowInsertIndex == tasks.size && isToday) {
            item(key = "now-end") { NowMarker(nowLabel) }
        }
    }

    if (showAddTask) {
        Dialog(onDismissRequest = { showAddTask = false }) {
            AddTaskCard(
                date = dateString,
                onAdd = { draft ->
                    scope.launch {
                        if (plan == null) container.planRepository.savePlan(dateString, "07:00", "23:00", emptyList())
                        container.planRepository.addTask(
                            dateString,
                            PlanTask(date = dateString, title = draft.title, category = draft.category, startTime = draft.startTime, endTime = draft.endTime, orderIndex = 0),
                        )
                    }
                    showAddTask = false
                },
                onDismiss = { showAddTask = false },
            )
        }
    }

    detailsTask?.let { task ->
        Dialog(onDismissRequest = { detailsTask = null }) {
            TaskDetailsCard(
                task = task,
                onToggle = { scope.launch { container.planRepository.toggleTaskDone(task) } },
                onDismiss = { detailsTask = null },
            )
        }
    }
}

@Composable
private fun TaskDetailsCard(task: PlanTask, onToggle: () -> Unit, onDismiss: () -> Unit) {
    val color = categoryColor(task.category)
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Task details", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                IconBadge(icon = categoryIcon(task.category), tint = color, size = 48.dp)
                Column {
                    Text(task.title, style = MaterialTheme.typography.titleLarge)
                    Text(task.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Time", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (task.endTime != null) "${task.startTime}–${task.endTime}" else task.startTime,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (task.isDone) "Completed" else "Not done yet",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.isDone) Sage else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Tag(
                    text = if (task.isDone) "Mark as not done" else "Mark as done",
                    accent = color,
                    filled = !task.isDone,
                    modifier = Modifier.doromClickable(onToggle),
                )
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TaskRow(task: PlanTask, onToggle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(com.arsham.dorom.ui.theme.CardShape)
                    .background(com.arsham.dorom.ui.theme.DangerRed)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
            }
        },
    ) {
        DoromCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
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
            }
        }
    }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false; scope.launch { dismissState.reset() } },
            title = { Text("Remove task?") },
            text = { Text("Remove \"${task.title}\" from this day's plan?") },
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

private data class TaskDraft(val title: String, val category: String, val startTime: String, val endTime: String?)

@Composable
private fun AddTaskCard(date: String, onAdd: (TaskDraft) -> Unit, onDismiss: () -> Unit) {
    val container = LocalAppContainer.current
    var newTitle by remember { mutableStateOf("") }
    var newTime by remember { mutableStateOf("09:00") }
    var newEndTime by remember { mutableStateOf<String?>(null) }
    var newCategory by remember { mutableStateOf(TASK_CATEGORIES.first()) }

    var gymCategories by remember(date) { mutableStateOf<String?>(null) }
    var gymExerciseCount by remember(date) { mutableStateOf(0) }
    LaunchedEffect(date) {
        val gymExercises = container.gymRepository.getScheduledExercises(date)
        gymCategories = if (gymExercises.isEmpty()) null else gymExercises.map { it.category }.distinct().joinToString(", ")
        gymExerciseCount = gymExercises.size
    }

    val projects by container.personalProjectRepository.observeProjects().collectAsStateWithLifecycle(initialValue = emptyList())

    DoromCard(modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp)) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Add a task", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            gymCategories?.let { categories ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(CardShape)
                        .background(Terracotta.copy(alpha = 0.14f))
                        .doromClickable {
                            newCategory = "Workout"
                            newTitle = "Gym: $categories"
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IconBadge(icon = Icons.Filled.FitnessCenter, tint = Terracotta, size = 38.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Today's gym plan is ready", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "$categories · $gymExerciseCount exercise${if (gymExerciseCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text("Use it", style = MaterialTheme.typography.labelMedium, color = Terracotta)
                }
            }

            LazyRow(modifier = Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(TASK_CATEGORIES) { category ->
                    CategoryChip(
                        category = category,
                        selected = category == newCategory,
                        onClick = {
                            newCategory = category
                            if (newTitle.isEmpty()) newTitle = category
                        },
                    )
                }
            }
            if (newCategory == "Personal Project") {
                if (projects.isNotEmpty()) {
                    Text(
                        "Your projects",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    LazyRow(modifier = Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(projects) { project ->
                            Tag(
                                text = project.name,
                                filled = newTitle == "Project: ${project.name}",
                                modifier = Modifier.doromClickable { newTitle = "Project: ${project.name}" },
                            )
                        }
                    }
                } else {
                    Text(
                        "No projects tracked yet — add one under Track > Personal Projects.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
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
