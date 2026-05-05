package com.selfproject.learningapp.model

/**
 * Configuration for the Questioner feature.
 */
data class QuestionerConfig(
    val questionCount: Int = 25,
    val mode: QuestionerMode = QuestionerMode.CRITIC,
)

enum class QuestionerMode(
    val label: String,
    val icon: String,
    val description: String,
    val systemPrompt: String,
) {
    CRITIC(
        label = "Critic",
        icon = "🔍",
        description = "Focuses on finding flaws, inaccuracies, and gaps in your responses. Points out exactly what you got wrong and why.",
        systemPrompt = """
            You are a rigorous CRITIC. Your role is to scrutinize the user's answers against the document content.
            For each answer:
            1. Identify SPECIFIC errors, omissions, or inaccuracies
            2. Quote the relevant document passage that contradicts their answer
            3. Explain exactly WHY their answer is wrong or incomplete
            4. Provide the corrected version
            5. Be direct and honest — don't soften criticism
            Base ALL critiques strictly on the document content.
        """.trimIndent()
    ),
    SOCRATIC(
        label = "Socratic",
        icon = "🤔",
        description = "Uses probing follow-up questions to expose gaps in your logic and deepen your understanding.",
        systemPrompt = """
            You are a SOCRATIC QUESTIONER. Your role is to probe the user's thinking with follow-up questions that expose gaps in their logic.
            For each answer:
            1. Acknowledge what they got right
            2. Identify a gap, assumption, or unstated implication in their answer
            3. Ask a probing follow-up question that forces them to think deeper
            4. Use phrases like "But what about...?", "How does that connect to...?", "If that's true, then why...?"
            5. Guide them to discover the missing piece themselves
            All questions must relate directly to the document content. Your goal is not to give answers but to expose what they haven't considered.
        """.trimIndent()
    ),
}

/**
 * State for the active Questioner session.
 */
data class QuestionerState(
    val isActive: Boolean = false,
    val config: QuestionerConfig = QuestionerConfig(),
    val currentQuestion: Int = 0,
    val totalQuestions: Int = 0,
    val awaitingAnswer: Boolean = false,
    val score: Int = 0,
    val completed: Boolean = false,
)
