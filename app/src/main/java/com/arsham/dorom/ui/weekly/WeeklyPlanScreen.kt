package com.arsham.dorom.ui.weekly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.WeeklyPlan
import com.arsham.dorom.data.entity.WeeklyPlanItem
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.util.dayOfWeekLabel
import com.arsham.dorom.util.mondayOfWeek
import com.arsham.dorom.util.pretty
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun WeeklyPlanScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val weekStart = remember { mondayOfWeek() }
    val weekStartStr = remember { weekStart.toString() }

    val plan by container.weeklyPlanRepository.observePlan(weekStartStr).collectAsStateWithLifecycle(initialValue = null)
    val items by container.weeklyPlanRepository.observeItems(weekStartStr).collectAsStateWithLifecycle(initialValue = emptyList())

    var theme by remember(plan) { mutableStateOf(plan?.theme ?: "") }
    var notes by remember(plan) { mutableStateOf(plan?.notes ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "This week", onBack = onBack)
        Text(
            "${weekStart.pretty()} – ${weekStart.plusDays(6).pretty()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                DoromCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        OutlinedTextField(
                            value = theme,
                            onValueChange = {
                                theme = it
                                scope.launch { container.weeklyPlanRepository.upsertPlan(WeeklyPlan(weekStartStr, it, notes)) }
                            },
                            label = { Text("This week's theme") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = {
                                notes = it
                                scope.launch { container.weeklyPlanRepository.upsertPlan(WeeklyPlan(weekStartStr, theme, it)) }
                            },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    }
                }
            }

            for (dow in 1..7) {
                item {
                    DayItemsSection(
                        dayOfWeek = dow,
                        date = weekStart.plusDays((dow - 1).toLong()),
                        items = items.filter { it.dayOfWeek == dow },
                        onAdd = { desc ->
                            scope.launch {
                                container.weeklyPlanRepository.upsertItem(WeeklyPlanItem(weekStartDate = weekStartStr, dayOfWeek = dow, description = desc))
                            }
                        },
                        onToggle = { item -> scope.launch { container.weeklyPlanRepository.toggleItem(item) } },
                        onDelete = { item -> scope.launch { container.weeklyPlanRepository.deleteItem(item) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayItemsSection(
    dayOfWeek: Int,
    date: LocalDate,
    items: List<WeeklyPlanItem>,
    onAdd: (String) -> Unit,
    onToggle: (WeeklyPlanItem) -> Unit,
    onDelete: (WeeklyPlanItem) -> Unit,
) {
    var newText by remember { mutableStateOf("") }
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionHeader(title = "${dayOfWeekLabel(dayOfWeek)} · ${date.pretty()}")
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = item.isDone, onCheckedChange = { onToggle(item) })
                    Text(
                        item.description,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = { onDelete(item) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    label = { Text("Add item") },
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    if (newText.isNotBlank()) {
                        onAdd(newText.trim())
                        newText = ""
                    }
                }) { Icon(Icons.Filled.Add, contentDescription = "Add") }
            }
        }
    }
}
