package com.arsham.dorom.ui.gym

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import com.arsham.dorom.R
import com.arsham.dorom.data.entity.GymLocation

/** Real reference photo for a category, when one has been supplied — falls back to a drawn glyph otherwise. */
fun categoryImageRes(category: String): Int? = when (category) {
    "Chest" -> R.drawable.gym_chest
    "Back" -> R.drawable.gym_back
    "Shoulder" -> R.drawable.gym_shoulder
    "Bicep" -> R.drawable.gym_bicep
    "Triceps" -> R.drawable.gym_triceps
    "Forearm" -> R.drawable.gym_forearm
    "Leg" -> R.drawable.gym_leg
    "Abs" -> R.drawable.gym_abs
    "Cardio" -> R.drawable.gym_cardio
    else -> null
}

/**
 * Small custom silhouette drawings standing in for exercise photography, which a fully local,
 * personal app has no library of. Each category/location gets a genuinely distinct shape rather
 * than one generic icon reused everywhere.
 */
@Composable
fun MuscleGlyph(category: String, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        when (category) {
            "Chest" -> drawChest(tint, w, h)
            "Back" -> drawBack(tint, w, h)
            "Shoulder" -> drawShoulder(tint, w, h)
            "Bicep" -> drawBicep(tint, w, h)
            "Triceps" -> drawTriceps(tint, w, h)
            "Forearm" -> drawForearm(tint, w, h)
            "Leg" -> drawLeg(tint, w, h)
            "Abs" -> drawAbs(tint, w, h)
            "Cardio" -> drawCardio(tint, w, h)
            "Sport" -> drawSport(tint, w, h)
            else -> drawChest(tint, w, h)
        }
    }
}

@Composable
fun GymLocationGlyph(location: GymLocation, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (location == GymLocation.GYM) drawBarbell(tint, w, h) else drawHomeGym(tint, w, h)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChest(tint: Color, w: Float, h: Float) {
    drawCircle(tint, radius = 0.09f * w, center = Offset(0.5f * w, 0.14f * h))
    drawRoundRect(tint, topLeft = Offset(0.28f * w, 0.24f * h), size = Size(0.44f * w, 0.42f * h), cornerRadius = CornerRadius(0.1f * w))
    drawCircle(tint, radius = 0.1f * w, center = Offset(0.36f * w, 0.36f * h))
    drawCircle(tint, radius = 0.1f * w, center = Offset(0.64f * w, 0.36f * h))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBack(tint: Color, w: Float, h: Float) {
    drawCircle(tint, radius = 0.09f * w, center = Offset(0.5f * w, 0.14f * h))
    val path = Path().apply {
        moveTo(0.22f * w, 0.26f * h)
        lineTo(0.78f * w, 0.26f * h)
        lineTo(0.62f * w, 0.68f * h)
        lineTo(0.38f * w, 0.68f * h)
        close()
    }
    drawPath(path, tint)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShoulder(tint: Color, w: Float, h: Float) {
    drawCircle(tint, radius = 0.08f * w, center = Offset(0.5f * w, 0.16f * h))
    drawRoundRect(tint, topLeft = Offset(0.4f * w, 0.24f * h), size = Size(0.2f * w, 0.14f * h), cornerRadius = CornerRadius(0.04f * w))
    drawCircle(tint, radius = 0.14f * w, center = Offset(0.26f * w, 0.32f * h))
    drawCircle(tint, radius = 0.14f * w, center = Offset(0.74f * w, 0.32f * h))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBicep(tint: Color, w: Float, h: Float) {
    drawCircle(tint, radius = 0.09f * w, center = Offset(0.62f * w, 0.14f * h))
    rotate(degrees = 20f, pivot = Offset(0.42f * w, 0.4f * h)) {
        drawRoundRect(tint, topLeft = Offset(0.32f * w, 0.24f * h), size = Size(0.2f * w, 0.34f * h), cornerRadius = CornerRadius(0.1f * w))
    }
    rotate(degrees = -35f, pivot = Offset(0.42f * w, 0.4f * h)) {
        drawRoundRect(tint, topLeft = Offset(0.34f * w, 0.4f * h), size = Size(0.16f * w, 0.3f * h), cornerRadius = CornerRadius(0.08f * w))
    }
    drawCircle(tint, radius = 0.09f * w, center = Offset(0.24f * w, 0.24f * h))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTriceps(tint: Color, w: Float, h: Float) {
    drawCircle(tint, radius = 0.09f * w, center = Offset(0.5f * w, 0.12f * h))
    drawRoundRect(tint, topLeft = Offset(0.4f * w, 0.2f * h), size = Size(0.2f * w, 0.34f * h), cornerRadius = CornerRadius(0.1f * w))
    drawRoundRect(tint, topLeft = Offset(0.38f * w, 0.52f * h), size = Size(0.24f * w, 0.3f * h), cornerRadius = CornerRadius(0.1f * w))
    drawCircle(tint, radius = 0.11f * w, center = Offset(0.5f * w, 0.86f * h))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawForearm(tint: Color, w: Float, h: Float) {
    drawRoundRect(tint, topLeft = Offset(0.4f * w, 0.12f * h), size = Size(0.2f * w, 0.4f * h), cornerRadius = CornerRadius(0.09f * w))
    drawCircle(tint, radius = 0.22f * w, center = Offset(0.5f * w, 0.68f * h))
    val fingerY = 0.5f * h
    for (i in -1..1) {
        drawRoundRect(
            tint,
            topLeft = Offset((0.5f + i * 0.1f) * w - 0.03f * w, fingerY),
            size = Size(0.06f * w, 0.14f * h),
            cornerRadius = CornerRadius(0.03f * w),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeg(tint: Color, w: Float, h: Float) {
    drawRoundRect(tint, topLeft = Offset(0.34f * w, 0.08f * h), size = Size(0.32f * w, 0.44f * h), cornerRadius = CornerRadius(0.14f * w))
    drawRoundRect(tint, topLeft = Offset(0.38f * w, 0.5f * h), size = Size(0.24f * w, 0.36f * h), cornerRadius = CornerRadius(0.1f * w))
    drawRoundRect(tint, topLeft = Offset(0.36f * w, 0.86f * h), size = Size(0.3f * w, 0.1f * h), cornerRadius = CornerRadius(0.04f * w))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAbs(tint: Color, w: Float, h: Float) {
    drawCircle(tint, radius = 0.09f * w, center = Offset(0.5f * w, 0.12f * h))
    drawRoundRect(
        color = tint,
        topLeft = Offset(0.3f * w, 0.22f * h),
        size = Size(0.4f * w, 0.62f * h),
        cornerRadius = CornerRadius(0.08f * w),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.025f * w),
    )
    for (row in 0..2) {
        for (col in 0..1) {
            drawRoundRect(
                tint,
                topLeft = Offset((0.35f + col * 0.18f) * w, (0.28f + row * 0.18f) * h),
                size = Size(0.12f * w, 0.12f * h),
                cornerRadius = CornerRadius(0.03f * w),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCardio(tint: Color, w: Float, h: Float) {
    val path = Path().apply {
        moveTo(0.5f * w, 0.82f * h)
        cubicTo(0.1f * w, 0.5f * h, 0.14f * w, 0.16f * h, 0.5f * w * 0.62f, 0.2f * h)
        cubicTo(0.42f * w, 0.05f * h, 0.58f * w, 0.05f * h, 0.5f * w, 0.2f * h)
        cubicTo(0.72f * w, 0.05f * h, 0.98f * w, 0.2f * h, 0.5f * w, 0.82f * h)
        close()
    }
    drawPath(path, tint)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSport(tint: Color, w: Float, h: Float) {
    // A simple five-point star standing in for "general sport/activity".
    val cx = 0.5f * w
    val cy = 0.52f * h
    val outerR = 0.42f * minOf(w, h)
    val innerR = outerR * 0.42f
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = Math.PI / 2 + i * Math.PI / 5
        val x = cx + (r * kotlin.math.cos(angle)).toFloat()
        val y = cy - (r * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, tint)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBarbell(tint: Color, w: Float, h: Float) {
    val barY = 0.5f * h
    drawRoundRect(tint, topLeft = Offset(0.1f * w, barY - 0.025f * h), size = Size(0.8f * w, 0.05f * h), cornerRadius = CornerRadius(0.02f * w))
    for (side in listOf(0.12f, 0.22f)) {
        drawRoundRect(tint, topLeft = Offset(side * w, barY - 0.28f * h), size = Size(0.09f * w, 0.56f * h), cornerRadius = CornerRadius(0.03f * w))
    }
    for (side in listOf(0.66f, 0.79f)) {
        drawRoundRect(tint, topLeft = Offset(side * w, barY - 0.28f * h), size = Size(0.09f * w, 0.56f * h), cornerRadius = CornerRadius(0.03f * w))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHomeGym(tint: Color, w: Float, h: Float) {
    val roof = Path().apply {
        moveTo(0.5f * w, 0.1f * h)
        lineTo(0.86f * w, 0.42f * h)
        lineTo(0.14f * w, 0.42f * h)
        close()
    }
    drawPath(roof, tint)
    drawRoundRect(tint, topLeft = Offset(0.22f * w, 0.4f * h), size = Size(0.56f * w, 0.44f * h), cornerRadius = CornerRadius(0.02f * w))
    val barY = 0.64f * h
    drawRoundRect(Color.White, topLeft = Offset(0.32f * w, barY - 0.02f * h), size = Size(0.36f * w, 0.04f * h), cornerRadius = CornerRadius(0.02f * w))
    drawCircle(Color.White, radius = 0.07f * w, center = Offset(0.34f * w, barY))
    drawCircle(Color.White, radius = 0.07f * w, center = Offset(0.66f * w, barY))
}
