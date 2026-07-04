package com.arsham.dorom.data.repository

import com.arsham.dorom.data.dao.PlanDao
import com.arsham.dorom.data.entity.DailyPlan
import com.arsham.dorom.data.entity.PlanTask
import com.arsham.dorom.notifications.AlarmScheduler
import kotlinx.coroutines.flow.Flow

class PlanRepository(
    private val dao: PlanDao,
    private val alarmScheduler: AlarmScheduler,
) {
    fun observePlan(date: String): Flow<DailyPlan?> = dao.observePlan(date)
    fun observeTasks(date: String): Flow<List<PlanTask>> = dao.observeTasks(date)
    suspend fun getPlan(date: String): DailyPlan? = dao.getPlan(date)
    suspend fun getTasks(date: String): List<PlanTask> = dao.getTasks(date)

    /** Saves tonight's plan for [date] and arms every alarm for it: wake, sleep, review, and one per task. */
    suspend fun savePlan(date: String, wakeTime: String, bedTime: String, tasks: List<PlanTask>) {
        dao.upsertPlan(DailyPlan(date = date, wakeTargetTime = wakeTime, bedTargetTime = bedTime, createdAtEpochMillis = System.currentTimeMillis()))

        dao.getTasks(date).forEach { alarmScheduler.cancelTaskReminder(it.id) }
        dao.deleteTasksForDate(date)
        tasks.forEachIndexed { index, task ->
            val id = dao.upsertTask(task.copy(date = date, orderIndex = index))
            alarmScheduler.scheduleTaskReminder(date, task.copy(id = id, date = date, orderIndex = index))
        }

        alarmScheduler.scheduleWakeCheckIn(date, wakeTime)
        alarmScheduler.scheduleSleepCheckIn(date, bedTime)
        alarmScheduler.scheduleEndOfDayReview(date, bedTime)
    }

    suspend fun toggleTaskDone(task: PlanTask) {
        val updated = task.copy(
            isDone = !task.isDone,
            completedAtEpochMillis = if (!task.isDone) System.currentTimeMillis() else null,
        )
        dao.updateTask(updated)
        if (updated.isDone) alarmScheduler.cancelTaskReminder(updated.id)
    }

    suspend fun deleteTask(task: PlanTask) {
        dao.deleteTask(task)
        alarmScheduler.cancelTaskReminder(task.id)
    }

    suspend fun getAllPlannedDates(): List<String> = dao.getAllPlannedDates()
}
