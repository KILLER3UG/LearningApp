package com.selfproject.learningapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Update
    suspend fun update(bookmark: BookmarkEntity)

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE documentUri = :documentUri ORDER BY timestamp ASC")
    fun getByDocument(documentUri: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE documentUri = :documentUri ORDER BY timestamp ASC")
    suspend fun getByDocumentSync(documentUri: String): List<BookmarkEntity>

    @Query("DELETE FROM bookmarks WHERE documentUri = :documentUri")
    suspend fun deleteByDocument(documentUri: String)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BookmarkEntity>>
}
