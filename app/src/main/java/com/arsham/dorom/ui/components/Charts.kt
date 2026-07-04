package com.arsham.dorom.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.theme.SoftSpring
import kotlinx.coroutines.delay

/** Flat solid-fill bar chart. Bars grow in with a small stagger — no gradients. */
@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    height: androidx.compose.ui.unit.Dp = 120.dp,
) {
    val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(0.0001f)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(values) { visible = true }

    Row(
        modifier = modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { i, v ->
            val target = if (visible) (v / maxValue).coerceIn(0f, 1f) else 0f
            val animated by animateFloatAsState(target, SoftSpring, label = "bar$i")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(height - 20.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(((height - 20.dp).value * animated).dp),
                    ) {
                        drawRoundRect(
                            color = barColor,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                        )
                    }
                }
                if (i < labels.size) {
                    Text(
                        text = labels[i],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

/** Flat line + soft baseline fill trend chart, revealed left-to-right. */
@Composable
fun LineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    height: androidx.compose.ui.unit.Dp = 120.dp,
    minValue: Float = 0f,
    maxValue: Float = 100f,
) {
    var reveal by remember { mutableStateOf(0f) }
    LaunchedEffect(values) {
        reveal = 0f
        delay(80)
        reveal = 1f
    }
    val animatedReveal by animateFloatAsState(reveal, tween(700), label = "lineReveal")

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        if (values.size < 2) return@Canvas
        val range = (maxValue - minValue).coerceAtLeast(0.0001f)
        val stepX = size.width / (values.size - 1)
        val points = values.mapIndexed { i, v ->
            val x = i * stepX
            val normalized = ((v - minValue) / range).coerceIn(0f, 1f)
            val y = size.height - (normalized * size.height)
            Offset(x, y)
        }

        val visibleCount = (points.size * animatedReveal).coerceIn(0f, points.size.toFloat())
        val fullSegments = visibleCount.toInt()
        if (fullSegments < 1) return@Canvas

        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until fullSegments) {
            path.lineTo(points[i].x, points[i].y)
        }
        // partial last segment for a smooth reveal
        if (fullSegments < points.size) {
            val frac = visibleCount - fullSegments
            val a = points[fullSegments - 1]
            val b = points[fullSegments]
            path.lineTo(a.x + (b.x - a.x) * frac, a.y + (b.y - a.y) * frac)
        }

        val fillPath = androidx.compose.ui.graphics.Path().apply {
            addPath(path)
            lineTo(points[minOf(fullSegments, points.size - 1)].x, size.height)
            lineTo(points[0].x, size.height)
            close()
        }
        drawPath(fillPath, color = lineColor.copy(alpha = 0.14f))
        drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

        for (i in 0 until fullSegments) {
            drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = points[i])
        }
    }
}
