package com.synthetic.linklog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "links",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class Link(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folderId: Long?, // null for 'Uncategorized' or top-level links
    val url: String,
    val title: String?,
    val description: String?,
    val notes: String? = null,
    val customTitle: String? = null,
    val imageUrl: String?,
    val userRank: Int, // for drag-and-drop reordering
    val timestamp: Long = System.currentTimeMillis()
)
