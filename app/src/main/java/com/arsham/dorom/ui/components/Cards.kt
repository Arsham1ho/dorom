package com.arsham.dorom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import com.arsham.dorom.ui.theme.InkSurface
import com.arsham.dorom.ui.theme.doromClickable

/** Flat, outlined-free card — no shadow, no gradient. A solid fill carries the "hand-drawn" feel. */
@Composable
fun DoromCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = CardShape,
    containerColor: Color = InkSurface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
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
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (filled) MaterialTheme.colorScheme.onPrimary else accent,
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
