package com.arsham.dorom.ui.journal

import android.Manifest
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.repository.JournalEntryPlain
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.RecordingWaveform
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.DangerRed
import com.arsham.dorom.util.AudioPlayer
import com.arsham.dorom.util.AudioRecorder
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonalJournalScreen(onBack: () -> Unit) {
    var unlocked by remember { mutableStateOf(false) }
    val unlock = rememberBiometricUnlock(onSuccess = { unlocked = true })
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Personal journal", onBack = onBack) {
            if (unlocked) {
                IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Add entry") }
            }
        }

        if (!unlocked) {
            LockedPlaceholder(modifier = Modifier.weight(1f), onUnlock = unlock)
        } else {
            UnlockedJournal(modifier = Modifier.weight(1f), showAdd = showAdd, onShowAddChange = { showAdd = it })
        }
    }
}

@Composable
private fun LockedPlaceholder(modifier: Modifier = Modifier, onUnlock: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyState(icon = Icons.Filled.Lock, title = "Locked", subtitle = "Authenticate to see your private entries.")
        Button(onClick = onUnlock) { Text("Unlock") }
    }
}

@Composable
private fun UnlockedJournal(modifier: Modifier = Modifier, showAdd: Boolean, onShowAddChange: (Boolean) -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val entries by container.journalRepository.observeEntries().collectAsStateWithLifecycle(initialValue = emptyList())
    var detailsEntry by remember { mutableStateOf<JournalEntryPlain?>(null) }

    if (entries.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Lock,
            title = "No entries yet",
            subtitle = "Tap + to write, record a voice note, or record a quick video.",
            modifier = modifier,
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(entries, key = { it.id }) { entry ->
                JournalEntryRow(
                    entry,
                    onClick = { detailsEntry = entry },
                    onDelete = { scope.launch { container.journalRepository.deleteEntry(entry) } },
                )
            }
        }
    }

    if (showAdd) {
        Dialog(onDismissRequest = { onShowAddChange(false) }) {
            AddEntryEditor(
                onSave = { text, audioPath, videoPath ->
                    scope.launch { container.journalRepository.saveEntry(null, text, audioPath, videoPath) }
                    onShowAddChange(false)
                },
                onCancel = { onShowAddChange(false) },
            )
        }
    }

    detailsEntry?.let { entry ->
        Dialog(onDismissRequest = { detailsEntry = null }) {
            JournalEntryDetails(entry = entry, onDismiss = { detailsEntry = null })
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun JournalEntryRow(entry: JournalEntryPlain, onClick: () -> Unit, onDelete: () -> Unit) {
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
        DoromCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
            Column {
                Text(formatDate(entry.createdAtEpochMillis), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                when {
                    entry.videoPath != null -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(18.dp))
                        Text("Video note", style = MaterialTheme.typography.bodyLarge)
                    }
                    entry.audioPath != null -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(18.dp))
                        Text("Voice note", style = MaterialTheme.typography.bodyLarge)
                    }
                    else -> Text(
                        entry.text,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false; scope.launch { dismissState.reset() } },
            title = { Text("Remove entry?") },
            text = { Text("Remove this journal entry? This can't be undone.") },
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

@Composable
private fun AddEntryEditor(onSave: (String, String?, String?) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    var draft by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var pendingAudioFile by remember { mutableStateOf<File?>(null) }
    var pendingVideoFile by remember { mutableStateOf<File?>(null) }
    val recorder = remember { AudioRecorder(context) }
    // Only cancel (and delete the in-progress file) if the dialog closes mid-recording — once
    // stop() has run, the file is either about to be saved or already orphaned, but it's no
    // longer safe to blanket-delete: doing so unconditionally would wipe a just-saved recording
    // the instant this editor leaves composition after a successful Save.
    DisposableEffect(Unit) { onDispose { if (isRecording) runCatching { recorder.cancel() } } }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingAudioFile = container.journalRepository.newAudioFile()
            recorder.start(pendingAudioFile!!)
            isRecording = true
        }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (!success) pendingVideoFile = null
    }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("New entry", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            OutlinedTextField(
                shape = com.arsham.dorom.ui.theme.InputShape,
                value = draft,
                onValueChange = { draft = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (isRecording) {
                            val file = recorder.stop()
                            isRecording = false
                            pendingAudioFile = file
                        } else {
                            micPermission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                ) {
                    Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null)
                    Text(if (isRecording) "Stop" else if (pendingAudioFile != null) "Voice ✓" else "Record voice")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val file = container.journalRepository.newVideoFile()
                        pendingVideoFile = file
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        videoLauncher.launch(uri)
                    },
                ) {
                    Icon(Icons.Filled.Videocam, contentDescription = null)
                    Text(if (pendingVideoFile != null) "Video ✓" else "Record video")
                }
            }
            if (isRecording) {
                RecordingWaveform(
                    isRecording = true,
                    getAmplitude = { recorder.currentAmplitude() },
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(onClick = {
                    // Finalize an in-progress recording first — saving while still recording would
                    // reference an unfinished file that dispose-cleanup would then delete out from
                    // under the just-created entry.
                    val finalAudioPath = if (isRecording) {
                        val file = recorder.stop()
                        isRecording = false
                        file?.absolutePath
                    } else {
                        pendingAudioFile?.absolutePath
                    }
                    if (draft.isNotBlank() || finalAudioPath != null || pendingVideoFile != null) {
                        onSave(draft.trim(), finalAudioPath, pendingVideoFile?.absolutePath)
                    }
                }) { Text("Save") }
            }
        }
    }
}

@Composable
private fun JournalEntryDetails(entry: JournalEntryPlain, onDismiss: () -> Unit) {
    val player = remember { AudioPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { player.stop() } }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(formatDate(entry.createdAtEpochMillis), style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            if (entry.text.isNotBlank()) {
                Text(entry.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 10.dp))
            }
            entry.videoPath?.let { path ->
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoPath(path)
                            setMediaController(MediaController(ctx).apply { setAnchorView(this@apply) })
                            setOnPreparedListener { start() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(240.dp).padding(top = 12.dp),
                )
            }
            entry.audioPath?.let { path ->
                DoromCard(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    onClick = {
                        if (isPlaying) {
                            player.stop(); isPlaying = false
                        } else {
                            player.play(path) { isPlaying = false }
                            isPlaying = true
                        }
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayCircle, contentDescription = null)
                        Text(if (isPlaying) "Playing…" else "Play voice note")
                    }
                }
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("EEE, MMM d · HH:mm", Locale.ENGLISH).format(Date(epochMillis))
