package com.arsham.dorom.ui.gym

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.data.entity.GymSession
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.EmptyState
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.BadgeBlue
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.Sage
import java.time.LocalDate

@Composable
fun GymHistoryScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val sessions by container.gymRepository.observeSessions().collectAsStateWithLifecycle(initialValue = emptyList())
    val finished = sessions.filter { it.endEpochMillis != null }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Gym history", onBack = onBack)

        if (finished.isEmpty()) {
            EmptyState(icon = Icons.Filled.History, title = "No sessions yet", subtitle = "Finish a workout and it'll show up here.")
        }

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(finished) { session -> SessionRow(session) }
        }
    }
}

@Composable
private fun SessionRow(session: GymSession) {
    val durationSec = ((session.endEpochMillis ?: session.startEpochMillis) - session.startEpochMillis) / 1000
    val h = durationSec / 3600
    val m = (durationSec % 3600) / 60
    val durationLabel = if (h > 0) "${h}h ${m}m" else "${m}m"
    val locationLabel = if (session.location == GymLocation.GYM) "Gym" else "Home Gym"

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBadge(icon = Icons.Filled.History, tint = BadgeBlue)
            Column {
                Text(runCatching { LocalDate.parse(session.date) }.getOrNull()?.toString() ?: session.date, style = MaterialTheme.typography.titleMedium)
                Text("$locationLabel · $durationLabel", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
