package com.arsham.dorom.ui.courses

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
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.Course
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.TopBarWithBack
import kotlinx.coroutines.launch

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

        if (courses.isEmpty() && !showAdd) {
            EmptyState(icon = Icons.Filled.MenuBook, title = "No courses yet", subtitle = "Add a course and log sessions as you finish them.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showAdd) {
                item { CourseEditor(onSave = { name, total -> scope.launch { container.courseRepository.upsertCourse(Course(name = name, totalLessons = total)) }; showAdd = false }, onCancel = { showAdd = false }) }
            }
            items(courses) { course ->
                CourseCard(
                    course = course,
                    onLogSession = { delta -> scope.launch { container.courseRepository.logSessionCompleted(course, delta) } },
                    onDelete = { scope.launch { container.courseRepository.deleteCourse(course) } },
                )
            }
        }
    }
}

@Composable
private fun CourseEditor(onSave: (String, Int) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("") }
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = total, onValueChange = { total = it.filter(Char::isDigit) }, label = { Text("Total lessons") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { val t = total.toIntOrNull() ?: 0; if (name.isNotBlank() && t > 0) onSave(name.trim(), t) }) { Text("Save") }
                Button(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun CourseCard(course: Course, onLogSession: (Int) -> Unit, onDelete: () -> Unit) {
    val pct = if (course.totalLessons == 0) 0f else course.completedLessons.toFloat() / course.totalLessons
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProgressRing(percent = pct, size = 64.dp, strokeWidth = 7.dp)
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(course.name, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                }
                Text(
                    "${course.completedLessons} done · ${course.totalLessons - course.completedLessons} remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onLogSession(-1) }) { Icon(Icons.Filled.Remove, contentDescription = "Log one fewer") }
                    Text("Sessions", style = MaterialTheme.typography.labelMedium)
                    IconButton(onClick = { onLogSession(1) }) { Icon(Icons.Filled.Add, contentDescription = "Log a session") }
                }
            }
        }
    }
}
