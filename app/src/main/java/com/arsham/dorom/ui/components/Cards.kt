package com.arsham.dorom.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.ChipShape
import com.arsham.dorom.ui.theme.doromClickable

/** Flat, borderless card — separation comes from surface-color contrast alone, no outline, no shadow. */
@Composable
fun DoromCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = CardShape,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .then(if (borderColor != Color.Unspecified) Modifier.border(BorderStroke(1.5.dp, borderColor), shape) else Modifier)
            .then(if (onClick != null) Modifier.doromClickable(onClick) else Modifier)
            .padding(contentPadding)
    ) {
        content()
    }
}

/** Sharp-cornered tag/chip — deliberately different shape language from cards. */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    filled: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(ChipShape)
            .background(if (filled) accent else Color.Transparent)
            .then(if (!filled) Modifier.border(BorderStroke(1.dp, accent.copy(alpha = 0.5f)), ChipShape) else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (filled) MaterialTheme.colorScheme.onPrimary else accent,
            maxLines = 1,
        )
    }
}

/** Press-scaled tappable surface without Material's default ripple. */
@Composable
fun PressableSurface(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.doromClickable(onClick)) {
        content()
    }
}

/** Icon rendered inside a solid tinted rounded-square badge — used across hub tiles/quick links for visual weight. */
@Composable
fun IconBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.28f)),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}
