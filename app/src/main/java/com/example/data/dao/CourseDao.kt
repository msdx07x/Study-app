package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Long)

    @Query("SELECT * FROM courses ORDER BY lastStudiedAt DESC, importedAt DESC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseById(id: Long): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseByIdSync(id: Long): CourseEntity?

    @Query("SELECT * FROM courses WHERE playlistId = :playlistId LIMIT 1")
    suspend fun getCourseByPlaylistId(playlistId: String): CourseEntity?

    @Query("UPDATE courses SET lastStudiedAt = :timestamp, lastOpenedVideoId = :videoId WHERE id = :courseId")
    suspend fun updateLastStudied(courseId: Long, videoId: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM courses")
    fun getCourseCount(): Flow<Int>

    @Query("DELETE FROM courses")
    suspend fun clearAll()
}
