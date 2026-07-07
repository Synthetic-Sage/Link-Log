package com.synthetic.linklog.data.repository

import com.synthetic.linklog.data.local.dao.DownloadedVideoDao
import com.synthetic.linklog.data.local.entity.DownloadStatus
import com.synthetic.linklog.data.local.entity.DownloadedVideo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadedVideoDao: DownloadedVideoDao
) {
    fun observeAllDownloads(): Flow<List<DownloadedVideo>> = downloadedVideoDao.observeAllDownloads()

    fun observeDownload(linkId: Long): Flow<DownloadedVideo?> = downloadedVideoDao.observeByLinkId(linkId)

    suspend fun getDownload(linkId: Long): DownloadedVideo? = downloadedVideoDao.getByLinkId(linkId)

    suspend fun insertOrUpdate(downloadedVideo: DownloadedVideo) {
        downloadedVideoDao.insertOrUpdate(downloadedVideo)
    }

    suspend fun updateProgress(linkId: Long, progress: Int, status: DownloadStatus) {
        downloadedVideoDao.updateProgress(linkId, progress, status)
    }

    suspend fun updatePlaybackPosition(linkId: Long, positionMs: Long) {
        downloadedVideoDao.updatePlaybackPosition(linkId, positionMs)
    }

    suspend fun deleteDownloadRecord(linkId: Long) {
        downloadedVideoDao.deleteByLinkId(linkId)
    }
}
