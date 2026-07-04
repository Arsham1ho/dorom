package com.arsham.dorom.ui.gym

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.AlertDialog
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
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.LocalFileImage
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.Terracotta
import kotlinx.coroutines.launch

@Composable
fun GymCategoryScreen(location: GymLocation, category: String, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val exercises by container.gymRepository.observeExercises(location, category).collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(
            title = category,
            onBack = onBack,
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add exercise")
                }
            },
        )

        if (exercises.isEmpty()) {
            EmptyState(icon = Icons.AutoMirrored.Filled.DirectionsRun, title = "No exercises yet", subtitle = "Tap + to add one to your $category workout.")
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(exercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onDelete = { scope.launch { container.gymRepository.deleteExercise(exercise) } },
                )
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, uri ->
                scope.launch { container.gymRepository.addExercise(location, category, name, uri) }
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun ExerciseCard(exercise: GymExercise, onDelete: () -> Unit) {
    DoromCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Column {
            if (exercise.imagePath != null) {
                LocalFileImage(
                    path = exercise.imagePath,
                    contentDescription = exercise.name,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconBadge(icon = Icons.AutoMirrored.Filled.DirectionsRun, tint = Terracotta, size = 56.dp)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun AddExerciseDialog(onDismiss: () -> Unit, onSave: (String, android.net.Uri?) -> Unit) {
    var newName by remember { mutableStateOf("") }
    var pendingImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        pendingImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add exercise") },
        text = {
            Column {
                OutlinedTextField(
                    shape = com.arsham.dorom.ui.theme.InputShape,
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (pendingImageUri != null) {
                        Text("Image attached ✓", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    } else {
                        Text(
                            "Add a photo (optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    IconButton(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Pick image")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (newName.isNotBlank()) onSave(newName.trim(), pendingImageUri) }) { Text("Save exercise") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
