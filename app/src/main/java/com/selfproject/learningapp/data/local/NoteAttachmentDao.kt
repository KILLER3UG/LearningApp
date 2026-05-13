package com.selfproject.learningapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteAttachmentDao {

    @Query("SELECT * FROM note_attachments WHERE note_uri = :noteUri ORDER BY upload_timestamp DESC")
    fun getAttachmentsForNote(noteUri: String): Flow<List<NoteAttachmentEntity>>

    @Query("SELECT COUNT(*) FROM note_attachments WHERE note_uri = :noteUri")
    fun getAttachmentCount(noteUri: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: NoteAttachmentEntity)

    @Delete
    suspend fun deleteAttachment(attachment: NoteAttachmentEntity)

    @Query("DELETE FROM note_attachments WHERE note_uri = :noteUri")
    suspend fun deleteAllForNote(noteUri: String)
}