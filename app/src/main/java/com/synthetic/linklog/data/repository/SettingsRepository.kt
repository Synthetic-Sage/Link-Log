package com.synthetic.linklog.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val YOUTUBE_API_KEY = stringPreferencesKey("youtube_api_key")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val DOWNLOAD_FORMAT = stringPreferencesKey("download_format")
        val SAVE_LOCATION_URI = stringPreferencesKey("save_location_uri")
        val IS_FIRST_LAUNCH = androidx.datastore.preferences.core.booleanPreferencesKey("is_first_launch")
        val IS_DARK_THEME = androidx.datastore.preferences.core.booleanPreferencesKey("is_dark_theme")
    }

    val youtubeApiKey: Flow<String?> = dataStore.data.map { it[YOUTUBE_API_KEY] }
    val videoQuality: Flow<String> = dataStore.data.map { it[VIDEO_QUALITY] ?: "best" } // e.g., "720p", "1080p", "best"
    val audioQuality: Flow<String> = dataStore.data.map { it[AUDIO_QUALITY] ?: "best" }
    val downloadFormat: Flow<String> = dataStore.data.map { it[DOWNLOAD_FORMAT] ?: "mp4" }
    val saveLocationUri: Flow<String?> = dataStore.data.map { it[SAVE_LOCATION_URI] }
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { it[IS_FIRST_LAUNCH] ?: true }
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { it[IS_DARK_THEME] ?: true }

    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { prefs -> prefs[IS_FIRST_LAUNCH] = false }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { prefs -> prefs[IS_DARK_THEME] = isDark }
    }

    suspend fun setYoutubeApiKey(key: String?) {
        dataStore.edit { prefs ->
            if (key == null) prefs.remove(YOUTUBE_API_KEY) else prefs[YOUTUBE_API_KEY] = key
        }
    }

    suspend fun setVideoQuality(quality: String) {
        dataStore.edit { prefs -> prefs[VIDEO_QUALITY] = quality }
    }

    suspend fun setAudioQuality(quality: String) {
        dataStore.edit { prefs -> prefs[AUDIO_QUALITY] = quality }
    }

    suspend fun setDownloadFormat(format: String) {
        dataStore.edit { prefs -> prefs[DOWNLOAD_FORMAT] = format }
    }

    suspend fun setSaveLocationUri(uri: String?) {
        dataStore.edit { prefs ->
            if (uri == null) prefs.remove(SAVE_LOCATION_URI) else prefs[SAVE_LOCATION_URI] = uri
        }
    }
}
