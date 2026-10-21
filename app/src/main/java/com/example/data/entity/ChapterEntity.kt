package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["courseId"]),
        Index(value = ["courseId", "videoId"], unique = true)
    ]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val videoId: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Int? = null,
    val playlistOrder: Int = 0,
    val lastPositionSeconds: Int = 0,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val lastWatchedAt: Long? = null,
    val availabilityStatus: String = "AVAILABLE", // AVAILABLE, PRIVATE, DELETED
    val notes: String = "" // Dedicated local lesson notes
)
