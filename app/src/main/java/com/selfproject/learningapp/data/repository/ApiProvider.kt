package com.selfproject.learningapp.data.repository

/**
 * Supported AI API providers.
 */
enum class ApiProvider(
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String
) {
    GOOGLE_AI_STUDIO(
        displayName = "Google AI Studio",
        defaultBaseUrl = "https://generativelanguage.googleapis.com",
        defaultModel = "gemini-2.0-flash"
    ),
    OPENAI_COMPATIBLE(
        displayName = "OpenAI Compatible",
        defaultBaseUrl = "https://api.openai.com",
        defaultModel = "gpt-4o"
    ),
    OLLAMA(
        displayName = "Ollama",
        defaultBaseUrl = "http://localhost:11434",
        defaultModel = "llama3"
    ),
    ANTHROPIC(
        displayName = "Anthropic",
        defaultBaseUrl = "https://api.anthropic.com",
        defaultModel = "claude-sonnet-4-20250514"
    );

    companion object {
        fun detectFromUrl(url: String): ApiProvider = when {
            url.contains("api.anthropic.com")                        -> ANTHROPIC
            url.contains("generativelanguage.googleapis.com")         -> GOOGLE_AI_STUDIO
            url.contains("ollama")                                   -> OLLAMA
            else                                                     -> OPENAI_COMPATIBLE
        }
    }
}