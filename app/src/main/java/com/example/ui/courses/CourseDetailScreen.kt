package com.example.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.ChapterEntity
import com.example.data.entity.CourseEntity
import com.example.data.entity.StudyReminderEntity
import com.example.network.youtube.YouTubeTimeUtils
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.CourseProgressBar
import com.example.ui.reminders.AddReminderDialog
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    course: CourseEntity?,
    chapters: List<ChapterEntity>,
    onNavigateBack: () -> Unit,
    onOpenLesson: (courseId: Long, chapterId: Long) -> Unit,
    onToggleCompletion: (chapterId: Long, completed: Boolean) -> Unit,
    onDeleteCourse: (courseId: Long) -> Unit,
    onRenameCourse: (courseId: Long, newTitle: String) -> Unit,
    onSaveReminder: (StudyReminderEntity) -> Unit
) {
    if (course == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading Course...", color = TextSecondary)
        }
        return
    }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(course.title) }
    var selectedChapterForReminder by remember { mutableStateOf<ChapterEntity?>(null) }

    val completedCount = chapters.count { it.completed }
    val totalCount = chapters.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    // Find next incomplete chapter
    val nextIncompleteChapter = chapters.firstOrNull { !it.completed } ?: chapters.firstOrNull()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Course Details", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("course_detail_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("course_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = TextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename Course", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricBlue) },
                            onClick = {
                                showMenu = false
                                renameText = course.title
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Course", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirm = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("course_detail_scroll")
        ) {
            // Course Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (!course.thumbnailUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = course.thumbnailUrl,
                                contentDescription = course.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    if (!course.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = course.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 20.sp
                            ),
                            maxLines = 3
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            CourseProgressBar(
                                progress = progress,
                                completedCount = completedCount,
                                totalCount = totalCount
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${totalCount - completedCount} chapters remaining",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                                Text(
                                    text = "Playlist Order",
                                    style = MaterialTheme.typography.labelSmall.copy(color = ElectricBlue)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Action: Continue Studying
                    if (nextIncompleteChapter != null) {
                        Button(
                            onClick = { onOpenLesson(course.id, nextIncompleteChapter.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("continue_studying_course_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (completedCount > 0) "Continue Studying" else "Start Course",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Chapters List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Chapters ($totalCount)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }

            // Chapters
            itemsIndexed(chapters, key = { _, item -> item.id }) { index, ch ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("chapter_item_${ch.id}")
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onOpenLesson(course.id, ch.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (ch.completed) DarkSurfaceVariant.copy(alpha = 0.6f) else DarkSurface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail or Order Badge
                        Box(
                            modifier = Modifier
                                .size(width = 72.dp, height = 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!ch.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ch.thumbnailUrl,
                                    contentDescription = ch.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (ch.completed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Chapter Title and metadata
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${index + 1}. ${ch.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (ch.completed) TextSecondary else TextPrimary
                                ),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ch.durationSeconds != null) {
                                    Text(
                                        text = YouTubeTimeUtils.formatSeconds(ch.durationSeconds),
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                                    )
                                }
                                if (ch.lastPositionSeconds > 0 && !ch.completed) {
                                    Text(
                                        text = " • Stopped at ${YouTubeTimeUtils.formatSeconds(ch.lastPositionSeconds)}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = ElectricBlue)
                                    )
                                }
                                if (ch.notes.isNotBlank()) {
                                    Text(
                                        text = " • Has Notes",
                                        style = MaterialTheme.typography.labelSmall.copy(color = AccentEmerald)
                                    )
                                }
                            }
                        }

                        // Action: Set Reminder
                        IconButton(
                            onClick = { selectedChapterForReminder = ch },
                            modifier = Modifier.testTag("chapter_reminder_btn_${ch.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Set Reminder",
                                tint = ElectricBlue.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Action: Toggle completion
                        IconButton(
                            onClick = { onToggleCompletion(ch.id, !ch.completed) },
                            modifier = Modifier.testTag("chapter_complete_btn_${ch.id}")
                        ) {
                            Icon(
                                imageVector = if (ch.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (ch.completed) "Mark Incomplete" else "Mark Complete",
                                tint = if (ch.completed) AccentEmerald else TextTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete Course",
            message = "Are you sure you want to delete \"${course.title}\"? All saved playback positions, chapters, lesson notes, and scheduled reminders for this course will also be permanently deleted.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDeleteCourse(course.id)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    // Rename Course Dialog
    if (showRenameDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Course", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Course Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameCourse(course.id, renameText.trim())
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Add Reminder Dialog
    selectedChapterForReminder?.let { ch ->
        AddReminderDialog(
            chapter = ch,
            courseTitle = course.title,
            onDismiss = { selectedChapterForReminder = null },
            onSaveReminder = { reminder ->
                onSaveReminder(reminder)
                selectedChapterForReminder = null
            }
        )
    }
}
