package com.arsham.dorom.ui.gym

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.GYM_CATEGORIES
import com.arsham.dorom.data.entity.GymExercise
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.navigation.Routes
import com.arsham.dorom.ui.theme.BadgeBlue
import com.arsham.dorom.ui.theme.BadgeGold
import com.arsham.dorom.ui.theme.BadgeViolet
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.ui.theme.Terracotta
import com.arsham.dorom.util.todayString

fun categoryTint(index: Int): Color = listOf(Terracotta, BadgeBlue, BadgeViolet, BadgeGold, Sage)[index % 5]
fun categoryTint(category: String): Color = categoryTint(GYM_CATEGORIES.indexOf(category).coerceAtLeast(0))

@Composable
fun GymScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val container = LocalAppContainer.current
    var selectedLocation by remember { mutableStateOf<GymLocation?>(null) }

    BackHandler(enabled = selectedLocation != null) { selectedLocation = null }

    val today = remember { todayString() }
    val todaySchedule by container.gymRepository.observeSchedule(today).collectAsStateWithLifecycle(initialValue = emptyList())
    var todayExercises by remember { mutableStateOf<List<GymExercise>>(emptyList()) }
    LaunchedEffect(todaySchedule) { todayExercises = container.gymRepository.getScheduledExercises(today) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(
            title = if (selectedLocation == null) "Gym" else if (selectedLocation == GymLocation.GYM) "Gym" else "Home Gym",
            onBack = { if (selectedLocation != null) selectedLocation = null else onBack() },
        )

        val location = selectedLocation
        if (location == null) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (todayExercises.isNotEmpty()) {
                    item {
                        DoromCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("Today's workout", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${todayExercises.size} exercise${if (todayExercises.size == 1) "" else "s"} planned",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                androidx.compose.material3.Button(
                                    modifier = Modifier.padding(top = 10.dp),
                                    onClick = { onNavigate(Routes.gymSession(todayExercises.first().location.name)) },
                                ) {
                                    Text("Let's GO! 💪")
                                }
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LocationCard(
                            location = GymLocation.GYM,
                            label = "Gym",
                            tint = Terracotta,
                            onClick = { selectedLocation = GymLocation.GYM },
                            modifier = Modifier.weight(1f),
                        )
                        LocationCard(
                            location = GymLocation.HOME_GYM,
                            label = "Home Gym",
                            tint = Sage,
                            onClick = { selectedLocation = GymLocation.HOME_GYM },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    DoromCard(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate(Routes.GYM_HISTORY) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            IconBadge(icon = Icons.Filled.History, tint = BadgeBlue)
                            Column {
                                Text("History", style = MaterialTheme.typography.titleMedium)
                                Text("Past sessions & progress", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    DoromCard(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate(Routes.gymCalendar(location.name)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            IconBadge(icon = Icons.Filled.CalendarMonth, tint = BadgeGold)
                            Column {
                                Text("Plan on calendar", style = MaterialTheme.typography.titleMedium)
                                Text("Assign workouts to a date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(((GYM_CATEGORIES.size + 1) / 2 * 110 + (GYM_CATEGORIES.size / 2) * 10).dp),
                        userScrollEnabled = false,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(GYM_CATEGORIES.size) { i ->
                            val category = GYM_CATEGORIES[i]
                            DoromCard(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                onClick = { onNavigate(Routes.gymCategory(location.name, category)) },
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val imageRes = categoryImageRes(category)
                                    if (imageRes != null) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(imageRes),
                                            contentDescription = category,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(64.dp).clip(com.arsham.dorom.ui.theme.CardShape),
                                        )
                                    } else {
                                        GlyphBadge(tint = categoryTint(i)) { MuscleGlyph(category = category, tint = categoryTint(i), modifier = Modifier.size(26.dp)) }
                                    }
                                    Text(category, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCard(location: GymLocation, label: String, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DoromCard(onClick = onClick, modifier = modifier.height(128.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            GlyphBadge(tint = tint, size = 56.dp) { GymLocationGlyph(location = location, tint = tint, modifier = Modifier.size(34.dp)) }
            Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/** Tinted rounded backdrop for a custom glyph, matching IconBadge's footprint. */
@Composable
private fun GlyphBadge(tint: Color, size: Dp = 44.dp, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(size).clip(com.arsham.dorom.ui.theme.CardShape).background(tint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
