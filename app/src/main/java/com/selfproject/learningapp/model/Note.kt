package com.selfproject.learningapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A note/study document. May have subject tags, be pinned, and have attachments.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String,  // content:// URI string

    /** Human-readable title (file name or user-defined) */
    val title: String,

    /** First ~200 characters of content for preview */
    val preview: String,

    /** Subject/tag labels */
    val subjects: List<String> = emptyList(),

    /** Pinned to top of note list */
    val pinned: Boolean = false,

    /** Number of file attachments */
    val attachmentCount: Int = 0,

    /** Epoch millis of last modification */
    val updatedAt: Long = System.currentTimeMillis()
)