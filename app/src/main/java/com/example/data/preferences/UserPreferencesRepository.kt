package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "study_os_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode") // "DARK", "LIGHT", "SYSTEM"
        val YOUTUBE_API_KEY = stringPreferencesKey("youtube_api_key")
        val DEFAULT_REPEAT_INTERVAL = longPreferencesKey("default_repeat_interval_minutes") // 180, 360, 1440, etc.
        val AUTO_RESUME_ENABLED = booleanPreferencesKey("auto_resume_enabled")
        val DAILY_CHAPTER_GOAL = intPreferencesKey("daily_chapter_goal")
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "DARK"
    }

    val youtubeApiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val saved = preferences[PreferencesKeys.YOUTUBE_API_KEY]
        if (!saved.isNullOrBlank()) {
            saved
        } else {
            com.example.BuildConfig.YOUTUBE_API_KEY
        }
    }

    val defaultRepeatIntervalFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_REPEAT_INTERVAL] ?: 180L // default 3 hours
    }

    val autoResumeEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_RESUME_ENABLED] ?: true
    }

    val dailyGoalFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_CHAPTER_GOAL] ?: 2
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setYoutubeApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.YOUTUBE_API_KEY] = key.trim()
        }
    }

    suspend fun setDefaultRepeatInterval(minutes: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REPEAT_INTERVAL] = minutes
        }
    }

    suspend fun setAutoResumeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_RESUME_ENABLED] = enabled
        }
    }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_CHAPTER_GOAL] = goal
        }
    }
}
