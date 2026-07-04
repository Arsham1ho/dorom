package com.arsham.dorom.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.repository.JournalEntryPlain
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.TopBarWithBack
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonalJournalScreen(onBack: () -> Unit) {
    var unlocked by remember { mutableStateOf(false) }
    val unlock = rememberBiometricUnlock(onSuccess = { unlocked = true })

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Personal journal", onBack = onBack)

        if (!unlocked) {
            LockedPlaceholder(modifier = Modifier.weight(1f), onUnlock = unlock)
        } else {
            UnlockedJournal(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LockedPlaceholder(modifier: Modifier = Modifier, onUnlock: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyState(icon = Icons.Filled.Lock, title = "Locked", subtitle = "Authenticate to see your private entries.")
        Button(onClick = onUnlock) { Text("Unlock") }
    }
}

@Composable
private fun UnlockedJournal(modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val entries by container.journalRepository.observeEntries().collectAsStateWithLifecycle(initialValue = emptyList())
    var draft by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            DoromCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        label = { Text("What's on your mind?") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                    )
                    Button(
                        onClick = {
                            if (draft.isNotBlank()) {
                                scope.launch { container.journalRepository.saveEntry(null, draft.trim()) }
                                draft = ""
                            }
                        },
                    ) { Text("Add entry") }
                }
            }
        }
        items(entries) { entry ->
            JournalEntryCard(entry, onDelete = { scope.launch { container.journalRepository.deleteEntry(entry) } })
        }
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntryPlain, onDelete: () -> Unit) {
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatDate(entry.createdAtEpochMillis), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(entry.text, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("EEE, MMM d · HH:mm", Locale.ENGLISH).format(Date(epochMillis))
