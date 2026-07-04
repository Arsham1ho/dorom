package com.arsham.dorom.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.SoftSpring
import kotlin.math.roundToInt

/**
 * Animated flat progress ring. No gradient sweep — a single solid arc, thick stroke, sharp cap.
 */
@Composable
fun ProgressRing(
    percent: Float, // 0f..1f
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 96.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    centerLabel: String? = null,
) {
    val animated by animateFloatAsState(percent.coerceIn(0f, 1f), SoftSpring, label = "ringProgress")
    Box(modifier = modifier.size(size), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val inset = strokeWidth.toPx() / 2
            val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
        }
        val label = centerLabel ?: "${(animated * 100).roundToInt()}%"
        Text(text = label, style = DataText.medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
