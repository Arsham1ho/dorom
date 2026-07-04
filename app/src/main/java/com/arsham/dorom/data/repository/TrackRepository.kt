package com.arsham.dorom.data.repository

import com.arsham.dorom.data.dao.CourseDao
import com.arsham.dorom.data.dao.TimeMarkerDao
import com.arsham.dorom.data.entity.Course
import com.arsham.dorom.data.entity.TimeMarker
import kotlinx.coroutines.flow.Flow

class CourseRepository(private val dao: CourseDao) {
    fun observeCourses(): Flow<List<Course>> = dao.observeCourses()
    suspend fun upsertCourse(course: Course) = dao.upsertCourse(course)
    suspend fun deleteCourse(course: Course) = dao.deleteCourse(course)
    suspend fun logSessionCompleted(course: Course, delta: Int) {
        val updated = course.copy(completedLessons = (course.completedLessons + delta).coerceIn(0, course.totalLessons))
        dao.upsertCourse(updated)
    }
}

class TimeMarkerRepository(private val dao: TimeMarkerDao) {
    fun observeMarkers(): Flow<List<TimeMarker>> = dao.observeMarkers()
    suspend fun upsertMarker(marker: TimeMarker) = dao.upsertMarker(marker)
    suspend fun deleteMarker(marker: TimeMarker) = dao.deleteMarker(marker)
}
