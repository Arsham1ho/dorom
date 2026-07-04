package com.arsham.dorom.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.DailyReview
import com.arsham.dorom.data.entity.TimeDirection
import com.arsham.dorom.data.entity.TimeMarker
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.LineChart
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.PressableSurface
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.navigation.Routes
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.util.pretty
import com.arsham.dorom.util.todayString
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val container = LocalAppContainer.current
    val today = todayString()

    val tasks by container.planRepository.observeTasks(today).collectAsStateWithLifecycle(initialValue = emptyList())
    val recentReviews by container.reviewRepository.observeRecentReviews(14).collectAsStateWithLifecycle(initialValue = emptyList())
    val markers by container.timeMarkerRepository.observeMarkers().collectAsStateWithLifecycle(initialValue = emptyList())

    val completed = tasks.count { it.isDone }
    val plannedCount = tasks.size
    val progress = if (plannedCount == 0) 0f else completed.toFloat() / plannedCount
    val streak = computeStreak(recentReviews)
    val upcomingMarkers = markers.sortedBy { kotlin.math.abs(it.targetEpochMillis - System.currentTimeMillis()) }.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Dorom", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        LocalDate.now().pretty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PressableSurface(onClick = { onNavigate(Routes.SETTINGS) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            DoromCard(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate(Routes.PLAN) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProgressRing(percent = progress, size = 84.dp)
                    Column {
                        Text("Today's plan", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (plannedCount == 0) "Nothing planned yet — set tonight's plan." else "$completed of $plannedCount tasks done",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (streak > 0) {
                            Text(
                                "🔥 $streak day streak",
                                style = MaterialTheme.typography.labelMedium,
                                color = Sage,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        item {
            DoromCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    SectionHeader(title = "Score trend")
                    val scores = recentReviews.reversed().map { it.score.toFloat() }
                    if (scores.size < 2) {
                        Text(
                            "Finish a couple of days and your trend shows up here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LineChart(values = scores, minValue = 0f, maxValue = 100f)
                    }
                }
            }
        }

        if (upcomingMarkers.isNotEmpty()) {
            item {
                Column {
                    SectionHeader(title = "Time markers")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        upcomingMarkers.forEach { marker -> TimeMarkerMini(marker) }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Quick links")
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(quickLinks) { link ->
                    QuickLinkCard(link) { onNavigate(link.route) }
                }
            }
        }
    }
}

private data class QuickLink(val icon: ImageVector, val label: String, val route: String)

private val quickLinks = listOf(
    QuickLink(Icons.Filled.Flag, "Goals", Routes.GOALS),
    QuickLink(Icons.Filled.FitnessCenter, "Gym", Routes.TRACK_GYM),
    QuickLink(Icons.Filled.MusicNote, "Guitar", Routes.TRACK_GUITAR),
    QuickLink(Icons.Filled.Savings, "Finance", Routes.TRACK_FINANCE),
    QuickLink(Icons.Filled.Timelapse, "Markers", Routes.TRACK_TIME_MARKERS),
)

@Composable
private fun QuickLinkCard(link: QuickLink, onClick: () -> Unit) {
    DoromCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(link.icon, contentDescription = link.label, tint = MaterialTheme.colorScheme.primary)
            Text(link.label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun TimeMarkerMini(marker: TimeMarker) {
    DoromCard {
        Column {
            Text(marker.title, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            val now = System.currentTimeMillis()
            val days = ChronoUnit.DAYS.between(
                Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate(),
                Instant.ofEpochMilli(marker.targetEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate(),
            )
            val label = when {
                marker.direction == TimeDirection.COUNTDOWN && days >= 0 -> "${days}d left"
                else -> "${kotlin.math.abs(days)}d ago"
            }
            Text(label, style = DataText.medium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun computeStreak(reviews: List<DailyReview>): Int {
    if (reviews.isEmpty()) return 0
    val sorted = reviews.sortedByDescending { it.date }
    var streak = 0
    for (review in sorted) {
        if (review.percentage >= 50) streak++ else break
    }
    return streak
}
