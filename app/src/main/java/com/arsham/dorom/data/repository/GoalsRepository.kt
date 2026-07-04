package com.arsham.dorom.data.repository

import com.arsham.dorom.data.dao.GoalDao
import com.arsham.dorom.data.dao.WeeklyPlanDao
import com.arsham.dorom.data.entity.LongTermGoal
import com.arsham.dorom.data.entity.WeeklyPlan
import com.arsham.dorom.data.entity.WeeklyPlanItem
import kotlinx.coroutines.flow.Flow

class GoalsRepository(private val dao: GoalDao) {
    fun observeGoals(): Flow<List<LongTermGoal>> = dao.observeGoals()
    suspend fun upsertGoal(goal: LongTermGoal) = dao.upsertGoal(goal)
    suspend fun deleteGoal(goal: LongTermGoal) = dao.deleteGoal(goal)
}

class WeeklyPlanRepository(private val dao: WeeklyPlanDao) {
    fun observePlan(weekStartDate: String): Flow<WeeklyPlan?> = dao.observeWeeklyPlan(weekStartDate)
    fun observeItems(weekStartDate: String): Flow<List<WeeklyPlanItem>> = dao.observeItems(weekStartDate)
    suspend fun upsertPlan(plan: WeeklyPlan) = dao.upsertWeeklyPlan(plan)
    suspend fun upsertItem(item: WeeklyPlanItem) = dao.upsertItem(item)
    suspend fun toggleItem(item: WeeklyPlanItem) = dao.updateItem(item.copy(isDone = !item.isDone))
    suspend fun deleteItem(item: WeeklyPlanItem) = dao.deleteItem(item)
}
