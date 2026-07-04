package com.arsham.dorom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.theme.ChipShape
import com.arsham.dorom.ui.theme.doromClickable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Tappable "HH:mm" chip that opens a Material3 TimePicker dialog. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(label: String, time: String, onTimeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var showDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val (h, m) = time.split(":").map { it.toInt() }

    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(ChipShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .doromClickable { showDialog = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(time, style = com.arsham.dorom.ui.theme.DataText.medium)
        }
    }

    if (showDialog) {
        val state = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange("%02d:%02d".format(state.hour, state.minute))
                    showDialog = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
            text = { TimePicker(state = state) },
        )
    }
}

/** Tappable date chip that opens a Material3 DatePicker dialog. Null means "not set". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, date: LocalDate?, onDateChange: (LocalDate?) -> Unit, modifier: Modifier = Modifier) {
    var showDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(ChipShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .doromClickable { showDialog = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(date?.toString() ?: "Not set", style = com.arsham.dorom.ui.theme.DataText.small)
        }
    }

    if (showDialog) {
        val state: DatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        )
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    onDateChange(
                        millis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                    )
                    showDialog = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
            text = { DatePicker(state = state) },
        )
    }
}
