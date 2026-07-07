package com.synthetic.linklog.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.synthetic.linklog.data.local.entity.Folder
import com.synthetic.linklog.data.local.entity.Link
import com.synthetic.linklog.data.repository.LinkRepository
import com.synthetic.linklog.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.content.Intent
import com.synthetic.linklog.data.local.entity.Group
import com.synthetic.linklog.service.DownloadService

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // UI trigger flags (observed by screens via side-effects)
    var pendingAddGroup: Boolean by mutableStateOf(false)
    var triggerAddFolder: Boolean by mutableStateOf(false)

    val groups: StateFlow<List<Group>> = linkRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupId: StateFlow<Long?> = _selectedGroupId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val folders: StateFlow<List<Folder>> = _selectedGroupId
        .flatMapLatest { groupId -> linkRepository.getFoldersForGroup(groupId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    private val _links = MutableStateFlow<List<Link>>(emptyList())
    // Expose raw folder links if needed, or keep it private
    
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activeSearchFilter = MutableStateFlow("All")
    val activeSearchFilter = _activeSearchFilter.asStateFlow()

    fun setSearchActive(active: Boolean) { _isSearchActive.value = active }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSearchFilter(filter: String) { _activeSearchFilter.value = filter }

    val displayedLinks: StateFlow<List<Link>> = combine(
        _links, _isSearchActive, _searchQuery, _activeSearchFilter, linkRepository.getAllLinksFlow()
    ) { currentFolderLinks, isSearching, query, filter, allLinks ->
        if (!isSearching) return@combine currentFolderLinks
        
        val targetList = allLinks
        if (query.isBlank()) return@combine targetList

        val lowerQuery = query.replace(filter, "").trim().lowercase()

        targetList.filter { link ->
            val matchTitle = (link.customTitle ?: link.title ?: "").lowercase().contains(lowerQuery)
            val matchNotes = link.notes?.lowercase()?.contains(lowerQuery) == true
            val matchUrl = link.url.lowercase().contains(lowerQuery)
            
            when (filter) {
                "@title" -> matchTitle
                "@notes" -> matchNotes
                // for @folder and @group we'd need more complex joining, falling back to title/notes/url for now
                else -> matchTitle || matchNotes || matchUrl
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val isFirstLaunch = settingsRepository.isFirstLaunch.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        viewModelScope.launch {
            // Seed defaults if empty
            val currentGroups = linkRepository.getAllGroups().firstOrNull()
            if (currentGroups.isNullOrEmpty()) {
                val defaultGroupId = linkRepository.addGroup("General")
                val currentFolders = linkRepository.getAllFolders().firstOrNull()
                if (currentFolders.isNullOrEmpty()) {
                    linkRepository.addFolder("Uncategorized", defaultGroupId)
                }
            }
        }
        // Load initial links (either uncategorized or all)
        loadLinksForFolder(null)
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
        loadLinksForFolder(folderId)
    }

    private fun loadLinksForFolder(folderId: Long?) {
        viewModelScope.launch {
            if (folderId != null) {
                linkRepository.getLinksForFolder(folderId).collect {
                    _links.value = it
                }
            } else {
                // If null, we might want to load uncategorized or all links.
                // For now, let's just observe an empty list or we can add a method for "All Links"
            }
        }
    }

    fun addLink(url: String, folderId: Long?) {
        viewModelScope.launch {
            linkRepository.saveLink(url, folderId)
        }
    }

    fun addPlaylist(url: String, folderId: Long?) {
        viewModelScope.launch {
            val urls = com.synthetic.linklog.util.LinkScraper.extractPlaylistUrls(url)
            urls.forEach { videoUrl ->
                linkRepository.saveLink(videoUrl, folderId)
            }
        }
    }

    fun selectGroup(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            linkRepository.addGroup(name)
        }
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            linkRepository.addFolder(name, _selectedGroupId.value)
        }
    }

    fun updateFolder(folder: com.synthetic.linklog.data.local.entity.Folder) {
        viewModelScope.launch {
            linkRepository.updateFolder(folder)
        }
    }

    fun deleteFolder(folder: com.synthetic.linklog.data.local.entity.Folder) {
        viewModelScope.launch {
            linkRepository.deleteFolder(folder)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setFirstLaunchCompleted()
        }
    }

    fun updateLinkRank(link: Link, newRank: Int) {
        viewModelScope.launch {
            linkRepository.updateLinkRankAndShift(link, newRank)
        }
    }

    fun updateCustomTitle(link: Link, customTitle: String?) {
        viewModelScope.launch {
            linkRepository.updateCustomTitle(link, customTitle)
        }
    }

    fun deleteLink(link: Link) {
        viewModelScope.launch {
            linkRepository.deleteLink(link)
        }
    }

    fun restoreLink(link: Link) {
        viewModelScope.launch {
            linkRepository.restoreLink(link)
        }
    }

    fun restoreFolder(folder: com.synthetic.linklog.data.local.entity.Folder) {
        viewModelScope.launch {
            linkRepository.restoreFolder(folder)
        }
    }

    fun downloadMedia(context: Context, url: String, linkId: Long) {
        val intent = Intent(context, DownloadService::class.java).apply {
            putExtra(DownloadService.EXTRA_URL, url)
            putExtra(DownloadService.EXTRA_LINK_ID, linkId)
        }
        context.startForegroundService(intent)
    }
}
