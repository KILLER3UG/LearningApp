package com.selfproject.learningapp.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.selfproject.learningapp.model.AiModel

/**
 * Handles AI interactions via Google Gemini/Gemma models.
 */
class AiRepository(private val apiKey: String) {

    @Volatile
    private var currentModelId: String = "gemma-3-27b-it"

    @Volatile
    private var cachedModel: GenerativeModel? = null

    private val generativeModel: GenerativeModel
        get() {
            val cached = cachedModel
            if (cached != null) return cached

            val newModel = GenerativeModel(
                modelName = currentModelId,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 64
                    topP = 0.95f
                }
            )
            cachedModel = newModel
            return newModel
        }

    /**
     * Sets the active model. Recreates the model instance on next call.
     */
    fun setModel(model: AiModel) {
        if (currentModelId != model.id) {
            currentModelId = model.id
            cachedModel = null
        }
    }

    /**
     * Gets the current active model.
     */
    fun getCurrentModelId(): String = currentModelId

    /**
     * System prompt for pedagogical, study-focused responses.
     */
    private val systemPrompt = """
        You are a helpful study assistant. Your role is to help students understand 
        concepts deeply and retain knowledge effectively.

        Guidelines for responses:
        - Be clear, concise, and well-structured
        - Use examples and analogies when helpful
        - Highlight key terms and concepts
        - Break down complex ideas into simpler parts
        - Provide connections to related concepts when relevant
        - Use formatting (headings, bullet points, numbered lists) for readability

        When asked to explain:
        1. Start with a direct answer
        2. Provide context and background
        3. Use examples or analogies
        4. Summarize key takeaways

        When asked to test or quiz:
        1. Generate questions of varying difficulty
        2. Provide answers after the questions
        3. Explain why each answer is correct

        Respond in markdown format.
    """.trimIndent()

    /**
     * Sends a query to the AI and returns the response as a Flow.
     * Falls back to non-streaming if streaming fails.
     */
    fun queryAi(
        selectedText: String,
        context: String,
        promptTemplate: String = "Explain this clearly"
    ): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flow {
        val fullPrompt = buildPrompt(selectedText, context, promptTemplate)
        try {
            val response = generativeModel.generateContentStream(fullPrompt)
            var accumulatedResponse = ""
            response.collect { chunk ->
                accumulatedResponse += chunk.text
                emit(accumulatedResponse)
            }
        } catch (e: Exception) {
            // Fall back to non-streaming if streaming fails
            try {
                val response = generativeModel.generateContent(fullPrompt)
                emit(response.text ?: "Error: Empty response from AI")
            } catch (e2: Exception) {
                emit("Error: AI request failed - ${e2.localizedMessage}")
            }
        }
    }

    /**
     * Sends a direct prompt without context wrapping (for full-document chat).
     * Falls back to non-streaming if streaming fails.
     */
    fun queryAiDirect(
        prompt: String
    ): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flow {
        try {
            val response = generativeModel.generateContentStream(prompt)
            var accumulatedResponse = ""
            response.collect { chunk ->
                accumulatedResponse += chunk.text
                emit(accumulatedResponse)
            }
        } catch (e: Exception) {
            // Fall back to non-streaming if streaming fails
            try {
                val response = generativeModel.generateContent(prompt)
                emit(response.text ?: "Error: Empty response from AI")
            } catch (e2: Exception) {
                emit("Error: AI request failed - ${e2.localizedMessage}")
            }
        }
    }

    /**
     * Builds the full prompt with context and system instructions.
     * Visible for testing.
     */
    internal fun buildPrompt(
        selectedText: String,
        surroundingContext: String,
        promptTemplate: String
    ): String {
        return """
            $systemPrompt

            CONTEXT FROM DOCUMENT:
            $surroundingContext

            SELECTED TEXT:
            $selectedText

            USER REQUEST:
            $promptTemplate

            Please provide a helpful, study-focused response to the user's request about the selected text, considering the surrounding context.
        """.trimIndent()
    }

    /**
     * Test-visible wrapper for buildPrompt.
     */
    fun buildPromptForTest(
        selectedText: String,
        surroundingContext: String,
        promptTemplate: String
    ): String = buildPrompt(selectedText, surroundingContext, promptTemplate)

    /**
     * Test-visible wrapper for flashcard prompt.
     */
    fun buildFlashcardsPrompt(content: String): String {
        return """
            Based on the following text, generate 5 question-answer flashcards.
            Format each as:
            Q: [question]
            A: [answer]

            Text:
            $content
        """.trimIndent()
    }

    /**
     * Test-visible wrapper for quiz prompt.
     */
    fun buildQuizPrompt(content: String): String {
        return """
            Based on the following text, generate 3 self-check questions of increasing difficulty.
            Include answers and explanations.

            Text:
            $content
        """.trimIndent()
    }

    /**
     * Generates flashcards from document content.
     */
    suspend fun generateFlashcards(content: String): String {
        val prompt = buildFlashcardsPrompt(content)
        val response = generativeModel.generateContent(prompt)
        return response.text ?: ""
    }

    /**
     * Generates a self-check quiz from document content.
     */
    suspend fun generateQuiz(content: String): String {
        val prompt = buildQuizPrompt(content)
        val response = generativeModel.generateContent(prompt)
        return response.text ?: ""
    }
}
