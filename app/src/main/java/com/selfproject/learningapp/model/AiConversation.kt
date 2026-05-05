package com.selfproject.learningapp.model

/**
 * A single conversation in the AI chat.
 */
data class AiConversation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Wrapper for ChatMessage with conversation context.
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val flashcards: List<com.selfproject.learningapp.data.local.FlashcardEntity> = emptyList(),
    val quizzes: List<com.selfproject.learningapp.data.local.QuizEntity> = emptyList(),
)

enum class MessageType { TEXT, FLASHCARD_GENERATED, QUIZ_GENERATED }
enum class Role { USER, AI }
