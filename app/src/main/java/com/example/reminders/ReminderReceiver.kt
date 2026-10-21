package com.example.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_CHAPTER_ID = "extra_chapter_id"
        const val EXTRA_COURSE_ID = "extra_course_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val chapterId = intent.getLongExtra(EXTRA_CHAPTER_ID, -1L)
        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L)

        if (reminderId == -1L || chapterId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val reminder = db.studyReminderDao().getReminderById(reminderId)
                val chapter = db.chapterDao().getChapterByIdSync(chapterId)
                val course = db.courseDao().getCourseByIdSync(courseId)

                if (reminder == null || !reminder.enabled) {
                    return@launch
                }

                // If chapter is already completed, cancel and stop rescheduling
                if (chapter == null || chapter.completed) {
                    db.studyReminderDao().disableRemindersForChapter(chapterId)
                    ReminderScheduler.cancelReminder(context, reminderId)
                    return@launch
                }

                // Chapter is incomplete: Deliver notification
                val courseName = course?.title ?: "Study Course"
                val notificationTitle = "${reminder.notificationTitle} — ${chapter.title}"
                val notificationMessage = "${reminder.notificationMessage} ($courseName)"

                NotificationHelper.showReminderNotification(
                    context = context,
                    notificationId = reminderId.toInt(),
                    title = notificationTitle,
                    message = notificationMessage,
                    courseId = courseId,
                    chapterId = chapterId
                )

                // Reschedule if repeating until completion
                if (reminder.repeatEnabled && reminder.repeatIntervalMinutes > 0) {
                    val nextScheduledTime = System.currentTimeMillis() + (reminder.repeatIntervalMinutes * 60 * 1000)
                    val updatedReminder = reminder.copy(scheduledAt = nextScheduledTime)
                    db.studyReminderDao().updateReminder(updatedReminder)
                    ReminderScheduler.scheduleReminder(context, updatedReminder)
                } else {
                    // One-shot: disable
                    db.studyReminderDao().updateReminder(reminder.copy(enabled = false))
                }
            } catch (_: Exception) {
                // Ignore background errors
            } finally {
                pendingResult.finish()
            }
        }
    }
}
