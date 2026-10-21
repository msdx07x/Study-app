package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChapterEntity
import com.example.data.entity.CourseEntity
import com.example.data.entity.StudyReminderEntity
import com.example.network.youtube.YouTubeTimeUtils
import com.example.player.YouTubeWebViewPlayer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    course: CourseEntity?,
    chapter: ChapterEntity?,
    allChapters: List<ChapterEntity>,
    onNavigateBack: () -> Unit,
    onSelectChapter: (ChapterEntity) -> Unit,
    onUpdatePosition: (positionSeconds: Int) -> Unit,
    onToggleCompletion: (chapterId: Long, completed: Boolean) -> Unit,
    onSaveNotes: (chapterId: Long, notes: String) -> Unit,
    onSaveReminder: (StudyReminderEntity) -> Unit
) {
    if (chapter == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading Lesson...", color = TextSecondary)
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Playback state
    var currentPosSeconds by remember(chapter.id) { mutableIntStateOf(chapter.lastPositionSeconds) }
    var durationSeconds by remember(chapter.id) { mutableIntStateOf(chapter.durationSeconds ?: 0) }

    // Notes local state
    var notesText by remember(chapter.id) { mutableStateOf(chapter.notes) }
    var notesSavedMessage by remember(chapter.id) { mutableStateOf(if (chapter.notes.isNotBlank()) "Saved locally" else "") }

    // UI flags
    var isChapterListExpanded by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }

    // Auto-save notes when user pauses typing
    LaunchedEffect(notesText) {
        if (notesText != chapter.notes) {
            notesSavedMessage = "Saving..."
            delay(1000)
            onSaveNotes(chapter.id, notesText)
            notesSavedMessage = "Saved locally"
        }
    }

    // Save position on screen exit
    DisposableEffect(chapter.id) {
        onDispose {
            if (currentPosSeconds > 0) {
                onUpdatePosition(currentPosSeconds)
            }
            if (notesText != chapter.notes) {
                onSaveNotes(chapter.id, notesText)
            }
        }
    }

    // Find previous and next chapters
    val currentIndex = allChapters.indexOfFirst { it.id == chapter.id }
    val previousChapter = if (currentIndex > 0) allChapters[currentIndex - 1] else null
    val nextChapter = if (currentIndex in 0 until allChapters.size - 1) allChapters[currentIndex + 1] else null

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = course?.title ?: "Study OS",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Chapter ${currentIndex + 1} of ${allChapters.size}",
                            style = MaterialTheme.typography.bodySmall.copy(color = ElectricBlue)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("lesson_back_button")
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
                        onClick = { showReminderDialog = true },
                        modifier = Modifier.testTag("lesson_reminder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Set Reminder",
                            tint = ElectricBlue
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
                .testTag("lesson_scroll_content")
        ) {
            // Official YouTube Player Container (16:9 aspect ratio)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                        .testTag("youtube_player_container")
                ) {
                    YouTubeWebViewPlayer(
                        videoId = chapter.videoId,
                        startPositionSeconds = chapter.lastPositionSeconds,
                        modifier = Modifier.fillMaxSize(),
                        onReady = { dur ->
                            if (dur > 0) durationSeconds = dur
                        },
                        onPositionUpdate = { pos, dur ->
                            currentPosSeconds = pos
                            if (dur > 0) durationSeconds = dur
                            // Save checkpoint every 10 seconds
                            if (pos > 0 && pos % 10 == 0) {
                                onUpdatePosition(pos)
                            }
                        },
                        onVideoEnded = {
                            // Automatic chapter completion!
                            if (!chapter.completed) {
                                onToggleCompletion(chapter.id, true)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Lesson completed! Progress saved.")
                                }
                            }
                        }
                    )
                }
            }

            // Chapter Details & Playback Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Time position bar & completion badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = YouTubeTimeUtils.formatSeconds(currentPosSeconds),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElectricBlue
                                )
                            )
                            Text(
                                text = " / ${YouTubeTimeUtils.formatSeconds(durationSeconds)}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextTertiary)
                            )
                        }

                        if (chapter.completed) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        } else if (chapter.lastPositionSeconds > 0) {
                            Text(
                                text = "Resumed at ${YouTubeTimeUtils.formatSeconds(chapter.lastPositionSeconds)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls Row: Prev, Mark Complete, Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous button
                        OutlinedButton(
                            onClick = { previousChapter?.let { onSelectChapter(it) } },
                            enabled = previousChapter != null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lesson_previous_button"),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = if (previousChapter != null) TextPrimary else TextTertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prev", color = if (previousChapter != null) TextPrimary else TextTertiary)
                        }

                        // Mark Complete / Incomplete button
                        Button(
                            onClick = {
                                val newStatus = !chapter.completed
                                onToggleCompletion(chapter.id, newStatus)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (newStatus) "Marked chapter as completed!" else "Marked chapter as incomplete"
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("lesson_toggle_complete_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (chapter.completed) AccentEmerald else ElectricBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (chapter.completed) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (chapter.completed) "Completed" else "Mark Complete",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Next button
                        OutlinedButton(
                            onClick = { nextChapter?.let { onSelectChapter(it) } },
                            enabled = nextChapter != null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lesson_next_button"),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next", color = if (nextChapter != null) TextPrimary else TextTertiary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = if (nextChapter != null) TextPrimary else TextTertiary
                            )
                        }
                    }
                }
            }

            // DEDICATED LESSON NOTES FEATURE (Locally persistent in Room)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("lesson_notes_card")
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lesson Notes",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            if (notesSavedMessage.isNotBlank()) {
                                Text(
                                    text = notesSavedMessage,
                                    style = MaterialTheme.typography.labelSmall.copy(color = AccentEmerald)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Jot down key takeaways, timestamps, and questions. Notes are saved locally on your device.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            placeholder = { Text("Write your notes for this lesson here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("lesson_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    onSaveNotes(chapter.id, notesText)
                                    notesSavedMessage = "Saved locally"
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Note saved!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("save_note_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Note", color = ElectricBlue, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Expandable Course Chapters List
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isChapterListExpanded = !isChapterListExpanded }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Course Chapters (${allChapters.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Icon(
                                imageVector = if (isChapterListExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        AnimatedVisibility(visible = isChapterListExpanded) {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                                allChapters.forEachIndexed { idx, ch ->
                                    val isCurrent = ch.id == chapter.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrent) DarkSurfaceVariant else Color.Transparent)
                                            .clickable { onSelectChapter(ch) }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (ch.completed) AccentEmerald else DarkCardBorder),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (ch.completed) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "${idx + 1}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = TextPrimary,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ch.title,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCurrent) ElectricBlue else TextPrimary
                                                ),
                                                maxLines = 1
                                            )
                                            if (ch.durationSeconds != null) {
                                                Text(
                                                    text = YouTubeTimeUtils.formatSeconds(ch.durationSeconds),
                                                    style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                                                )
                                            }
                                        }

                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Currently playing",
                                                tint = ElectricBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showReminderDialog) {
        AddReminderDialog(
            chapter = chapter,
            courseTitle = course?.title ?: "Study OS Course",
            onDismiss = { showReminderDialog = false },
            onSaveReminder = { reminder ->
                onSaveReminder(reminder)
                showReminderDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Reminder scheduled!")
                }
            }
        )
    }
}
