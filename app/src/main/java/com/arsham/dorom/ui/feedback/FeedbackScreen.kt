package com.arsham.dorom.ui.feedback

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.RecordingWaveform
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.util.AudioPlayer
import com.arsham.dorom.util.AudioRecorder
import com.arsham.dorom.util.todayString
import kotlinx.coroutines.launch

@Composable
fun FeedbackScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val today = todayString()

    val review by container.reviewRepository.observeReview(today).collectAsStateWithLifecycle(initialValue = null)
    var text by remember(review?.date) { mutableStateOf(review?.feedbackText ?: "") }

    val recorder = remember { AudioRecorder(context) }
    val player = remember { AudioPlayer() }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { player.stop() } }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            recorder.start(container.reviewRepository.newFeedbackAudioFile(today))
            isRecording = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Daily feedback", onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProgressRing(percent = (review?.percentage ?: 0) / 100f, size = 72.dp)
                        Column {
                            Text("Today's score", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${review?.score ?: 0}",
                                style = DataText.large,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "${review?.completedCount ?: 0} of ${review?.plannedCount ?: 0} tasks done",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    SectionHeader(title = "In your words")
                    OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape,
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        label = { Text("How did today go?") },
                    )
                    Button(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = {
                            focusManager.clearFocus()
                            scope.launch {
                                container.reviewRepository.saveFeedback(today, text, null)
                                onBack()
                            }
                        },
                    ) { Text("Save") }
                }
            }

            item {
                Column {
                    SectionHeader(title = "Or say it out loud")
                    DoromCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (isRecording) {
                                val file = recorder.stop()
                                isRecording = false
                                if (file != null) scope.launch { container.reviewRepository.saveFeedback(today, text, file) }
                            } else {
                                micPermission.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(if (isRecording) "Stop recording" else "Record voice feedback", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    if (isRecording) {
                        RecordingWaveform(
                            isRecording = true,
                            getAmplitude = { recorder.currentAmplitude() },
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                    review?.feedbackAudioPath?.let { path ->
                        DoromCard(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            onClick = {
                                if (isPlaying) {
                                    player.stop(); isPlaying = false
                                } else {
                                    player.play(path) { isPlaying = false }
                                    isPlaying = true
                                }
                            },
                        ) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
                                Text("Play recorded feedback")
                            }
                        }
                    }
                }
            }
        }
    }
}
