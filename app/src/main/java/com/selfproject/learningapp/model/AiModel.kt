package com.selfproject.learningapp.model

/**
 * Available AI models. Each model includes display name, API model ID, and metadata.
 * `supportsVision` indicates if the model can process images directly.
 */
data class AiModel(
    val id: String,
    val displayName: String,
    val family: ModelFamily,
    val parameterSize: String,
    val supportsVision: Boolean = false,
    val isDefault: Boolean = false,
)

enum class ModelFamily {
    GEMMA_3,
    GEMMA_4,
    CLAUDE,
    OPENAI,
    OLLAMA,
}

object ModelCatalog {

    val models = listOf(
        // ── Gemma 3 ────────────────────────────────────────────
        AiModel(
            id = "gemma-3-4b-it",
            displayName = "Gemma 3 4B",
            family = ModelFamily.GEMMA_3,
            parameterSize = "4B",
        ),
        AiModel(
            id = "gemma-3-12b-it",
            displayName = "Gemma 3 12B",
            family = ModelFamily.GEMMA_3,
            parameterSize = "12B",
        ),
        AiModel(
            id = "gemma-3-27b-it",
            displayName = "Gemma 3 27B",
            family = ModelFamily.GEMMA_3,
            parameterSize = "27B",
            isDefault = true,
        ),

        // ── Gemma 4 ────────────────────────────────────────────
        AiModel(
            id = "gemma-4-26b-it",
            displayName = "Gemma 4 26B (MoE)",
            family = ModelFamily.GEMMA_4,
            parameterSize = "26B",
        ),
        AiModel(
            id = "gemma-4-31b-it",
            displayName = "Gemma 4 31B",
            family = ModelFamily.GEMMA_4,
            parameterSize = "31B",
        ),

        // ── Claude (Anthropic) — all support vision ──────────────
        AiModel(
            id = "claude-opus-4-5",
            displayName = "Claude Opus 4",
            family = ModelFamily.CLAUDE,
            parameterSize = "200K ctx",
            supportsVision = true,
        ),
        AiModel(
            id = "claude-sonnet-4-20250514",
            displayName = "Claude Sonnet 4",
            family = ModelFamily.CLAUDE,
            parameterSize = "200K ctx",
            supportsVision = true,
        ),
        AiModel(
            id = "claude-haiku-4-20250514",
            displayName = "Claude Haiku 4",
            family = ModelFamily.CLAUDE,
            parameterSize = "200K ctx",
            supportsVision = true,
        ),
        AiModel(
            id = "claude-3-5-haiku-latest",
            displayName = "Claude 3.5 Haiku",
            family = ModelFamily.CLAUDE,
            parameterSize = "200K ctx",
            supportsVision = true,
        ),

        // ── OpenAI / OpenAI-compatible — some support vision ─────
        AiModel(
            id = "gpt-4o",
            displayName = "GPT-4o",
            family = ModelFamily.OPENAI,
            parameterSize = "128K ctx",
            supportsVision = true,
        ),
        AiModel(
            id = "gpt-4o-mini",
            displayName = "GPT-4o Mini",
            family = ModelFamily.OPENAI,
            parameterSize = "128K ctx",
            supportsVision = true,
        ),
        AiModel(
            id = "gpt-4-turbo",
            displayName = "GPT-4 Turbo",
            family = ModelFamily.OPENAI,
            parameterSize = "128K ctx",
            supportsVision = true,
        ),
        AiModel(
            id = "gpt-3.5-turbo",
            displayName = "GPT-3.5 Turbo",
            family = ModelFamily.OPENAI,
            parameterSize = "16K ctx",
            supportsVision = false,
        ),

        // ── Ollama — depends on the model, default to no vision ─
        AiModel(
            id = "llama3",
            displayName = "Llama 3",
            family = ModelFamily.OLLAMA,
            parameterSize = "8B",
            supportsVision = false,
        ),
        AiModel(
            id = "llama3.1",
            displayName = "Llama 3.1",
            family = ModelFamily.OLLAMA,
            parameterSize = "8B",
            supportsVision = false,
        ),
        AiModel(
            id = "llava",
            displayName = "LLaVA",
            family = ModelFamily.OLLAMA,
            parameterSize = "7B",
            supportsVision = true,
        ),
        AiModel(
            id = "qwen2-vl",
            displayName = "Qwen2 VL",
            family = ModelFamily.OLLAMA,
            parameterSize = "7B",
            supportsVision = true,
        ),
    )

    val defaultModel: AiModel = models.find { it.isDefault } ?: models.first()

    val groupedByFamily: Map<ModelFamily, List<AiModel>> = models.groupBy { it.family }

    fun getById(id: String): AiModel = models.find { it.id == id } ?: defaultModel
}