package com.arsham.dorom.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arsham.dorom.ui.theme.doromClickable

private fun iconFor(route: String): ImageVector = when (route) {
    Routes.HOME -> Icons.Filled.Home
    Routes.PLAN -> Icons.Filled.CalendarMonth
    Routes.TRACK -> Icons.Filled.Insights
    Routes.JOURNAL -> Icons.Filled.AutoStories
    Routes.MOOD_REPORT -> Icons.Filled.Mood
    else -> Icons.Filled.Home
}

@Composable
fun DoromBottomBar(currentRoute: String?, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    // Background spans the full width including behind the gesture nav area; only the
    // tappable row content is inset above it, so there's no dead black strip at the bottom.
    Column(modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
        ) {
            BottomDestinations.forEach { dest ->
                val selected = currentRoute == dest.route
                BottomBarItem(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = iconFor(dest.route),
                    label = dest.label,
                    selected = selected,
                    // Re-navigating to the already-selected tab is a harmless no-op, but always
                    // routing through the same call keeps behavior identical for every tab —
                    // no special-casing that could silently swallow a tap.
                    onClick = { onNavigate(dest.route) },
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(modifier: Modifier, icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    // Spotify-style flat tab: no highlight background, just an icon/label color change on select.
    // The clickable area fills this entire weighted slot (not just the wrapped icon+label),
    // so there's no dead space between tabs where a tap could silently miss.
    Column(
        modifier = modifier
            .padding(horizontal = 3.dp, vertical = 6.dp)
            .doromClickable(onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
