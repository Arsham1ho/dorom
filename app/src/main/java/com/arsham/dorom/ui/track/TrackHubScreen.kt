package com.arsham.dorom.ui.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.navigation.Routes

private data class TrackTile(val icon: ImageVector, val label: String, val subtitle: String, val route: String)

private val tiles = listOf(
    TrackTile(Icons.Filled.MenuBook, "Courses", "Lessons & progress", Routes.TRACK_COURSES),
    TrackTile(Icons.Filled.Timelapse, "Time markers", "Countdowns & count-ups", Routes.TRACK_TIME_MARKERS),
    TrackTile(Icons.Filled.FitnessCenter, "Gym", "Workout plan", Routes.TRACK_GYM),
    TrackTile(Icons.Filled.MusicNote, "Guitar", "Songs, tabs & recordings", Routes.TRACK_GUITAR),
    TrackTile(Icons.Filled.Savings, "Finance", "Income, expenses & saving", Routes.TRACK_FINANCE),
)

@Composable
fun TrackHubScreen(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Track",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp, 20.dp, 16.dp, 8.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tiles) { tile ->
                DoromCard(onClick = { onNavigate(tile.route) }, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Icon(tile.icon, contentDescription = tile.label, tint = MaterialTheme.colorScheme.primary)
                        Text(tile.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
                        Text(tile.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
