package com.arsham.dorom.ui.gym

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.arsham.dorom.ui.components.TopBarWithBack
import kotlinx.coroutines.launch

@Composable
fun GymCategoryScreen(location: GymLocation, category: String, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val exercises by container.gymRepository.observeExercises(location, category).collectAsStateWithLifecycle(initialValue = emptyList())
    val tint = categoryTint(category)

    var showAddDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<GymExercise?>(null) }

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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (exercises.isNotEmpty()) {
                item { GymSectionHeader(title = "${exercises.size} exercises") }
            }
            items(exercises) { exercise ->
                ExerciseRow(
                    exercise = exercise,
                    tint = tint,
                    onEdit = { editingExercise = exercise },
                    onDelete = { scope.launch { container.gymRepository.deleteExercise(exercise) } },
                )
            }
        }
    }

    if (showAddDialog) {
        ExerciseDialog(
            title = "Add exercise",
            initial = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, uri, sets, reps, weight ->
                scope.launch { container.gymRepository.addExercise(location, category, name, uri, sets, reps, weight) }
                showAddDialog = false
            },
        )
    }

    editingExercise?.let { exercise ->
        ExerciseDialog(
            title = "Edit exercise",
            initial = exercise,
            onDismiss = { editingExercise = null },
            onSave = { name, uri, sets, reps, weight ->
                scope.launch { container.gymRepository.updateExercise(exercise, name, uri, sets, reps, weight) }
                editingExercise = null
            },
        )
    }
}

@Composable
private fun ExerciseRow(exercise: GymExercise, tint: androidx.compose.ui.graphics.Color, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ExerciseThumb(exercise = exercise, tint = tint)
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    prescriptionLabel(exercise.defaultSets, exercise.defaultReps, exercise.defaultWeight),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { menuOpen = false; onEdit() },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { menuOpen = false; onDelete() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseDialog(
    title: String,
    initial: GymExercise?,
    onDismiss: () -> Unit,
    onSave: (String, android.net.Uri?, Int, Int, Double) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var sets by remember { mutableStateOf((initial?.defaultSets ?: 3).toString()) }
    var reps by remember { mutableStateOf((initial?.defaultReps ?: 10).toString()) }
    var weight by remember { mutableStateOf(if ((initial?.defaultWeight ?: 0.0) > 0) initial!!.defaultWeight.toString() else "") }
    var pendingImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        pendingImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    shape = com.arsham.dorom.ui.theme.InputShape,
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        shape = com.arsham.dorom.ui.theme.InputShape,
                        value = sets,
                        onValueChange = { sets = it.filter(Char::isDigit) },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        shape = com.arsham.dorom.ui.theme.InputShape,
                        value = reps,
                        onValueChange = { reps = it.filter(Char::isDigit) },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        shape = com.arsham.dorom.ui.theme.InputShape,
                        value = weight,
                        onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Weight") },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val hasImage = pendingImageUri != null || initial?.imagePath != null
                    Text(
                        if (hasImage) "Image attached ✓" else "Add a photo (optional)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Pick image")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        name.trim(),
                        pendingImageUri,
                        sets.toIntOrNull() ?: 3,
                        reps.toIntOrNull() ?: 10,
                        weight.toDoubleOrNull() ?: 0.0,
                    )
                }
            }) { Text("Save exercise") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
