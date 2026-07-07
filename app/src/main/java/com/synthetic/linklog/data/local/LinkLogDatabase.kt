package com.synthetic.linklog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.synthetic.linklog.data.local.dao.FolderDao
import com.synthetic.linklog.data.local.dao.LinkDao
import com.synthetic.linklog.data.local.entity.Folder
import com.synthetic.linklog.data.local.entity.Link

import com.synthetic.linklog.data.local.entity.Group
import com.synthetic.linklog.data.local.dao.GroupDao
import com.synthetic.linklog.data.local.entity.DownloadedVideo
import com.synthetic.linklog.data.local.dao.DownloadedVideoDao

@Database(
    entities = [Group::class, Folder::class, Link::class, DownloadedVideo::class],
    version = 2,
    exportSchema = false
)
abstract class LinkLogDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun folderDao(): FolderDao
    abstract fun linkDao(): LinkDao
    abstract fun downloadedVideoDao(): DownloadedVideoDao
}
