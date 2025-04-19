package com.example.cleansync.ui.profile

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ThemePreferenceManager {
    private const val PREFERENCES_NAME = "user_preferences"
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    private val Context.dataStore by preferencesDataStore(name = PREFERENCES_NAME)

    suspend fun saveDarkMode(context: Context, isDarkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = isDarkMode
        }
    }

    fun getDarkMode(context: Context): Flow<Boolean> {
        return context.dataStore.data
            .map { prefs -> prefs[DARK_MODE_KEY] ?: false }
    }
}
