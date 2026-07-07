package com.synthetic.linklog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.synthetic.linklog.data.local.entity.DownloadStatus
import com.synthetic.linklog.data.local.entity.DownloadedVideo
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedVideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(downloadedVideo: DownloadedVideo)

    @Update
    suspend fun update(downloadedVideo: DownloadedVideo)

    @Query("SELECT * FROM downloaded_videos WHERE linkId = :linkId")
    suspend fun getByLinkId(linkId: Long): DownloadedVideo?

    @Query("SELECT * FROM downloaded_videos WHERE linkId = :linkId")
    fun observeByLinkId(linkId: Long): Flow<DownloadedVideo?>

    @Query("SELECT * FROM downloaded_videos ORDER BY downloadDate DESC")
    fun observeAllDownloads(): Flow<List<DownloadedVideo>>

    @Query("UPDATE downloaded_videos SET downloadProgress = :progress, status = :status WHERE linkId = :linkId")
    suspend fun updateProgress(linkId: Long, progress: Int, status: DownloadStatus)

    @Query("UPDATE downloaded_videos SET playbackPositionMs = :position WHERE linkId = :linkId")
    suspend fun updatePlaybackPosition(linkId: Long, position: Long)

    @Query("DELETE FROM downloaded_videos WHERE linkId = :linkId")
    suspend fun deleteByLinkId(linkId: Long)
}
