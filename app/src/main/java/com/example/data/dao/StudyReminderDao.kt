package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.StudyReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: StudyReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: StudyReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: StudyReminderEntity)

    @Query("DELETE FROM study_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM study_reminders WHERE chapterId = :chapterId")
    suspend fun deleteRemindersForChapter(chapterId: Long)

    @Query("DELETE FROM study_reminders WHERE courseId = :courseId")
    suspend fun deleteRemindersForCourse(courseId: Long)

    @Query("SELECT * FROM study_reminders ORDER BY scheduledAt ASC")
    fun getAllReminders(): Flow<List<StudyReminderEntity>>

    @Query("SELECT * FROM study_reminders WHERE enabled = 1 ORDER BY scheduledAt ASC")
    suspend fun getActiveReminders(): List<StudyReminderEntity>

    @Query("SELECT * FROM study_reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): StudyReminderEntity?

    @Query("SELECT * FROM study_reminders WHERE chapterId = :chapterId")
    fun getRemindersForChapter(chapterId: Long): Flow<List<StudyReminderEntity>>

    @Query("SELECT * FROM study_reminders WHERE chapterId = :chapterId")
    suspend fun getRemindersForChapterSync(chapterId: Long): List<StudyReminderEntity>

    @Query("UPDATE study_reminders SET enabled = 0 WHERE chapterId = :chapterId")
    suspend fun disableRemindersForChapter(chapterId: Long)

    @Query("SELECT * FROM study_reminders WHERE scheduledAt >= :startOfDay AND scheduledAt <= :endOfDay AND enabled = 1 ORDER BY scheduledAt ASC")
    fun getRemindersForDay(startOfDay: Long, endOfDay: Long): Flow<List<StudyReminderEntity>>

    @Query("DELETE FROM study_reminders")
    suspend fun clearAll()
}
