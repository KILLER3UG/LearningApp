package com.selfproject.learningapp.data

import com.selfproject.learningapp.data.local.FlashcardEntity
import com.selfproject.learningapp.data.local.QuizEntity

/**
 * Parses AI responses into structured flashcards and quiz items.
 */
object StudyItemParser {

    /**
     * Parses AI flashcard response into FlashcardEntity list.
     * Expected format:
     * Q: [question]
     * A: [answer]
     */
    fun parseFlashcards(
        response: String,
        documentUri: String
    ): List<FlashcardEntity> {
        val flashcards = mutableListOf<FlashcardEntity>()
        val lines = response.split("\n")
        var currentQuestion: String? = null

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("Q:") || trimmed.startsWith("Q.")) {
                currentQuestion = if (trimmed.contains(":")) {
                    trimmed.substringAfter(":").trim()
                } else {
                    trimmed.substringAfter(".").trim()
                }
            } else if ((trimmed.startsWith("A:") || trimmed.startsWith("A.")) && currentQuestion != null) {
                val answer = if (trimmed.contains(":")) {
                    trimmed.substringAfter(":").trim()
                } else {
                    trimmed.substringAfter(".").trim()
                }
                flashcards.add(
                    FlashcardEntity(
                        documentUri = documentUri,
                        question = currentQuestion!!,
                        answer = answer,
                    )
                )
                currentQuestion = null
            }
        }

        return flashcards
    }

    /**
     * Parses AI quiz response into QuizEntity list.
     * Expected format:
     * Q1: [question]
     * A: [answer]
     * Explanation: [explanation]
     */
    fun parseQuizzes(
        response: String,
        documentUri: String,
        sourceText: String
    ): List<QuizEntity> {
        val quizzes = mutableListOf<QuizEntity>()
        val lines = response.split("\n")
        var currentQuestion: String? = null
        var currentAnswer: String? = null
        var currentExplanation = StringBuilder()
        var inExplanation = false

        fun flushCurrent() {
            if (currentQuestion != null && currentAnswer != null) {
                quizzes.add(
                    QuizEntity(
                        documentUri = documentUri,
                        sourceText = sourceText,
                        question = currentQuestion!!,
                        answer = currentAnswer!!,
                        explanation = currentExplanation.toString().trim(),
                    )
                )
            }
            currentQuestion = null
            currentAnswer = null
            currentExplanation.clear()
            inExplanation = false
        }

        for (line in lines) {
            val trimmed = line.trim()

            // New question
            if (trimmed.matches(Regex("Q\\d*[:.)].*")) || trimmed == "Q:") {
                flushCurrent()
                currentQuestion = trimmed.substringAfter(":").trim()
            } else if (trimmed.startsWith("A:") || trimmed.startsWith("Answer:")) {
                if (trimmed.startsWith("A:")) {
                    currentAnswer = trimmed.substringAfter(":").trim()
                } else {
                    currentAnswer = trimmed.substringAfter(":").trim()
                }
                inExplanation = false
            } else if (trimmed.lowercase().startsWith("explanation") ||
                trimmed.lowercase().startsWith("why") ||
                trimmed.lowercase().startsWith("because")
            ) {
                inExplanation = true
                currentExplanation.append(trimmed).append("\n")
            } else if (inExplanation && currentAnswer != null) {
                currentExplanation.append(trimmed).append("\n")
            } else if (currentQuestion != null && currentAnswer == null) {
                // Multi-line question
                currentQuestion += " " + trimmed
            } else if (currentAnswer != null && !inExplanation) {
                // Multi-line answer
                currentAnswer += " " + trimmed
            }
        }

        // Flush last item
        flushCurrent()

        return quizzes
    }

    /**
     * Extracts key topics from a document for flashcard generation context.
     */
    fun extractKeyTopics(content: String, maxTopics: Int = 5): List<String> {
        val lines = content.split("\n")
        val topics = mutableListOf<String>()

        // Extract headings
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") && trimmed.any { it != '#' }) {
                val heading = trimmed.trimStart('#').trim()
                if (heading.isNotEmpty()) {
                    topics.add(heading)
                }
            }
        }

        // If not enough headings, extract bold terms
        if (topics.size < maxTopics) {
            val boldPattern = Regex("\\*\\*(.+?)\\*\\*")
            for (match in boldPattern.findAll(content)) {
                val term = match.groupValues[1].trim()
                if (term.length in 3..50 && !topics.contains(term)) {
                    topics.add(term)
                }
                if (topics.size >= maxTopics) break
            }
        }

        return topics.take(maxTopics)
    }
}
