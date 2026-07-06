package com.arsham.dorom.ui.guitar

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.GuitarSong
import com.arsham.dorom.data.entity.SongStatus
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.LocalFileImage
import com.arsham.dorom.ui.components.RecordingWaveform
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.Tag
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.BadgeViolet
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.doromClickable
import com.arsham.dorom.util.AudioPlayer
import com.arsham.dorom.util.AudioRecorder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GuitarScreen(onBack: () -> Unit, onOpenSong: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val songs by container.guitarRepository.observeSongs().collectAsStateWithLifecycle(initialValue = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Guitar", onBack = onBack) {
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Add song") }
        }

        if (songs.isEmpty()) {
            EmptyState(icon = Icons.Filled.MusicNote, title = "No songs yet", subtitle = "Add a song you're learning, then attach tabs and recordings.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(songs) { song ->
                DoromCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpenSong(song.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            IconBadge(icon = Icons.Filled.MusicNote, tint = BadgeViolet)
                            Column {
                                Text(song.title.ifBlank { "Untitled song" }, style = MaterialTheme.typography.titleMedium)
                                if (song.artist.isNotBlank()) {
                                    Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Tag(text = if (song.status == SongStatus.LEARNED) "Learned" else "Learning", filled = song.status == SongStatus.LEARNED)
                    }
                }
            }
        }
    }

    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            AddSongEditor(
                onSave = { title, artist ->
                    scope.launch {
                        container.guitarRepository.upsertSong(
                            GuitarSong(title = title, artist = artist, createdAtEpochMillis = System.currentTimeMillis())
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
private fun AddSongEditor(onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var newTitle by remember { mutableStateOf("") }
    var newArtist by remember { mutableStateOf("") }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Add a song", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape, value = newTitle, onValueChange = { newTitle = it }, label = { Text("Song title") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape, value = newArtist, onValueChange = { newArtist = it }, label = { Text("Artist (optional)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(onClick = {
                    if (newTitle.isNotBlank()) onSave(newTitle.trim(), newArtist.trim())
                }) { Text("Save") }
            }
        }
    }
}

@Composable
fun GuitarSongScreen(songId: Long, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val songs by container.guitarRepository.observeSongs().collectAsStateWithLifecycle(initialValue = emptyList())
    val song = songs.find { it.id == songId }
    val images by container.guitarRepository.observeTabImages(songId).collectAsStateWithLifecycle(initialValue = emptyList())
    val recordings by container.guitarRepository.observeRecordings(songId).collectAsStateWithLifecycle(initialValue = emptyList())

    val recorder = remember { AudioRecorder(context) }
    val player = remember { AudioPlayer() }
    var isRecording by remember { mutableStateOf(false) }
    var playingId by remember { mutableStateOf<Long?>(null) }

    DisposableEffect(Unit) { onDispose { player.stop() } }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch { container.guitarRepository.importTabImage(songId, uri) }
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = container.guitarRepository.newRecordingFile(songId)
            recorder.start(file)
            isRecording = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = song?.title ?: "Song", onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SongStatus.entries.forEach { status ->
                        Tag(
                            text = if (status == SongStatus.LEARNED) "Learned" else "Learning",
                            filled = song?.status == status,
                            modifier = Modifier.doromClickable { song?.let { scope.launch { container.guitarRepository.upsertSong(it.copy(status = status)) } } },
                        )
                    }
                }
            }

            item {
                Column {
                    SectionHeader(title = "Tabs")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(images) { image ->
                            LocalFileImage(
                                path = image.imagePath,
                                contentDescription = "Tab",
                                modifier = Modifier.size(96.dp).clip(CardShape),
                            )
                        }
                        item {
                            DoromCard(
                                modifier = Modifier.size(96.dp),
                                onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add tab photo")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    SectionHeader(title = "Recordings")
                    DoromCard(modifier = Modifier.fillMaxWidth(), onClick = {
                        if (isRecording) {
                            val file = recorder.stop()
                            isRecording = false
                            if (file != null) scope.launch { container.guitarRepository.saveRecording(songId, file) }
                        } else {
                            micPermission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(if (isRecording) "Stop recording" else "Record yourself playing", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    if (isRecording) {
                        RecordingWaveform(
                            isRecording = true,
                            getAmplitude = { recorder.currentAmplitude() },
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }

            items(recordings) { rec ->
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (playingId == rec.id) {
                                    player.stop(); playingId = null
                                } else {
                                    player.play(rec.audioPath) { playingId = null }
                                    playingId = rec.id
                                }
                            }) {
                                Icon(if (playingId == rec.id) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = "Play")
                            }
                            Text(formatDate(rec.recordedAtEpochMillis), style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { scope.launch { container.guitarRepository.deleteRecording(rec) } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.ENGLISH).format(Date(epochMillis))
