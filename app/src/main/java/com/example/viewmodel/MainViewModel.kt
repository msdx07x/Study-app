package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.ChapterEntity
import com.example.data.entity.CourseEntity
import com.example.data.entity.StudyReminderEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.CourseRepository
import com.example.data.repository.ImportResult
import com.example.data.repository.YouTubeRepository
import com.example.reminders.NotificationHelper
import com.example.reminders.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val preferencesRepository = UserPreferencesRepository(application)
    private val youtubeRepository = YouTubeRepository()

    private val courseRepository = CourseRepository(
        courseDao = db.courseDao(),
        chapterDao = db.chapterDao(),
        reminderDao = db.studyReminderDao(),
        onReminderCancelled = { reminderId ->
            ReminderScheduler.cancelReminder(application, reminderId)
        }
    )

    init {
        NotificationHelper.createNotificationChannel(application)
    }

    // App Preferences
    val themeMode: StateFlow<String> = preferencesRepository.themeModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK"
    )

    val youtubeApiKey: StateFlow<String> = preferencesRepository.youtubeApiKeyFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )

    val defaultRepeatInterval: StateFlow<Long> = preferencesRepository.defaultRepeatIntervalFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 180L
    )

    val autoResumeEnabled: StateFlow<Boolean> = preferencesRepository.autoResumeEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    // Data Flows
    val allCourses: StateFlow<List<CourseEntity>> = courseRepository.allCourses.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val totalCoursesCount: StateFlow<Int> = courseRepository.totalCoursesCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val totalChaptersCount: StateFlow<Int> = courseRepository.totalChaptersCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val completedChaptersCount: StateFlow<Int> = courseRepository.completedChaptersCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val pendingChaptersCount: StateFlow<Int> = courseRepository.pendingChaptersCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    val mostRecentIncompleteChapter: StateFlow<ChapterEntity?> = courseRepository.mostRecentIncompleteChapter.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val recentlyWatchedChapters: StateFlow<List<ChapterEntity>> = courseRepository.recentlyWatchedChapters.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allReminders: StateFlow<List<StudyReminderEntity>> = courseRepository.allReminders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Import state
    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    fun clearImportError() {
        _importError.value = null
    }

    fun importPlaylistUrl(urlOrId: String, onCourseCreated: (Long) -> Unit) {
        viewModelScope.launch {
            _isImporting.value = true
            _importError.value = null

            val currentKey = youtubeApiKey.first()
            val result = youtubeRepository.importPlaylist(urlOrId, currentKey)

            when (result) {
                is ImportResult.Success -> {
                    val courseId = courseRepository.saveImportedCourse(result.course, result.chapters)
                    _isImporting.value = false
                    onCourseCreated(courseId)
                }
                is ImportResult.Error -> {
                    _isImporting.value = false
                    _importError.value = result.message
                }
            }
        }
    }

    fun loadStarterCourse(index: Int, onCourseCreated: (Long) -> Unit) {
        viewModelScope.launch {
            _isImporting.value = true
            val curatedList = youtubeRepository.getCuratedCourses()
            val (course, chapters) = curatedList.getOrElse(index) { curatedList.first() }
            val courseId = courseRepository.saveImportedCourse(course, chapters)
            _isImporting.value = false
            onCourseCreated(courseId)
        }
    }

    fun getCourseFlow(courseId: Long) = courseRepository.getCourseFlow(courseId)
    fun getChaptersFlow(courseId: Long) = courseRepository.getChaptersForCourse(courseId)
    fun getChapterFlow(chapterId: Long) = courseRepository.getChapterFlow(chapterId)

    fun updatePlaybackPosition(courseId: Long, chapterId: Long, videoId: String, posSeconds: Int) {
        viewModelScope.launch {
            courseRepository.updatePlaybackPosition(courseId, chapterId, videoId, posSeconds)
        }
    }

    fun toggleChapterCompletion(chapterId: Long, completed: Boolean) {
        viewModelScope.launch {
            courseRepository.updateChapterCompletion(chapterId, completed)
        }
    }

    fun saveChapterNotes(chapterId: Long, notes: String) {
        viewModelScope.launch {
            courseRepository.updateChapterNotes(chapterId, notes)
        }
    }

    fun scheduleReminder(reminder: StudyReminderEntity) {
        viewModelScope.launch {
            val reminderId = courseRepository.insertReminder(reminder)
            val updatedReminder = reminder.copy(id = reminderId)
            ReminderScheduler.scheduleReminder(getApplication(), updatedReminder)
        }
    }

    fun toggleReminder(reminderId: Long, enabled: Boolean) {
        viewModelScope.launch {
            courseRepository.toggleReminder(reminderId, enabled)
            if (enabled) {
                val rem = db.studyReminderDao().getReminderById(reminderId)
                if (rem != null) {
                    ReminderScheduler.scheduleReminder(getApplication(), rem)
                }
            } else {
                ReminderScheduler.cancelReminder(getApplication(), reminderId)
            }
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            courseRepository.deleteReminder(reminderId)
        }
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            courseRepository.deleteCourse(courseId)
        }
    }

    fun renameCourse(courseId: Long, newTitle: String) {
        viewModelScope.launch {
            courseRepository.renameCourse(courseId, newTitle)
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            preferencesRepository.setYoutubeApiKey(key)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDefaultRepeatInterval(minutes: Long) {
        viewModelScope.launch {
            preferencesRepository.setDefaultRepeatInterval(minutes)
        }
    }

    fun setAutoResumeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoResumeEnabled(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val activeReminders = db.studyReminderDao().getActiveReminders()
            for (rem in activeReminders) {
                ReminderScheduler.cancelReminder(getApplication(), rem.id)
            }
            courseRepository.clearAllData()
        }
    }
}
