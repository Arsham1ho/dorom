package com.arsham.dorom.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Deliberately mixed shape language: sharp corners for tags/chips/data readouts,
// soft corners for content cards. The contrast is the point.
val SharpShape = RoundedCornerShape(2.dp)
val ChipShape = RoundedCornerShape(4.dp)
val CardShape = RoundedCornerShape(18.dp)
val CardShapeLarge = RoundedCornerShape(24.dp)
val PillShape = RoundedCornerShape(50)

val DoromShapes = Shapes(
    extraSmall = SharpShape,
    small = ChipShape,
    medium = CardShape,
    large = CardShapeLarge,
    extraLarge = RoundedCornerShape(28.dp),
)
