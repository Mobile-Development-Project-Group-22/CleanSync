package com.example.cleansync.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ThemePreferenceManager {

    private const val PREFERENCES_NAME = "user_preferences"
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    private val Context.dataStore by preferencesDataStore(name = PREFERENCES_NAME)

    suspend fun saveThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }

    fun getThemeMode(context: Context): Flow<ThemeMode> {
        return context.dataStore.data.map { prefs ->
            val saved = prefs[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(saved)
        }
    }
}
