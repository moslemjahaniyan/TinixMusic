package com.tinixmusic.tinixmusic2



import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingsRepository {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val DEFAULT_QUALITY_KEY = stringPreferencesKey("default_quality")

    fun isDarkMode(context: Context): Flow<Boolean> {
        return context.settingsDataStore.data.map { prefs ->
            prefs[DARK_MODE_KEY] ?: false
        }
    }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    fun getDefaultQuality(context: Context): Flow<String> {
        return context.settingsDataStore.data.map { prefs ->
            prefs[DEFAULT_QUALITY_KEY] ?: "320"
        }
    }

    suspend fun setDefaultQuality(context: Context, quality: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[DEFAULT_QUALITY_KEY] = quality
        }
    }
}
