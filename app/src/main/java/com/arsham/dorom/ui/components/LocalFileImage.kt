package com.arsham.dorom.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

/** Minimal local-file image loader — no third-party image library needed for on-device photos. */
@Composable
fun LocalFileImage(path: String, contentDescription: String?, modifier: Modifier = Modifier) {
    val bitmapState = produceState<android.graphics.Bitmap?>(initialValue = null, path) {
        value = runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }
    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        androidx.compose.foundation.layout.Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    }
}
