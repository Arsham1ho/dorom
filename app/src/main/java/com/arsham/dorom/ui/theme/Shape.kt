package com.arsham.dorom.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Spotify-style shape language: fully rounded pill chips, no sharp corners anywhere.
val SharpShape = RoundedCornerShape(2.dp)
val PillShape = RoundedCornerShape(50)
val ChipShape = PillShape
val CardShape = RoundedCornerShape(12.dp)
val CardShapeLarge = RoundedCornerShape(20.dp)

// Text fields default to MaterialTheme.shapes.extraSmall (our sharp chip shape) unless given
// their own shape explicitly — every OutlinedTextField in the app passes this one instead.
val InputShape = RoundedCornerShape(14.dp)

val DoromShapes = Shapes(
    extraSmall = SharpShape,
    small = ChipShape,
    medium = CardShape,
    large = CardShapeLarge,
    extraLarge = RoundedCornerShape(28.dp),
)
