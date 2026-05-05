package com.selfproject.learningapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentUri: String,
    val sourceText: String,
    val question: String,
    val answer: String,
    val explanation: String = "",
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
)
