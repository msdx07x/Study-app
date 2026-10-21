package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    @Query("SELECT * FROM chapters WHERE courseId = :courseId ORDER BY playlistOrder ASC")
    fun getChaptersForCourse(courseId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE courseId = :courseId ORDER BY playlistOrder ASC")
    suspend fun getChaptersForCourseSync(courseId: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id")
    fun getChapterById(id: Long): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterByIdSync(id: Long): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE courseId = :courseId AND videoId = :videoId LIMIT 1")
    suspend fun getChapterByCourseAndVideo(courseId: Long, videoId: String): ChapterEntity?

    @Query("UPDATE chapters SET lastPositionSeconds = :positionSeconds, lastWatchedAt = :lastWatchedAt WHERE id = :chapterId")
    suspend fun updatePlaybackPosition(chapterId: Long, positionSeconds: Int, lastWatchedAt: Long)

    @Query("UPDATE chapters SET completed = :completed, completedAt = :completedAt WHERE id = :chapterId")
    suspend fun updateChapterCompletion(chapterId: Long, completed: Boolean, completedAt: Long?)

    @Query("UPDATE chapters SET notes = :notes WHERE id = :chapterId")
    suspend fun updateChapterNotes(chapterId: Long, notes: String)

    @Query("SELECT * FROM chapters WHERE courseId = :courseId AND completed = 0 ORDER BY playlistOrder ASC")
    suspend fun getIncompleteChaptersForCourse(courseId: Long): List<ChapterEntity>

    @Query("SELECT COUNT(*) FROM chapters WHERE completed = 1")
    fun getAllCompletedChaptersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM chapters")
    fun getAllChaptersCount(): Flow<Int>

    @Query("SELECT * FROM chapters WHERE lastWatchedAt IS NOT NULL ORDER BY lastWatchedAt DESC LIMIT :limit")
    fun getRecentlyWatchedChapters(limit: Int = 5): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE lastWatchedAt IS NOT NULL AND completed = 0 ORDER BY lastWatchedAt DESC LIMIT 1")
    fun getMostRecentIncompleteChapter(): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE completed = 0 ORDER BY lastWatchedAt DESC, id ASC LIMIT 1")
    fun getFirstIncompleteChapter(): Flow<ChapterEntity?>

    @Query("DELETE FROM chapters WHERE courseId = :courseId")
    suspend fun deleteChaptersForCourse(courseId: Long)

    @Query("DELETE FROM chapters")
    suspend fun clearAll()
}
