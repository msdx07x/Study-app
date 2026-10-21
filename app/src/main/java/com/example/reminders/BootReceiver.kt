package com.example.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val activeReminders = db.studyReminderDao().getActiveReminders()
                val now = System.currentTimeMillis()

                for (reminder in activeReminders) {
                    val chapter = db.chapterDao().getChapterByIdSync(reminder.chapterId)
                    if (chapter == null || chapter.completed) {
                        db.studyReminderDao().disableRemindersForChapter(reminder.chapterId)
                        continue
                    }

                    var targetTime = reminder.scheduledAt
                    if (targetTime < now) {
                        if (reminder.repeatEnabled && reminder.repeatIntervalMinutes > 0) {
                            // Catch up to next future interval
                            val intervalMs = reminder.repeatIntervalMinutes * 60 * 1000
                            while (targetTime < now) {
                                targetTime += intervalMs
                            }
                            val updated = reminder.copy(scheduledAt = targetTime)
                            db.studyReminderDao().updateReminder(updated)
                            ReminderScheduler.scheduleReminder(context, updated)
                        } else {
                            // Expired one-shot alarm
                            db.studyReminderDao().updateReminder(reminder.copy(enabled = false))
                        }
                    } else {
                        ReminderScheduler.scheduleReminder(context, reminder)
                    }
                }
            } catch (_: Exception) {
                // Ignore
            } finally {
                pendingResult.finish()
            }
        }
    }
}
