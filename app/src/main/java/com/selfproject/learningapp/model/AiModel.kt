package com.selfproject.learningapp.model

/**
 * Available AI models for use with the Google AI Studio API.
 * Each model includes display name, API model ID, and metadata.
 */
data class AiModel(
    val id: String,
    val displayName: String,
    val family: ModelFamily,
    val parameterSize: String,
    val isDefault: Boolean = false,
)

enum class ModelFamily {
    GEMMA_3,
    GEMMA_4,
}

object ModelCatalog {

    val models = listOf(
        // Gemma 3 models
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

        // Gemma 4 models
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
    )

    val defaultModel: AiModel = models.find { it.isDefault } ?: models.first()

    val groupedByFamily: Map<ModelFamily, List<AiModel>> = models.groupBy { it.family }

    fun getById(id: String): AiModel = models.find { it.id == id } ?: defaultModel
}
