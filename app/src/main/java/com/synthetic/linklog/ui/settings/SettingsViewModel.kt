package com.synthetic.linklog.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synthetic.linklog.data.repository.SettingsRepository
import com.synthetic.linklog.util.YoutubeDlManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = settingsRepository.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val youtubeApiKey: StateFlow<String?> = settingsRepository.youtubeApiKey
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
        
    val videoQuality: StateFlow<String> = settingsRepository.videoQuality
        .stateIn(viewModelScope, SharingStarted.Lazily, "best")
        
    val audioQuality: StateFlow<String> = settingsRepository.audioQuality
        .stateIn(viewModelScope, SharingStarted.Lazily, "best")
        
    val downloadFormat: StateFlow<String> = settingsRepository.downloadFormat
        .stateIn(viewModelScope, SharingStarted.Lazily, "mp4")

    val saveLocationUri: StateFlow<String?> = settingsRepository.saveLocationUri
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkTheme(isDark) }
    }

    fun setYoutubeApiKey(key: String) {
        viewModelScope.launch { settingsRepository.setYoutubeApiKey(if (key.isBlank()) null else key) }
    }

    fun setVideoQuality(quality: String) {
        viewModelScope.launch { settingsRepository.setVideoQuality(quality) }
    }

    fun setAudioQuality(quality: String) {
        viewModelScope.launch { settingsRepository.setAudioQuality(quality) }
    }

    fun setDownloadFormat(format: String) {
        viewModelScope.launch { settingsRepository.setDownloadFormat(format) }
    }

    fun setSaveLocationUri(uri: String) {
        viewModelScope.launch { settingsRepository.setSaveLocationUri(uri) }
    }
    
    fun updateYoutubeDl() {
        viewModelScope.launch {
            // Trigger yt-dlp update mechanism
            // For now just logging or showing toast in UI
        }
    }
}
