package com.arsham.dorom.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val SnappySpring = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
val SoftSpring = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)

@Composable
fun rememberPressScale(interactionSource: MutableInteractionSource): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, SnappySpring, label = "pressScale")
    return scale
}

/** Press-scale interaction: shrinks slightly on press, springs back. Use instead of a ripple-only clickable. */
@Composable
fun Modifier.doromClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

/** Staggered fade + rise entrance for list items. Call once per item with its index. */
@Composable
fun Modifier.staggeredEntrance(index: Int, baseDelayMs: Int = 40): Modifier {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(index) {
        delay((index * baseDelayMs).toLong())
        visible = true
    }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(280), label = "entranceAlpha")
    val offsetPx by animateFloatAsState(if (visible) 0f else 24f, SoftSpring, label = "entranceOffset")
    val density = LocalDensity.current
    return this.graphicsLayer {
        this.alpha = alpha
        translationY = with(density) { offsetPx.dp.toPx() }
    }
}

/** Continuous gentle pulse, e.g. for a live countdown number. */
@Composable
fun rememberPulse(): Float {
    val anim = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400),
                repeatMode = RepeatMode.Reverse,
            ),
        )
    }
    return anim.value
}
