package com.synthetic.linklog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "downloaded_videos",
    foreignKeys = [
        ForeignKey(
            entity = Link::class,
            parentColumns = ["id"],
            childColumns = ["linkId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DownloadedVideo(
    @PrimaryKey val linkId: Long, // One-to-One relationship with Link
    val localUri: String? = null,
    val downloadProgress: Int = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val durationMs: Long = 0,
    val playbackPositionMs: Long = 0,
    val fileSize: Long = 0,
    val downloadDate: Long = System.currentTimeMillis()
)
