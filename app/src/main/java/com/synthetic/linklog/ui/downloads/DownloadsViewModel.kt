package com.synthetic.linklog.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synthetic.linklog.data.local.entity.DownloadStatus
import com.synthetic.linklog.data.local.entity.DownloadedVideo
import com.synthetic.linklog.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    val allDownloads = downloadRepository.observeAllDownloads()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeDownloads: StateFlow<List<DownloadedVideo>> = allDownloads.map { list ->
        list.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.QUEUED }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val completedDownloads: StateFlow<List<DownloadedVideo>> = allDownloads.map { list ->
        list.filter { it.status == DownloadStatus.COMPLETED }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteDownload(linkId: Long) {
        viewModelScope.launch {
            downloadRepository.deleteDownloadRecord(linkId)
            // Note: Should also delete the actual file from disk if we own it
        }
    }
}
