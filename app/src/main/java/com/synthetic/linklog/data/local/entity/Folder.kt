package com.synthetic.linklog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long?, // null for 'Uncategorized' folders
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
)
