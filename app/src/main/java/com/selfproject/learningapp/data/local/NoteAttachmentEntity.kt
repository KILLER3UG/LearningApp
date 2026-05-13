package com.selfproject.learningapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A file attachment belonging to a specific note/document.
 * Stored via SAF content:// URI; cached content holds extracted text.
 */
@Entity(tableName = "note_attachments")
data class NoteAttachmentEntity(
    @PrimaryKey
    val id: String,

    /** The document this attachment belongs to (content:// URI string) */
    @ColumnInfo(name = "note_uri")
    val noteUri: String,

    /** Human-readable file name */
    @ColumnInfo(name = "file_name")
    val fileName: String,

    /** FileType.name string for icon lookup */
    @ColumnInfo(name = "file_type")
    val fileType: String,

    /** Bytes, used for display */
    @ColumnInfo(name = "file_size_bytes")
    val fileSizeBytes: Long,

    /** Actual MIME type from content resolver */
    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    /** SAF content:// URI string */
    @ColumnInfo(name = "file_uri")
    val fileUri: String,

    /** Epoch millis of upload */
    @ColumnInfo(name = "upload_timestamp")
    val uploadTimestamp: Long,

    /** Extracted text content (null for binary files until parsed) */
    @ColumnInfo(name = "cached_content")
    val cachedContent: String? = null
)