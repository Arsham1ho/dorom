package com.arsham.dorom.ui.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.navigation.Routes
import com.arsham.dorom.ui.theme.BadgeBlue
import com.arsham.dorom.ui.theme.BadgeGold
import com.arsham.dorom.ui.theme.BadgeViolet
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.ui.theme.Terracotta

private data class TrackTile(val icon: ImageVector, val label: String, val subtitle: String, val route: String, val tint: Color)

private val tiles = listOf(
    TrackTile(Icons.AutoMirrored.Filled.MenuBook, "Courses", "Lessons & progress", Routes.TRACK_COURSES, BadgeBlue),
    TrackTile(Icons.Filled.Timelapse, "Time markers", "Countdowns & count-ups", Routes.TRACK_TIME_MARKERS, BadgeGold),
    TrackTile(Icons.Filled.FitnessCenter, "Gym", "Workout plan", Routes.TRACK_GYM, Terracotta),
    TrackTile(Icons.Filled.MusicNote, "Guitar", "Songs, tabs & recordings", Routes.TRACK_GUITAR, BadgeViolet),
    TrackTile(Icons.Filled.Savings, "Finance", "Income, expenses & saving", Routes.TRACK_FINANCE, Sage),
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
                DoromCard(
                    onClick = { onNavigate(tile.route) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        IconBadge(icon = tile.icon, tint = tile.tint)
                        Text(tile.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                        Text(tile.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
