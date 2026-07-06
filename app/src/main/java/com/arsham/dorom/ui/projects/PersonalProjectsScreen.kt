package com.arsham.dorom.ui.projects

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
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
import com.arsham.dorom.data.entity.PersonalProject
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.DangerRed
import kotlinx.coroutines.launch

@Composable
fun PersonalProjectsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val projects by container.personalProjectRepository.observeProjects().collectAsStateWithLifecycle(initialValue = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Personal Projects", onBack = onBack) {
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Add project") }
        }

        if (projects.isEmpty()) {
            EmptyState(icon = Icons.Filled.Lightbulb, title = "No projects yet", subtitle = "Track the side projects you build for yourself, beside work.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(projects) { project ->
                ProjectCard(
                    project = project,
                    onProgressChange = { pct -> scope.launch { container.personalProjectRepository.upsertProject(project.copy(progressPercent = pct)) } },
                    onDelete = { scope.launch { container.personalProjectRepository.deleteProject(project) } },
                )
            }
        }
    }

    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            ProjectEditor(
                onSave = { name, desc ->
                    scope.launch {
                        container.personalProjectRepository.upsertProject(
                            PersonalProject(name = name, description = desc, createdAtEpochMillis = System.currentTimeMillis())
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
private fun ProjectEditor(onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Add a project", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = name, onValueChange = { name = it }, label = { Text("Project name") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = desc, onValueChange = { desc = it }, label = { Text("What's it about? (optional)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(onClick = { if (name.isNotBlank()) onSave(name.trim(), desc.trim()) }) { Text("Save") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ProjectCard(project: PersonalProject, onProgressChange: (Int) -> Unit, onDelete: () -> Unit) {
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
        DoromCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                com.arsham.dorom.ui.components.ProgressRing(percent = project.progressPercent / 100f, size = 56.dp, strokeWidth = 6.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium)
                    if (project.description.isNotBlank()) {
                        Text(
                            project.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    androidx.compose.material3.Slider(
                        value = project.progressPercent.toFloat(),
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
            title = { Text("Remove project?") },
            text = { Text("Remove \"${project.name}\"?") },
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
