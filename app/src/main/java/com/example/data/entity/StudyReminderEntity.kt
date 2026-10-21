package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_reminders",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chapterId"]),
        Index(value = ["courseId"])
    ]
)
data class StudyReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: Long,
    val courseId: Long,
    val scheduledAt: Long,
    val repeatIntervalMinutes: Long = 0, // 0 = no repeat, 180 = 3h, 360 = 6h, 1440 = daily
    val repeatEnabled: Boolean = false,
    val enabled: Boolean = true,
    val notificationTitle: String,
    val notificationMessage: String,
    val createdAt: Long = System.currentTimeMillis()
)
