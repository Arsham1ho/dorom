package com.arsham.dorom.ui.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.navigation.Routes

private data class JournalTile(val icon: ImageVector, val title: String, val subtitle: String, val route: String)

private val tiles = listOf(
    JournalTile(Icons.Filled.RateReview, "Daily feedback", "How today went, in your words", Routes.FEEDBACK),
    JournalTile(Icons.Filled.Lock, "Personal journal", "Locked — just for you", Routes.PERSONAL_JOURNAL),
    JournalTile(Icons.Filled.History, "History", "Past days, scores & plans", Routes.HISTORY),
)

@Composable
fun JournalHubScreen(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Journal", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(16.dp, 20.dp, 16.dp, 8.dp))
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tiles) { tile ->
                DoromCard(onClick = { onNavigate(tile.route) }, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(tile.icon, contentDescription = tile.title, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(tile.title, style = MaterialTheme.typography.titleMedium)
                            Text(tile.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
