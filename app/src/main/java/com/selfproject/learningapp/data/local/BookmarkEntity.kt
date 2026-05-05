package com.selfproject.learningapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentUri: String,
    val heading: String,
    val position: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
)
