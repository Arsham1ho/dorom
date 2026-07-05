package com.arsham.dorom.ui.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.ui.components.LocalFileImage
import com.arsham.dorom.ui.theme.CardShape

/** Bold-italic display header, matching a stripped-down "N exercises" style rather than a boxed button. */
@Composable
fun GymSectionHeader(title: String, onAdd: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.ExtraBold),
        )
        if (onAdd != null) {
            IconButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = "Add") }
        }
    }
}

/** Square exercise thumbnail with a small tinted category dot overlapping its bottom-right corner. */
@Composable
fun ExerciseThumb(exercise: GymExercise, tint: Color, size: Dp = 56.dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size)) {
        if (exercise.imagePath != null) {
            LocalFileImage(path = exercise.imagePath, contentDescription = exercise.name, modifier = Modifier.size(size).clip(CardShape))
        } else {
            Box(
                modifier = Modifier.size(size).clip(CardShape).background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                MuscleGlyph(category = exercise.category, tint = tint, modifier = Modifier.size(size * 0.62f))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(size * 0.4f)
                .clip(CardShape)
                .background(tint),
        )
    }
}

/** "3 sets • 10 reps • 45 lb" style subtitle, omitting the weight segment when it's zero. */
fun prescriptionLabel(sets: Int, reps: Int, weight: Double): String {
    val base = "$sets sets • $reps reps"
    if (weight <= 0) return base
    val weightText = if (weight == weight.toLong().toDouble()) weight.toLong().toString() else weight.toString()
    return "$base • $weightText lb"
}
