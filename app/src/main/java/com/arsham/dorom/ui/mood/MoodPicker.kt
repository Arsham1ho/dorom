package com.arsham.dorom.ui.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsham.dorom.ui.theme.doromClickable

val MOOD_OPTIONS = listOf("🤘🏻", "😄", "🙃", "😭", "😕", "🥴", "🤬")

/**
 * Tap an emoji to select it, optionally type a short note, then confirm to log it.
 * The note is never required — confirming with it blank just logs the mood alone.
 */
@Composable
fun MoodPickerRow(onLog: (emoji: String, note: String) -> Unit, modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MOOD_OPTIONS.forEach { emoji ->
                val isSelected = selected == emoji
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else androidx.compose.ui.graphics.Color.Transparent)
                        .then(
                            if (isSelected) Modifier.border(androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape)
                            else Modifier
                        )
                        .doromClickable { selected = if (isSelected) null else emoji },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
            }
        }

        selected?.let { emoji ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a note (optional)") },
                    singleLine = true,
                )
                IconButton(onClick = {
                    onLog(emoji, note)
                    selected = null
                    note = ""
                }) {
                    Icon(Icons.Filled.Check, contentDescription = "Log mood", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
