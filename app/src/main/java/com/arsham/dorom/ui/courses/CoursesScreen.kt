package com.arsham.dorom.ui.courses

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.Course
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DateField
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.DangerRed
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CourseDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

@Composable
fun CoursesScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val courses by container.courseRepository.observeCourses().collectAsStateWithLifecycle(initialValue = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Courses", onBack = onBack) {
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Add course") }
        }

        if (courses.isEmpty()) {
            EmptyState(icon = Icons.AutoMirrored.Filled.MenuBook, title = "No courses yet", subtitle = "Add a course and log sessions as you finish them.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(courses) { course ->
                CourseCard(
                    course = course,
                    onLogSession = { delta -> scope.launch { container.courseRepository.logSessionCompleted(course, delta) } },
                    onDelete = { scope.launch { container.courseRepository.deleteCourse(course) } },
                )
            }
        }
    }

    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            CourseEditor(
                onSave = { name, total, start, deadline ->
                    scope.launch {
                        container.courseRepository.upsertCourse(
                            Course(name = name, totalLessons = total, startDate = start?.toString(), deadlineDate = deadline?.toString())
                        )
                    }
                    showAdd = false
                },
                onCancel = { showAdd = false },
            )
        }
    }
}

@Composable
private fun CourseEditor(onSave: (String, Int, LocalDate?, LocalDate?) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var deadlineDate by remember { mutableStateOf<LocalDate?>(null) }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Add a course", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = name,
                onValueChange = { name = it },
                label = { Text("Course name") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = total, onValueChange = { total = it.filter(Char::isDigit) }, label = { Text("Total lessons") },
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
                    val t = total.toIntOrNull() ?: 0
                    if (name.isNotBlank() && t > 0) onSave(name.trim(), t, startDate, deadlineDate)
                }) { Text("Save") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CourseCard(course: Course, onLogSession: (Int) -> Unit, onDelete: () -> Unit) {
    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }
    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd) showConfirm = true
            false
        },
    )
    val pct = if (course.totalLessons == 0) 0f else course.completedLessons.toFloat() / course.totalLessons

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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProgressRing(percent = pct, size = 64.dp, strokeWidth = 7.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${course.completedLessons} done · ${course.totalLessons - course.completedLessons} remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val startLabel = course.startDate?.let { LocalDate.parse(it).format(CourseDateFormat) }
                    val deadlineLabel = course.deadlineDate?.let { LocalDate.parse(it).format(CourseDateFormat) }
                    if (startLabel != null || deadlineLabel != null) {
                        Text(
                            listOfNotNull(
                                startLabel?.let { "Starts $it" },
                                deadlineLabel?.let { "Due $it" },
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onLogSession(-1) }) { Icon(Icons.Filled.Remove, contentDescription = "Log one fewer") }
                        Text("Sessions", style = MaterialTheme.typography.labelMedium)
                        IconButton(onClick = { onLogSession(1) }) { Icon(Icons.Filled.Add, contentDescription = "Log a session") }
                    }
                }
            }
        }
    }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false; scope.launch { dismissState.reset() } },
            title = { Text("Remove course?") },
            text = { Text("Remove \"${course.name}\" and its progress?") },
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
