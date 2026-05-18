package com.selfproject.learningapp.data

import com.selfproject.learningapp.data.repository.ApiConfig
import com.selfproject.learningapp.data.repository.ApiProvider
import com.selfproject.learningapp.data.repository.SettingsRepository
import com.selfproject.learningapp.model.AiModel
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.util.concurrent.TimeUnit

// ── Google AI Studio ──────────────────────────────────────────

@Serializable
data class GoogleChatRequest(
    val contents: List<GoogleContent>,
    val generationConfig: GoogleGenConfig = GoogleGenConfig()
)

@Serializable
data class GoogleContent(val parts: List<GooglePart>)
@Serializable
data class GooglePart(val text: String = "", val inlineData: GoogleInlineData? = null)
@Serializable
data class GoogleInlineData(val mimeType: String = "", val data: String = "")
@Serializable
data class GoogleGenConfig(
    val temperature: Double = 0.9,
    val maxOutputTokens: Int = 8192
)

@Serializable
data class GoogleChatResponse(
    val candidates: List<GoogleCandidate>? = null
)

@Serializable
data class GoogleCandidate(val content: GoogleContent? = null)

// ── OpenAI Compatible ─────────────────────────────────────────

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessageRoleContent>,
    val stream: Boolean = false,
    val temperature: Double = 0.9,
    @SerialName("max_tokens") val maxTokens: Int = 8192
)

@Serializable
data class OpenAIMessageRoleContent(val role: String, val content: String)

// OpenAI multi-block content for vision support
@Serializable
data class OpenAIMessageContent(
    val type: String,
    val text: String? = null,
    val image_url: OpenAIImageUrl? = null
)

@Serializable
data class OpenAIMessageContentWrapper(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIImageUrl(
    val url: String,
    val detail: String = "auto"
)

@Serializable
data class OpenAIResponse(
    val choices: List<OpenAIChoice>? = null
)

@Serializable
data class OpenAIChoice(val message: OpenAIMessage? = null, val delta: OpenAIChoiceDelta? = null)

@Serializable
data class OpenAIMessage(val content: String? = null)

@Serializable
data class OpenAIChoiceDelta(val content: String? = null)

// Anthropic content blocks
@Serializable
sealed class AnthropicContentBlock {
    @Serializable
    @SerialName("text")
    data class TextBlock(val text: String) : AnthropicContentBlock()

    @Serializable
    @SerialName("image")
    data class ImageBlock(val source: AnthropicImageSource) : AnthropicContentBlock()
}

@Serializable
data class AnthropicImageSource(
    val type: String = "base64",
    @SerialName("media_type") val mediaType: String,
    val data: String
)

@Serializable
data class AnthropicRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    val system: String? = null,
    val max_tokens: Int = 2048,
    val stream: Boolean = false,
    val temperature: Double? = null
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: String
)

@Serializable
data class AnthropicResponse(
    val content: List<AnthropicContent>? = null
)

@Serializable
data class AnthropicContent(
    val type: String,
    val text: String? = null
)

@Serializable
data class AnthropicContentBlockDelta(
    val type: String,
    val index: Int,
    val delta: AnthropicTextDelta
)

@Serializable
data class AnthropicTextDelta(
    val type: String,
    val text: String
)

/**
 * AI Repository supporting 4 API providers via Ktor HTTP client:
 * - Google AI Studio (Gemini/Gemma)
 * - OpenAI Compatible (LM Studio, LocalAI, etc.)
 * - Ollama
 * - Anthropic (Claude)
 *
 * Uses in-memory config by default; can be wired to SettingsRepository for persistent settings.
 */
class AiRepository(
    private val fallbackApiKey: String = "",
    private val settingsRepository: SettingsRepository? = null
) {
    private var httpClient: HttpClient = buildClient()
    private var currentConfig: ApiConfig? = null
    private var currentModelId: String = "gemma-3-27b-it"

    private fun buildClient(): HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(120, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 30_000
        }
    }

    /**
     * Sets the active model (for Gemma model selection UI).
     */
    fun setModel(model: AiModel) {
        currentModelId = model.id
        cachedModel = null
    }

    @Volatile
    private var cachedModel: com.google.ai.client.generativeai.GenerativeModel? = null

    private val generativeModel: com.google.ai.client.generativeai.GenerativeModel
        get() {
            val cached = cachedModel
            if (cached != null) return cached
            val key = currentConfig?.apiKey ?: fallbackApiKey
            val newModel = com.google.ai.client.generativeai.GenerativeModel(
                modelName = currentModelId,
                apiKey = key,
                generationConfig = com.google.ai.client.generativeai.type.generationConfig {
                    temperature = 0.7f
                    topK = 64
                    topP = 0.95f
                }
            )
            cachedModel = newModel
            return newModel
        }

    fun getCurrentModelId(): String = currentModelId

    private suspend fun resolveConfig(): ApiConfig {
        return settingsRepository?.getConfig() ?: ApiConfig(
            baseUrl   = "https://generativelanguage.googleapis.com",
            apiKey    = fallbackApiKey,
            modelName = currentModelId,
            provider  = ApiProvider.GOOGLE_AI_STUDIO
        )
    }

    private fun ensureClientForConfig(config: ApiConfig) {
        if (currentConfig?.baseUrl != config.baseUrl) {
            httpClient.close()
            httpClient = buildClient()
            currentConfig = config
        }
    }

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

        Respond in markdown format.
    """.trimIndent()

    /**
     * Sends a query to the AI with document context. Returns streaming response as Flow.
     */
    fun queryAi(
        selectedText: String,
        context: String,
        promptTemplate: String = "Explain this clearly"
    ): Flow<String> = flow {
        val fullPrompt = buildPrompt(selectedText, context, promptTemplate)
        try {
            val response = generativeModel.generateContentStream(fullPrompt)
            var accumulatedResponse = ""
            response.collect { chunk ->
                accumulatedResponse += chunk.text
                emit(accumulatedResponse)
            }
        } catch (e: Exception) {
            try {
                val response = generativeModel.generateContent(fullPrompt)
                emit(response.text ?: "Error: Empty response from AI")
            } catch (e2: Exception) {
                emit("Error: AI request failed - ${e2.localizedMessage}")
            }
        }
    }

    /**
     * Sends a direct prompt for full-document chat. Uses Ktor HTTP client for flexibility.
     */
    fun queryAiDirect(prompt: String): Flow<String> = flow {
        val config = resolveConfig()
        ensureClientForConfig(config)

        when (config.provider) {
            ApiProvider.GOOGLE_AI_STUDIO -> {
                emitAll(googleStream(prompt, config))
            }
            ApiProvider.OPENAI_COMPATIBLE -> {
                emitAll(openAIStream(prompt, config))
            }
            ApiProvider.OLLAMA -> {
                emitAll(ollamaStream(prompt, config))
            }
            ApiProvider.ANTHROPIC -> {
                emitAll(anthropicStream(prompt, config))
            }
        }
    }

    /**
     * Sends a prompt with optional image attachments for vision-capable models.
     * Falls back to text-only if the model doesn't support vision.
     * If no images are provided, delegates to the standard queryAiDirect.
     */
    fun queryAiDirect(
        prompt: String,
        imageUris: List<android.net.Uri>,
        context: android.content.Context
    ): Flow<String> = flow {
        val config = resolveConfig()
        ensureClientForConfig(config)

        // If no images, delegate to text-only
        if (imageUris.isEmpty()) {
            queryAiDirect(prompt).collect { emit(it) }
            return@flow
        }

        when (config.provider) {
            ApiProvider.GOOGLE_AI_STUDIO -> {
                emitAll(googleStreamWithImages(prompt, imageUris, config, context))
            }
            ApiProvider.OPENAI_COMPATIBLE -> {
                emitAll(openAIStreamWithImages(prompt, imageUris, config, context))
            }
            ApiProvider.OLLAMA -> {
                // Ollama with vision models typically uses the /api/generate endpoint
                // with a different format — delegate to image-capable method
                emitAll(ollamaStreamWithImages(prompt, imageUris, config, context))
            }
            ApiProvider.ANTHROPIC -> {
                emitAll(anthropicStreamWithImages(prompt, imageUris, config, context))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // VISION SUPPORT: Google AI Studio
    // ═══════════════════════════════════════════════════════════

    private fun googleStreamWithImages(
        prompt: String,
        imageUris: List<android.net.Uri>,
        config: ApiConfig,
        context: android.content.Context
    ): Flow<String> = flow {
        try {
            val parts = mutableListOf<GooglePart>()

            // Text part first
            parts.add(GooglePart(text = prompt))

            // Attach images as inline data
            for (uri in imageUris) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val mime = context.contentResolver.getType(uri) ?: "image/png"
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    parts.add(GooglePart(inlineData = GoogleInlineData(mimeType = mime, data = base64)))
                }
            }

            val requestBody = GoogleChatRequest(
                contents = listOf(GoogleContent(parts))
            )
            val host = config.baseUrl.removePrefix("https://").removePrefix("http://")
            val response = httpClient.post {
                url {
                    protocol = URLProtocol.HTTPS
                    this.host = host
                    path("/v1beta/models/${config.modelName}:generateContent")
                    parameter("key", config.apiKey)
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body: String = response.bodyAsText()
            val parsed = Json.decodeFromString<GoogleChatResponse>(body)
            val text = parsed.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull { it.text.isNotEmpty() }
                ?.text ?: ""
            emit(text)
        } catch (e: Exception) {
            emit("Error: Vision request failed - ${e.localizedMessage}")
        }
    }

    // ═══════════════════════════════════════════════════════════
    // VISION SUPPORT: OpenAI Compatible
    // ═══════════════════════════════════════════════════════════

    private fun openAIStreamWithImages(
        prompt: String,
        imageUris: List<android.net.Uri>,
        config: ApiConfig,
        context: android.content.Context
    ): Flow<String> = flow {
        try {
            val contentBlocks = mutableListOf<Any>()
            contentBlocks.add(mapOf("type" to "text", "text" to prompt))

            for (uri in imageUris) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    val mime = context.contentResolver.getType(uri) ?: "image/png"
                    contentBlocks.add(
                        mapOf(
                            "type" to "image_url",
                            "image_url" to mapOf(
                                "url" to "data:$mime;base64,$base64",
                                "detail" to "auto"
                            )
                        )
                    )
                }
            }

            val requestBody = mapOf(
                "model" to config.modelName,
                "messages" to listOf(
                    mapOf("role" to "system", "content" to systemPrompt),
                    mapOf("role" to "user", "content" to contentBlocks)
                ),
                "stream" to true
            )

            val host = config.baseUrl.removePrefix("https://").removePrefix("http://")
            val response: HttpResponse = httpClient.post {
                url {
                    protocol = if (host.startsWith("localhost") || host.contains("127.0.0.1"))
                        URLProtocol.HTTP else URLProtocol.HTTPS
                    this.host = host
                    path("/v1/chat/completions")
                    header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body: String = response.bodyAsText()
            parseOpenAISSE(body).collect { emit(it) }
        } catch (e: Exception) {
            emit("Error: OpenAI vision request failed - ${e.localizedMessage}")
        }
    }

    // ═══════════════════════════════════════════════════════════
    // VISION SUPPORT: Ollama
    // ═══════════════════════════════════════════════════════════

    private fun ollamaStreamWithImages(
        prompt: String,
        imageUris: List<android.net.Uri>,
        config: ApiConfig,
        context: android.content.Context
    ): Flow<String> = flow {
        try {
            val base64Images = mutableListOf<String>()
            for (uri in imageUris) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    base64Images.add(base64)
                }
            }

            val requestBody = mapOf(
                "model" to config.modelName,
                "prompt" to prompt,
                "images" to base64Images,
                "stream" to true
            )

            val host = config.baseUrl.removePrefix("http://").removePrefix("https://")
            val response: HttpResponse = httpClient.post {
                url {
                    protocol = URLProtocol.HTTP
                    this.host = host
                    path("/api/generate")
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body: String = response.bodyAsText()
            parseOllamaSSE(body).collect { emit(it) }
        } catch (e: Exception) {
            emit("Error: Ollama vision request failed - ${e.localizedMessage}")
        }
    }

    // ═══════════════════════════════════════════════════════════
    // VISION SUPPORT: Anthropic
    // ═══════════════════════════════════════════════════════════

    private fun anthropicStreamWithImages(
        prompt: String,
        imageUris: List<android.net.Uri>,
        config: ApiConfig,
        context: android.content.Context
    ): Flow<String> = flow {
        try {
            val contentBlocks = mutableListOf<Any>()
            contentBlocks.add(mapOf("type" to "text", "text" to prompt))

            for (uri in imageUris) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    val mime = context.contentResolver.getType(uri) ?: "image/png"
                    contentBlocks.add(
                        mapOf(
                            "type" to "image",
                            "source" to mapOf(
                                "type" to "base64",
                                "media_type" to mime,
                                "data" to base64
                            )
                        )
                    )
                }
            }

            val requestBody = mapOf(
                "model" to config.modelName,
                "messages" to listOf(
                    mapOf("role" to "user", "content" to contentBlocks)
                ),
                "system" to systemPrompt,
                "max_tokens" to 2048,
                "stream" to true
            )

            val host = config.baseUrl.removePrefix("https://").removePrefix("http://")
            val response: HttpResponse = httpClient.post {
                url {
                    protocol = URLProtocol.HTTPS
                    this.host = host
                    path("/v1/messages")
                    header("x-api-key", config.apiKey)
                    header("anthropic-version", "2023-06-01")
                    header("Accept", "text/event-stream")
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body: String = response.bodyAsText()
            parseAnthropicSSE(body).collect { emit(it) }
        } catch (e: Exception) {
            emit("Error: Anthropic vision request failed - ${e.localizedMessage}")
        }
    }

    // ── Google AI Studio ─────────────────────────────────────

    private fun googleStream(prompt: String, config: ApiConfig): Flow<String> = flow {
        try {
            val requestBody = GoogleChatRequest(
                contents = listOf(GoogleContent(listOf(GooglePart(prompt))))
            )
            val host = config.baseUrl.removePrefix("https://").removePrefix("http://")
            val response = httpClient.post {
                url {
                    protocol = URLProtocol.HTTPS
                    this.host = host
                    path("/v1beta/models/${config.modelName}:generateContent")
                    parameter("key", config.apiKey)
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body: String = response.bodyAsText()
            val parsed = Json.decodeFromString<GoogleChatResponse>(body)
            val text = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            emit(text)
        } catch (e: Exception) {
            // Fall back to Gemini SDK
            var accumulated = ""
            try {
                val response = generativeModel.generateContentStream(prompt)
                response.collect { chunk ->
                    accumulated += chunk.text
                    emit(accumulated)
                }
            } catch (e2: Exception) {
                emit("Error: AI request failed - ${e2.localizedMessage}")
            }
        }
    }

    // ── OpenAI Compatible ────────────────────────────────────

    private fun openAIStream(prompt: String, config: ApiConfig): Flow<String> = flow {
        try {
            val requestBody = OpenAIRequest(
                model = config.modelName,
                messages = listOf(
                    OpenAIMessageRoleContent("system", systemPrompt),
                    OpenAIMessageRoleContent("user", prompt)
                ),
                stream = true
            )
            val host = config.baseUrl.removePrefix("https://").removePrefix("http://")
            val response: HttpResponse = httpClient.post {
                url {
                    protocol = if (host.startsWith("localhost") || host.contains("127.0.0.1"))
                        URLProtocol.HTTP else URLProtocol.HTTPS
                    this.host = host
                    path("/v1/chat/completions")
                    header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body: String = response.bodyAsText()
            parseOpenAISSE(body).collect { emit(it) }
        } catch (e: Exception) {
            emit("Error: OpenAI-compatible request failed - ${e.localizedMessage}")
        }
    }

    private fun parseOpenAISSE(body: String): Flow<String> = flow {
        val lines = body.split("\n")
        for (line in lines) {
            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break
                try {
                    val parsed = Json.decodeFromString<OpenAIResponse>(data)
                    val content = parsed.choices?.firstOrNull()?.message?.content
                    if (!content.isNullOrBlank()) emit(content)
                } catch (_: Exception) {
                    // Skip malformed lines
                }
            }
        }
    }

    // ── Ollama ──────────────────────────────────────────────

    @Serializable
    data class OllamaGenerateReq(val model: String, val prompt: String, val stream: Boolean = false, val options: OllamaOpts = OllamaOpts())
    @Serializable data class OllamaOpts(val temperature: Double = 0.9)
    @Serializable data class OllamaResponse(val response: String = "")

    private fun ollamaStream(prompt: String, config: ApiConfig): Flow<String> = flow {
        try {
            val requestBody = OllamaGenerateReq(model = config.modelName, prompt = prompt, stream = true)
            val host = config.baseUrl.removePrefix("http://").removePrefix("https://")
            val response: HttpResponse = httpClient.post {
                url {
                    protocol = URLProtocol.HTTP
                    this.host = host
                    path("/api/generate")
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body: String = response.bodyAsText()
            parseOllamaSSE(body).collect { emit(it) }
        } catch (e: Exception) {
            emit("Error: Ollama request failed - ${e.localizedMessage}")
        }
    }

    private fun parseOllamaSSE(body: String): Flow<String> = flow {
        val rawEvents = body.split("\n\n").filter { it.isNotBlank() }
        for (rawEvent in rawEvents) {
            val lines = rawEvent.split("\n")
            for (line in lines) {
                if (line.startsWith("data: ")) {
                    val json = line.removePrefix("data: ").trim()
                    try {
                        val resp = Json.decodeFromString<OllamaResponse>(json)
                        if (resp.response.isNotBlank()) emit(resp.response)
                    } catch (_: Exception) { /* skip */ }
                }
            }
        }
    }

    // ── Anthropic ────────────────────────────────────────────

    private fun anthropicStream(prompt: String, config: ApiConfig): Flow<String> = flow {
        try {
            val requestBody = AnthropicRequest(
                model = config.modelName,
                messages = listOf(AnthropicMessage("user", prompt)),
                system = systemPrompt,
                max_tokens = 2048,
                stream = true
            )
            val host = config.baseUrl.removePrefix("https://").removePrefix("http://")
            val response: HttpResponse = httpClient.post {
                url {
                    protocol = URLProtocol.HTTPS
                    this.host = host
                    path("/v1/messages")
                    header("x-api-key", config.apiKey)
                    header("anthropic-version", "2023-06-01")
                    header("Accept", "text/event-stream")
                }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val body: String = response.bodyAsText()
            parseAnthropicSSE(body).collect { emit(it) }
        } catch (e: Exception) {
            emit("Error: Anthropic request failed - ${e.localizedMessage}")
        }
    }

    private fun parseAnthropicSSE(body: String): Flow<String> = flow {
        val rawEvents = body.split("\n\n").filter { it.isNotBlank() }
        var currentEventType = ""
        var currentData = StringBuilder()

        for (rawEvent in rawEvents) {
            val lines = rawEvent.split("\n")
            for (line in lines) {
                when {
                    line.startsWith("event:") -> currentEventType = line.removePrefix("event:").trim()
                    line.startsWith("data:") -> {
                        if (currentData.isNotEmpty() && currentEventType == "content_block_delta") {
                            try {
                                val parsed = Json.decodeFromString<AnthropicContentBlockDelta>(currentData.toString())
                                emit(parsed.delta.text)
                            } catch (_: Exception) { /* skip */ }
                        }
                        currentData = StringBuilder(line.removePrefix("data:").trim())
                    }
                    line.isEmpty() -> {
                        // End of event
                        if (currentEventType == "content_block_delta" && currentData.isNotEmpty()) {
                            try {
                                val parsed = Json.decodeFromString<AnthropicContentBlockDelta>(currentData.toString())
                                emit(parsed.delta.text)
                            } catch (_: Exception) { /* skip */ }
                        }
                        currentData = StringBuilder()
                    }
                    else -> currentData.append("\n").append(line)
                }
            }
        }
    }

    /**
     * Builds the full prompt with context and system instructions.
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

            Response structure:
            - For explanation requests, include a direct answer, context and background, and key takeaways.
            - For testing requests, include questions and answers.

            Please provide a helpful, study-focused response to the user's request about the selected text, considering the surrounding context.
        """.trimIndent()
    }

    fun buildPromptForTest(
        selectedText: String,
        surroundingContext: String,
        promptTemplate: String
    ): String = buildPrompt(selectedText, surroundingContext, promptTemplate)

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
        return try {
            val response = generativeModel.generateContent(buildFlashcardsPrompt(content))
            response.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Generates a self-check quiz from document content.
     */
    suspend fun generateQuiz(content: String): String {
        return try {
            val response = generativeModel.generateContent(buildQuizPrompt(content))
            response.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
