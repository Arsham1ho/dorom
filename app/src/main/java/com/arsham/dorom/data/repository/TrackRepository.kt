package com.arsham.dorom.data.repository

import com.arsham.dorom.data.dao.CourseDao
import com.arsham.dorom.data.dao.FinnishPracticeDao
import com.arsham.dorom.data.dao.PersonalProjectDao
import com.arsham.dorom.data.dao.TimeMarkerDao
import com.arsham.dorom.data.entity.Course
import com.arsham.dorom.data.entity.FinnishPracticeEntry
import com.arsham.dorom.data.entity.PersonalProject
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

class PersonalProjectRepository(private val dao: PersonalProjectDao) {
    fun observeProjects(): Flow<List<PersonalProject>> = dao.observeProjects()
    suspend fun upsertProject(project: PersonalProject) = dao.upsertProject(project)
    suspend fun deleteProject(project: PersonalProject) = dao.deleteProject(project)
}

class FinnishPracticeRepository(private val dao: FinnishPracticeDao) {
    fun observeEntries(): Flow<List<FinnishPracticeEntry>> = dao.observeEntries()
    suspend fun upsertEntry(entry: FinnishPracticeEntry) = dao.upsertEntry(entry)
    suspend fun deleteEntry(entry: FinnishPracticeEntry) = dao.deleteEntry(entry)
}
