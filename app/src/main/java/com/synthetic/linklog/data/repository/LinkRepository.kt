package com.synthetic.linklog.data.repository

import com.synthetic.linklog.data.local.dao.FolderDao
import com.synthetic.linklog.data.local.dao.LinkDao
import com.synthetic.linklog.data.local.entity.Folder
import com.synthetic.linklog.data.local.entity.Link
import com.synthetic.linklog.data.local.dao.GroupDao
import com.synthetic.linklog.data.local.entity.Group
import com.synthetic.linklog.util.LinkScraper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepository @Inject constructor(
    private val groupDao: GroupDao,
    private val linkDao: LinkDao,
    private val folderDao: FolderDao,
    private val settingsRepository: SettingsRepository
) {
    fun getAllGroups(): Flow<List<Group>> = groupDao.getAllGroups()
    
    fun getFoldersForGroup(groupId: Long?): Flow<List<Folder>> = folderDao.getFoldersByGroupId(groupId)

    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()
    
    fun getLinksForFolder(folderId: Long): Flow<List<Link>> = linkDao.getLinksByFolderId(folderId)

    fun getAllLinksFlow(): Flow<List<Link>> = linkDao.getAllLinks()

    suspend fun addGroup(name: String): Long {
        return groupDao.insertGroup(Group(name = name))
    }

    suspend fun addFolder(name: String, groupId: Long? = null): Long {
        return folderDao.insertFolder(Folder(name = name, groupId = groupId))
    }

    suspend fun deleteFolder(folder: Folder) {
        folderDao.deleteFolder(folder)
    }

    suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folder)
    }

    suspend fun saveLink(url: String, folderId: Long?) {
        // Scrape metadata
        val apiKey = settingsRepository.youtubeApiKey.firstOrNull()
        val metadata = LinkScraper.scrape(url, apiKey)
        
        // Calculate userRank (insert at top)
        val currentMaxRank = linkDao.getMaxUserRankForFolder(folderId) ?: 0
        
        val link = Link(
            folderId = folderId,
            url = metadata.url,
            title = metadata.title ?: "Unknown Title",
            description = metadata.description,
            imageUrl = metadata.imageUrl,
            userRank = currentMaxRank + 1
        )
        linkDao.insertLink(link)
    }

    suspend fun getLinkById(id: Long): Link? = linkDao.getLinkById(id)

    suspend fun updateLink(link: Link) {
        linkDao.updateLink(link)
    }

    suspend fun updateLinkRankAndShift(link: Link, newRank: Int) {
        val allLinks = linkDao.getLinksSnapshotForFolder(link.folderId).toMutableList()
        val currentIndex = allLinks.indexOfFirst { it.id == link.id }
        if (currentIndex != -1) {
            allLinks.removeAt(currentIndex)
            
            // Snap to nearest valid position (1-indexed)
            val targetIndex = (newRank - 1).coerceIn(0, allLinks.size)
            allLinks.add(targetIndex, link)
            
            // Re-assign ranks sequentially
            val updatedLinks = allLinks.mapIndexed { index, l -> 
                l.copy(userRank = index + 1) 
            }
            linkDao.updateLinks(updatedLinks)
        }
    }

    suspend fun updateCustomTitle(link: Link, customTitle: String?) {
        linkDao.updateLink(link.copy(customTitle = customTitle))
    }

    suspend fun deleteLink(link: Link) {
        linkDao.deleteLink(link)
    }

    /** Re-inserts a previously deleted link (used for snackbar Undo). */
    suspend fun restoreLink(link: Link) {
        linkDao.insertLink(link)
    }

    /** Re-inserts a previously deleted folder (used for snackbar Undo). */
    suspend fun restoreFolder(folder: Folder) {
        folderDao.insertFolder(folder)
    }
}
