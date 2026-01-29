package com.bhanu.rupeegarden.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bhanu.rupeegarden.data.model.Achievement
import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rupee_garden")

class RupeeGardenDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private val USER_PROGRESS_KEY = stringPreferencesKey("user_progress")
        private val ENTRIES_KEY = stringPreferencesKey("entries")
        private val ACTIVE_SESSION_KEY = stringPreferencesKey("active_session")
        private val ACHIEVEMENTS_KEY = stringPreferencesKey("achievements")
    }

    // User Progress
    val userProgress: Flow<UserProgress> = context.dataStore.data.map { preferences ->
        preferences[USER_PROGRESS_KEY]?.let {
            json.decodeFromString<UserProgress>(it)
        } ?: UserProgress()
    }

    suspend fun saveUserProgress(progress: UserProgress) {
        context.dataStore.edit { preferences ->
            preferences[USER_PROGRESS_KEY] = json.encodeToString(progress)
        }
    }

    // Entries
    val entries: Flow<List<DayEntry>> = context.dataStore.data.map { preferences ->
        preferences[ENTRIES_KEY]?.let {
            json.decodeFromString<List<DayEntry>>(it)
        } ?: emptyList()
    }

    suspend fun saveEntries(entries: List<DayEntry>) {
        context.dataStore.edit { preferences ->
            preferences[ENTRIES_KEY] = json.encodeToString(entries)
        }
    }

    // Active Session
    val activeSession: Flow<DayEntry?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_SESSION_KEY]?.let {
            json.decodeFromString<DayEntry>(it)
        }
    }

    suspend fun saveActiveSession(entry: DayEntry?) {
        context.dataStore.edit { preferences ->
            if (entry != null) {
                preferences[ACTIVE_SESSION_KEY] = json.encodeToString(entry)
            } else {
                preferences.remove(ACTIVE_SESSION_KEY)
            }
        }
    }

    suspend fun clearActiveSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACTIVE_SESSION_KEY)
        }
    }

    // Achievements
    val achievements: Flow<List<Achievement>> = context.dataStore.data.map { preferences ->
        preferences[ACHIEVEMENTS_KEY]?.let {
            json.decodeFromString<List<Achievement>>(it)
        } ?: emptyList()
    }

    suspend fun saveAchievements(achievements: List<Achievement>) {
        context.dataStore.edit { preferences ->
            preferences[ACHIEVEMENTS_KEY] = json.encodeToString(achievements)
        }
    }
}
