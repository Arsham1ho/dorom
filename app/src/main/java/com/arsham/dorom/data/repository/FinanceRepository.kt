package com.arsham.dorom.data.repository

import com.arsham.dorom.data.dao.FinanceDao
import com.arsham.dorom.data.entity.MoneyTransaction
import com.arsham.dorom.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class MonthlyReport(
    val income: Double,
    val expense: Double,
    val saving: Double,
    val byCategory: Map<String, Double>, // expense-only breakdown
    val spendingScore: Int, // 0..100, higher = healthier ratio of saving+leftover to income
)

class FinanceRepository(private val dao: FinanceDao) {
    fun observeForMonth(yearMonth: String): Flow<List<MoneyTransaction>> = dao.observeForMonth(yearMonth)
    fun observeAll(): Flow<List<MoneyTransaction>> = dao.observeAll()

    suspend fun upsertTransaction(transaction: MoneyTransaction) = dao.upsertTransaction(transaction)
    suspend fun deleteTransaction(transaction: MoneyTransaction) = dao.deleteTransaction(transaction)

    fun observeMonthlyReport(yearMonth: String): Flow<MonthlyReport> =
        observeForMonth(yearMonth).map { transactions -> buildReport(transactions) }

    private fun buildReport(transactions: List<MoneyTransaction>): MonthlyReport {
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val saving = transactions.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }
        val byCategory = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        // Healthy target: spend no more than ~70% of income, save the rest. Score rewards
        // staying under that line and punishes spending beyond income.
        val score = if (income <= 0.0) {
            if (expense > 0.0) 0 else 50
        } else {
            val leftoverRatio = ((income - expense) / income).coerceIn(-1.0, 1.0)
            (50 + leftoverRatio * 50).toInt().coerceIn(0, 100)
        }

        return MonthlyReport(income, expense, saving, byCategory, score)
    }
}
