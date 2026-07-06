package com.arsham.dorom.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.MoneyTransaction
import com.arsham.dorom.data.entity.TransactionType
import com.arsham.dorom.data.repository.MonthlyReport
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.BarChart
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.IconBadge
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.Tag
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.CardShape
import com.arsham.dorom.ui.theme.DangerRed
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.ui.theme.doromClickable
import com.arsham.dorom.util.todayString
import com.arsham.dorom.util.yearMonthString
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val EXPENSE_CATEGORIES = listOf("Rent", "Food", "Transport", "Bills", "Fun", "Shopping", "Health", "Other")
private val INCOME_CATEGORIES = listOf("Salary", "Freelance", "Gift", "Other")

@Composable
fun FinanceScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val yearMonth = remember { yearMonthString() }

    val transactions by container.financeRepository.observeForMonth(yearMonth).collectAsStateWithLifecycle(initialValue = emptyList())
    val report by container.financeRepository.observeMonthlyReport(yearMonth).collectAsStateWithLifecycle(
        initialValue = MonthlyReport(0.0, 0.0, 0.0, emptyMap(), 50),
    )
    var showAdd by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarWithBack(title = "Finance", onBack = onBack) {
            IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Add transaction") }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { MonthlyReportCard(report) }

            if (report.byCategory.isNotEmpty()) {
                item {
                    DoromCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            SectionHeader(title = "Where it went")
                            BarChart(
                                values = report.byCategory.values.map { it.toFloat() },
                                labels = report.byCategory.keys.toList(),
                            )
                        }
                    }
                }
            }

            item { SectionHeader(title = "This month") }
            items(transactions) { tx ->
                TransactionRow(tx, onDelete = { scope.launch { container.financeRepository.deleteTransaction(tx) } })
            }
        }
    }

    if (showAdd) {
        Dialog(onDismissRequest = { showAdd = false }) {
            TransactionEditor(
                onSave = { tx -> scope.launch { container.financeRepository.upsertTransaction(tx) }; showAdd = false },
                onCancel = { showAdd = false },
            )
        }
    }
}

@Composable
private fun MonthlyReportCard(report: MonthlyReport) {
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProgressRing(percent = report.spendingScore / 100f, size = 76.dp, centerLabel = "${report.spendingScore}")
            Column {
                Text("Spending score", style = MaterialTheme.typography.titleMedium)
                Text("Income  ${money(report.income)}", style = MaterialTheme.typography.bodyMedium, color = Sage)
                Text("Expense ${money(report.expense)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                Text("Saving  ${money(report.saving)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun money(v: Double): String = "€%.0f".format(v)

@Composable
private fun TransactionEditor(onSave: (MoneyTransaction) -> Unit, onCancel: () -> Unit) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(EXPENSE_CATEGORIES.first()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Add a transaction", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onCancel) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.entries.forEach { t ->
                    Tag(
                        text = t.name.lowercase().replaceFirstChar { it.uppercase() },
                        filled = type == t,
                        modifier = Modifier.doromClickable { type = t; category = if (t == TransactionType.INCOME) INCOME_CATEGORIES.first() else EXPENSE_CATEGORIES.first() },
                    )
                }
            }
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val categories = if (type == TransactionType.INCOME) INCOME_CATEGORIES else EXPENSE_CATEGORIES
                items(categories) { c ->
                    Tag(text = c, filled = category == c, modifier = Modifier.doromClickable { category = c })
                }
            }
            OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape,
                value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(shape = com.arsham.dorom.ui.theme.InputShape, value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                Button(onClick = onCancel) { Text("Cancel") }
                Button(onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    onSave(MoneyTransaction(date = todayString(), type = type, category = category, amount = amt, note = note.trim()))
                }) { Text("Save") }
            }
        }
    }
}

@Composable
private fun typeColor(type: TransactionType) = when (type) {
    TransactionType.INCOME -> Sage
    TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
    TransactionType.SAVING -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun typeIcon(type: TransactionType) = when (type) {
    TransactionType.INCOME -> Icons.Filled.ArrowUpward
    TransactionType.EXPENSE -> Icons.Filled.ArrowDownward
    TransactionType.SAVING -> Icons.Filled.Savings
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TransactionRow(tx: MoneyTransaction, onDelete: () -> Unit) {
    val scope = rememberCoroutineScope()
    val color = typeColor(tx.type)
    val icon = typeIcon(tx.type)
    var showDetails by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd) showConfirm = true
            false
        },
    )

    androidx.compose.material3.SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardShape)
                    .background(DangerRed)
                    .padding(horizontal = 20.dp),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
            }
        },
    ) {
        DoromCard(modifier = Modifier.fillMaxWidth(), onClick = { showDetails = true }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconBadge(icon = icon, tint = color)
                    Column {
                        Text(tx.category, style = MaterialTheme.typography.titleMedium)
                        Text(tx.date + if (tx.note.isNotBlank()) " · ${tx.note}" else "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(money(tx.amount), style = DataText.medium, color = color)
            }
        }
    }

    if (showDetails) {
        Dialog(onDismissRequest = { showDetails = false }) {
            TransactionDetails(tx = tx, onDismiss = { showDetails = false })
        }
    }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false; scope.launch { dismissState.reset() } },
            title = { Text("Remove transaction?") },
            text = { Text("Remove this ${tx.category} entry?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showConfirm = false
                    onDelete()
                }) { Text("Remove") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showConfirm = false; scope.launch { dismissState.reset() } }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun TransactionDetails(tx: MoneyTransaction, onDismiss: () -> Unit) {
    val color = typeColor(tx.type)
    val icon = typeIcon(tx.type)
    val time = remember(tx.timestampEpochMillis) {
        Instant.ofEpochMilli(tx.timestampEpochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Transaction details", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close") }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                IconBadge(icon = icon, tint = color, size = 48.dp)
                Column {
                    Text(tx.category, style = MaterialTheme.typography.titleLarge)
                    Text(tx.type.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(money(tx.amount), style = DataText.large, color = color, modifier = Modifier.padding(top = 14.dp))
            DetailLine(label = "Date", value = tx.date)
            DetailLine(label = "Time", value = time)
            if (tx.note.isNotBlank()) {
                DetailLine(label = "Description", value = tx.note)
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
