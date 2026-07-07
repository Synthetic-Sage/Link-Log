package com.synthetic.linklog.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synthetic.linklog.data.local.entity.DownloadedVideo
import com.synthetic.linklog.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _videoData = MutableStateFlow<DownloadedVideo?>(null)
    val videoData: StateFlow<DownloadedVideo?> = _videoData.asStateFlow()

    fun loadVideo(linkId: Long) {
        viewModelScope.launch {
            _videoData.value = downloadRepository.getDownload(linkId)
        }
    }

    fun savePlaybackPosition(linkId: Long, positionMs: Long) {
        viewModelScope.launch {
            downloadRepository.updatePlaybackPosition(linkId, positionMs)
        }
    }
}
