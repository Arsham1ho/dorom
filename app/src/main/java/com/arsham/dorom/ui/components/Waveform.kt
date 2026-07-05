package com.arsham.dorom.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.DangerRed
import com.arsham.dorom.ui.theme.rememberPulse
import kotlinx.coroutines.delay

private const val MAX_BARS = 45
private const val SAMPLE_INTERVAL_MS = 90L

/**
 * Live recording indicator: a pulsing rec dot, an elapsed timer, and a scrolling bar waveform
 * driven by real mic amplitude — the same shape language as the rest of the app (flat bars,
 * no gradient), just animated in real time like a proper recording app.
 */
@Composable
fun RecordingWaveform(
    isRecording: Boolean,
    getAmplitude: () -> Int,
    modifier: Modifier = Modifier,
) {
    val samples = remember { mutableStateListOf<Float>() }
    var elapsedMillis by remember { mutableStateOf(0L) }

    LaunchedEffect(isRecording) {
        if (!isRecording) {
            samples.clear()
            elapsedMillis = 0L
            return@LaunchedEffect
        }
        val start = System.currentTimeMillis()
        while (true) {
            val amp = getAmplitude()
            val normalized = (amp / 32767f).coerceIn(0f, 1f)
            samples.add(normalized)
            if (samples.size > MAX_BARS) samples.removeAt(0)
            elapsedMillis = System.currentTimeMillis() - start
            delay(SAMPLE_INTERVAL_MS)
        }
    }

    if (!isRecording) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PulsingRecDot()
            Text(formatElapsed(elapsedMillis), style = DataText.medium, color = MaterialTheme.colorScheme.onSurface)
        }
        WaveformBars(
            samples = samples,
            modifier = Modifier.fillMaxWidth().height(44.dp).padding(top = 8.dp),
        )
    }
}

@Composable
fun PulsingRecDot() {
    val pulse = rememberPulse()
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .alpha(0.5f + 0.5f * pulse)
            .background(DangerRed),
    )
}

@Composable
private fun WaveformBars(samples: List<Float>, modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        if (samples.isEmpty()) return@Canvas
        val slotWidth = size.width / MAX_BARS
        val centerY = size.height / 2
        val barWidth = (slotWidth * 0.5f).coerceAtLeast(2.dp.toPx())
        val startIndex = MAX_BARS - samples.size

        samples.forEachIndexed { i, amplitude ->
            val x = (startIndex + i) * slotWidth + slotWidth / 2
            val halfHeight = (size.height / 2) * amplitude.coerceIn(0.06f, 1f)
            drawLine(
                color = barColor,
                start = Offset(x, centerY - halfHeight),
                end = Offset(x, centerY + halfHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
