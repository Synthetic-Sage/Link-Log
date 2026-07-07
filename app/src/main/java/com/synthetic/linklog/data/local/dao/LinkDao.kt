package com.synthetic.linklog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.synthetic.linklog.data.local.entity.Link
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {
    @Query("SELECT * FROM links WHERE folderId = :folderId ORDER BY userRank ASC")
    fun getLinksByFolderId(folderId: Long): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE (folderId = :folderId OR (folderId IS NULL AND :folderId IS NULL)) ORDER BY userRank ASC")
    suspend fun getLinksSnapshotForFolder(folderId: Long?): List<Link>

    @Query("SELECT * FROM links WHERE folderId IS NULL ORDER BY userRank ASC")
    fun getUncategorizedLinks(): Flow<List<Link>>

    @Query("SELECT * FROM links ORDER BY timestamp DESC")
    fun getAllLinks(): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE id = :id")
    suspend fun getLinkById(id: Long): Link?

    @Query("SELECT MAX(userRank) FROM links WHERE (folderId = :folderId OR (folderId IS NULL AND :folderId IS NULL))")
    suspend fun getMaxUserRankForFolder(folderId: Long?): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: Link): Long

    @Update
    suspend fun updateLink(link: Link)

    @Delete
    suspend fun deleteLink(link: Link)
    
    @Update
    suspend fun updateLinks(links: List<Link>) // Useful for drag-and-drop reordering
}
