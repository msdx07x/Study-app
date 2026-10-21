package com.example.data.repository

import com.example.data.dao.ChapterDao
import com.example.data.dao.CourseDao
import com.example.data.dao.StudyReminderDao
import com.example.data.entity.ChapterEntity
import com.example.data.entity.CourseEntity
import com.example.data.entity.StudyReminderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

class CourseRepository(
    private val courseDao: CourseDao,
    private val chapterDao: ChapterDao,
    private val reminderDao: StudyReminderDao,
    private val onReminderCancelled: (Long) -> Unit = {} // Callback to cancel system alarms
) {

    val allCourses: Flow<List<CourseEntity>> = courseDao.getAllCourses()
    val totalCoursesCount: Flow<Int> = courseDao.getCourseCount()
    val totalChaptersCount: Flow<Int> = chapterDao.getAllChaptersCount()
    val completedChaptersCount: Flow<Int> = chapterDao.getAllCompletedChaptersCount()
    val recentlyWatchedChapters: Flow<List<ChapterEntity>> = chapterDao.getRecentlyWatchedChapters(5)
    val mostRecentIncompleteChapter: Flow<ChapterEntity?> = chapterDao.getMostRecentIncompleteChapter()
    val allReminders: Flow<List<StudyReminderEntity>> = reminderDao.getAllReminders()

    // Calculated stats
    val pendingChaptersCount: Flow<Int> = combine(
        totalChaptersCount,
        completedChaptersCount
    ) { total, completed ->
        val pending = total - completed
        if (pending >= 0) pending else 0
    }

    suspend fun getCourseById(courseId: Long): CourseEntity? {
        return courseDao.getCourseByIdSync(courseId)
    }

    fun getCourseFlow(courseId: Long): Flow<CourseEntity?> {
        return courseDao.getCourseById(courseId)
    }

    fun getChaptersForCourse(courseId: Long): Flow<List<ChapterEntity>> {
        return chapterDao.getChaptersForCourse(courseId)
    }

    suspend fun getChaptersForCourseSync(courseId: Long): List<ChapterEntity> {
        return chapterDao.getChaptersForCourseSync(courseId)
    }

    suspend fun getChapterById(chapterId: Long): ChapterEntity? {
        return chapterDao.getChapterByIdSync(chapterId)
    }

    fun getChapterFlow(chapterId: Long): Flow<ChapterEntity?> {
        return chapterDao.getChapterById(chapterId)
    }

    suspend fun saveImportedCourse(course: CourseEntity, chapters: List<ChapterEntity>): Long {
        // Prevent duplicate playlist
        val existing = courseDao.getCourseByPlaylistId(course.playlistId)
        val courseId = if (existing != null) {
            existing.id
        } else {
            courseDao.insertCourse(course)
        }

        val chaptersWithCourseId = chapters.map { chapter ->
            val existingChapter = chapterDao.getChapterByCourseAndVideo(courseId, chapter.videoId)
            if (existingChapter != null) {
                // Preserve user progress, notes, and completion if already existing
                existingChapter.copy(
                    title = chapter.title,
                    thumbnailUrl = chapter.thumbnailUrl ?: existingChapter.thumbnailUrl,
                    durationSeconds = chapter.durationSeconds ?: existingChapter.durationSeconds,
                    playlistOrder = chapter.playlistOrder
                )
            } else {
                chapter.copy(courseId = courseId)
            }
        }

        chapterDao.insertChapters(chaptersWithCourseId)
        return courseId
    }

    suspend fun updatePlaybackPosition(courseId: Long, chapterId: Long, videoId: String, positionSeconds: Int) {
        val now = System.currentTimeMillis()
        chapterDao.updatePlaybackPosition(chapterId, positionSeconds, now)
        courseDao.updateLastStudied(courseId, videoId, now)
    }

    suspend fun updateChapterCompletion(chapterId: Long, completed: Boolean) {
        val now = if (completed) System.currentTimeMillis() else null
        chapterDao.updateChapterCompletion(chapterId, completed, now)

        if (completed) {
            // When chapter is completed, cancel and disable all associated reminders
            val reminders = reminderDao.getRemindersForChapterSync(chapterId)
            for (reminder in reminders) {
                onReminderCancelled(reminder.id)
            }
            reminderDao.disableRemindersForChapter(chapterId)
        }
    }

    suspend fun updateChapterNotes(chapterId: Long, notes: String) {
        chapterDao.updateChapterNotes(chapterId, notes)
    }

    suspend fun deleteCourse(courseId: Long) {
        // Clean up reminders for this course
        val chapters = chapterDao.getChaptersForCourseSync(courseId)
        for (chapter in chapters) {
            val reminders = reminderDao.getRemindersForChapterSync(chapter.id)
            for (reminder in reminders) {
                onReminderCancelled(reminder.id)
            }
        }
        reminderDao.deleteRemindersForCourse(courseId)
        chapterDao.deleteChaptersForCourse(courseId)
        courseDao.deleteCourseById(courseId)
    }

    suspend fun renameCourse(courseId: Long, newTitle: String) {
        val course = courseDao.getCourseByIdSync(courseId) ?: return
        courseDao.updateCourse(course.copy(title = newTitle))
    }

    suspend fun insertReminder(reminder: StudyReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(reminderId: Long) {
        onReminderCancelled(reminderId)
        reminderDao.deleteReminderById(reminderId)
    }

    suspend fun toggleReminder(reminderId: Long, enabled: Boolean) {
        val reminder = reminderDao.getReminderById(reminderId) ?: return
        if (!enabled) {
            onReminderCancelled(reminderId)
        }
        reminderDao.updateReminder(reminder.copy(enabled = enabled))
    }

    suspend fun clearAllData() {
        reminderDao.clearAll()
        chapterDao.clearAll()
        courseDao.clearAll()
    }
}
