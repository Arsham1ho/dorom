package com.arsham.dorom.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
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
    else -> Icons.Filled.Home
}

@Composable
fun DoromBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BottomDestinations.forEach { dest ->
                val selected = currentRoute == dest.route
                BottomBarItem(
                    icon = iconFor(dest.route),
                    label = dest.label,
                    selected = selected,
                    onClick = { onNavigate(dest.route) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else androidx.compose.ui.graphics.Color.Transparent
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .doromClickable(onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
