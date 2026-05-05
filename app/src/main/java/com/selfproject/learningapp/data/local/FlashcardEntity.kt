package com.selfproject.learningapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentUri: String,
    val question: String,
    val answer: String,
    val nextReview: Long = System.currentTimeMillis(),
    val interval: Long = 1,
    val easeFactor: Double = 2.5,
    val repetitions: Int = 0,
)
