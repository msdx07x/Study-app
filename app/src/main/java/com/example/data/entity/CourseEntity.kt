package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    indices = [
        Index(value = ["playlistId"], unique = true)
    ]
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val importedAt: Long = System.currentTimeMillis(),
    val lastStudiedAt: Long? = null,
    val lastOpenedVideoId: String? = null,
    val totalKnownVideos: Int = 0,
    val metadataRefreshAt: Long = System.currentTimeMillis()
)
