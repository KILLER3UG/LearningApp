package com.selfproject.learningapp.model

/**
 * Represents a loaded markdown document with its content and metadata.
 */
data class Document(
    val uri: String,
    val displayName: String,
    val content: String,
    val lastModified: Long = System.currentTimeMillis(),
)

/**
 * Represents an AI response saved alongside a document.
 */
data class AIResponse(
    val id: Long = 0,
    val documentUri: String,
    val selectedText: String,
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * Represents a bookmarked heading/position in a document.
 */
data class Bookmark(
    val id: Long = 0,
    val documentUri: String,
    val heading: String,
    val position: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * Represents a flashcard generated from AI analysis.
 */
data class Flashcard(
    val id: Long = 0,
    val documentUri: String,
    val question: String,
    val answer: String,
    val nextReview: Long = System.currentTimeMillis(),
    val interval: Long = 1, // days
    val easeFactor: Double = 2.5,
)

/**
 * UI state for the document viewer screen.
 */
sealed class DocumentUiState {
    object Empty : DocumentUiState()
    object Loading : DocumentUiState()
    data class Error(val message: String) : DocumentUiState()
    data class Success(val document: Document) : DocumentUiState()
}

/**
 * AI query state.
 */
sealed class AiQueryState {
    object Idle : AiQueryState()
    object Loading : AiQueryState()
    data class Error(val message: String) : AiQueryState()
    data class Success(val response: String) : AiQueryState()
}
