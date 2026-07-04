package com.arsham.dorom.ui.finance

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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsham.dorom.data.entity.MoneyTransaction
import com.arsham.dorom.data.entity.TransactionType
import com.arsham.dorom.data.repository.MonthlyReport
import com.arsham.dorom.ui.LocalAppContainer
import com.arsham.dorom.ui.components.BarChart
import com.arsham.dorom.ui.components.DoromCard
import com.arsham.dorom.ui.components.ProgressRing
import com.arsham.dorom.ui.components.SectionHeader
import com.arsham.dorom.ui.components.Tag
import com.arsham.dorom.ui.components.TopBarWithBack
import com.arsham.dorom.ui.theme.DataText
import com.arsham.dorom.ui.theme.Sage
import com.arsham.dorom.ui.theme.doromClickable
import com.arsham.dorom.util.todayString
import com.arsham.dorom.util.yearMonthString
import kotlinx.coroutines.launch
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

            if (showAdd) {
                item {
                    TransactionEditor(
                        onSave = { tx -> scope.launch { container.financeRepository.upsertTransaction(tx) }; showAdd = false },
                        onCancel = { showAdd = false },
                    )
                }
            }

            item { SectionHeader(title = "This month") }
            items(transactions) { tx ->
                TransactionRow(tx, onDelete = { scope.launch { container.financeRepository.deleteTransaction(tx) } })
            }
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

private fun money(v: Double): String = "%.0f".format(v)

@Composable
private fun TransactionEditor(onSave: (MoneyTransaction) -> Unit, onCancel: () -> Unit) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(EXPENSE_CATEGORIES.first()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            OutlinedTextField(
                value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    onSave(MoneyTransaction(date = todayString(), type = type, category = category, amount = amt, note = note.trim()))
                }) { Text("Save") }
                Button(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: MoneyTransaction, onDelete: () -> Unit) {
    val color = when (tx.type) {
        TransactionType.INCOME -> Sage
        TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
        TransactionType.SAVING -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    DoromCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(tx.category, style = MaterialTheme.typography.titleMedium)
                Text(tx.date + if (tx.note.isNotBlank()) " · ${tx.note}" else "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(money(tx.amount), style = DataText.medium, color = color)
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
            }
        }
    }
}
