package com.arsham.dorom.ui.gym

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.GYM_CATEGORIES
import com.arsham.dorom.data.entity.GymLocation
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.MonthCalendar
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.Terracotta
import com.arsham.dorom.ui.theme.doromClickable
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun GymCalendarScreen(location: GymLocation, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf(LocalDate.now()) }
    val selectedCategories = remember { mutableStateListOf<String>() }
    val plannedDates by container.gymRepository.observePlannedDates().collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(date) {
        val scheduled = container.gymRepository.getScheduledExercises(date.toString())
        selectedCategories.clear()
        selectedCategories.addAll(scheduled.map { it.category }.distinct())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Plan on calendar", onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Which day?", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Days with a dot already have a saved gym plan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                        )
                        MonthCalendar(
                            selectedDate = date,
                            markedDates = plannedDates.toSet(),
                            onSelectDate = { date = it },
                        )
                    }
                }
            }

            item {
                Column {
                    SectionHeader(title = "Pick a plan for $date")
                    Text(
                        "Tap the muscle groups you're training this day — every exercise saved under them comes along automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                CategoryGrid(
                    location = location,
                    selectedCategories = selectedCategories,
                    onToggle = { category ->
                        if (selectedCategories.contains(category)) selectedCategories.remove(category) else selectedCategories.add(category)
                    },
                )
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            val exerciseIds = selectedCategories.flatMap { category ->
                                container.gymRepository.getExercises(location, category).map { it.id }
                            }
                            container.gymRepository.setSchedule(date.toString(), exerciseIds)
                            onBack()
                        }
                    },
                ) { Text("Save plan for this day (${selectedCategories.size} selected)") }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    location: GymLocation,
    selectedCategories: List<String>,
    onToggle: (String) -> Unit,
) {
    Column {
        val rows = (GYM_CATEGORIES.size + 1) / 2
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (col in 0..1) {
                    val index = row * 2 + col
                    if (index < GYM_CATEGORIES.size) {
                        val category = GYM_CATEGORIES[index]
                        SelectableCategoryCard(
                            category = category,
                            tint = categoryTint(index),
                            selected = selectedCategories.contains(category),
                            onClick = { onToggle(category) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableCategoryCard(category: String, tint: Color, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor = if (selected) Terracotta else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.5.dp else 1.dp

    Box(
        modifier = modifier
            .height(88.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(borderWidth, borderColor), CardShape)
            .doromClickable(onClick)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(44.dp).clip(CardShape).background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                MuscleGlyph(category = category, tint = tint, modifier = Modifier.size(26.dp))
            }
            Text(category, style = MaterialTheme.typography.titleMedium)
        }
        if (selected) {
            SelectedBadge(modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
private fun SelectedBadge(modifier: Modifier = Modifier, icon: ImageVector = Icons.Filled.Check) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(14.dp))
    }
}
