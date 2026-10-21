package com.example.ui.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChapterEntity
import com.example.data.entity.CourseEntity
import com.example.data.entity.StudyReminderEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    reminders: List<StudyReminderEntity>,
    chaptersMap: Map<Long, ChapterEntity>,
    coursesMap: Map<Long, CourseEntity>,
    onToggleReminder: (reminderId: Long, enabled: Boolean) -> Unit,
    onDeleteReminder: (reminderId: Long) -> Unit,
    onOpenLesson: (courseId: Long, chapterId: Long) -> Unit
) {
    var reminderToDelete by remember { mutableStateOf<StudyReminderEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Study Reminders",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        if (reminders.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.NotificationsActive,
                title = "No Active Study Reminders",
                subtitle = "Set study reminders for any chapter to build a consistent habit. Reminders can repeat until the lesson is completed.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .testTag("reminders_list")
            ) {
                item {
                    Text(
                        text = "Scheduled Alarms (${reminders.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                items(reminders, key = { it.id }) { reminder ->
                    val chapter = chaptersMap[reminder.chapterId]
                    val course = coursesMap[reminder.courseId]
                    val isPast = reminder.scheduledAt < System.currentTimeMillis()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("reminder_card_${reminder.id}")
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (reminder.enabled) DarkSurface else DarkSurface.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (reminder.enabled) ElectricBlue.copy(alpha = 0.15f) else DarkCardBorder
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (reminder.enabled) Icons.Default.AlarmOn else Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = if (reminder.enabled) ElectricBlue else TextTertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = course?.title ?: "Study Course",
                                            style = MaterialTheme.typography.labelSmall.copy(color = ElectricBlue)
                                        )
                                        Text(
                                            text = chapter?.title ?: "Chapter",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (reminder.enabled) TextPrimary else TextSecondary
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Switch(
                                    checked = reminder.enabled,
                                    onCheckedChange = { onToggleReminder(reminder.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = ElectricBlue,
                                        uncheckedThumbColor = TextTertiary,
                                        uncheckedTrackColor = DarkCardBorder
                                    ),
                                    modifier = Modifier.testTag("toggle_reminder_${reminder.id}")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Scheduled time info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = dateFormat.format(Date(reminder.scheduledAt)),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isPast) AccentAmber else TextPrimary
                                        )
                                    )

                                    if (reminder.repeatEnabled && reminder.repeatIntervalMinutes > 0) {
                                        val repeatText = when (reminder.repeatIntervalMinutes) {
                                            180L -> "Repeats every 3 hours until completed"
                                            360L -> "Repeats every 6 hours until completed"
                                            1440L -> "Repeats daily until completed"
                                            else -> "Repeats every ${reminder.repeatIntervalMinutes} min"
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Repeat,
                                                contentDescription = null,
                                                tint = AccentEmerald,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = repeatText,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = AccentEmerald,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Open chapter
                                    if (chapter != null) {
                                        IconButton(
                                            onClick = { onOpenLesson(reminder.courseId, reminder.chapterId) },
                                            modifier = Modifier.testTag("open_lesson_from_reminder_${reminder.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Open Lesson",
                                                tint = ElectricBlue
                                            )
                                        }
                                    }

                                    // Delete reminder
                                    IconButton(
                                        onClick = { reminderToDelete = reminder },
                                        modifier = Modifier.testTag("delete_reminder_${reminder.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Reminder",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
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
    }

    reminderToDelete?.let { reminder ->
        ConfirmDialog(
            title = "Delete Reminder",
            message = "Do you want to cancel and remove this scheduled study reminder?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteReminder(reminder.id)
                reminderToDelete = null
            },
            onDismiss = { reminderToDelete = null }
        )
    }
}
