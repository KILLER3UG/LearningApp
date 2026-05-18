package com.selfproject.learningapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.local.FlashcardEntity
import com.selfproject.learningapp.data.local.QuizEntity
import com.selfproject.learningapp.model.*
import com.selfproject.learningapp.model.AiConversation
import com.selfproject.learningapp.model.AiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullDocumentChat(
    documentContent: String,
    documentTitle: String,
    queryState: AiQueryState,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onNotebookAction: (String, String) -> Unit = { _, _ -> },
    onStopStreaming: () -> Unit = {},
    onSubmitAnswer: (String) -> Unit = {},
    questionerState: com.selfproject.learningapp.model.QuestionerState = com.selfproject.learningapp.model.QuestionerState(),
    onDismiss: () -> Unit,
    conversations: List<AiConversation> = emptyList(),
    currentConversationId: String? = null,
    onCreateConversation: () -> Unit = {},
    onSwitchConversation: (String) -> Unit = {},
    onDeleteConversation: (String) -> Unit = {},
    onModelSelected: (AiModel) -> Unit = {},
    availableModels: List<AiModel> = emptyList(),
    selectedModel: AiModel? = null,
    onGenerateFullDocFlashcards: () -> Unit = {},
    onGenerateFullDocQuiz: () -> Unit = {},
    onStartGuidedLearning: () -> Unit = {},
    onStartQuestioner: (QuestionerConfig) -> Unit = {},
    onNavigateFlashcards: () -> Unit = {},
    onNavigateQuizzes: () -> Unit = {},
    onNavigateMatch: () -> Unit = {},
    onReviewFlashcard: (FlashcardEntity, Boolean) -> Unit = { _, _ -> },
    onDeleteFlashcard: (FlashcardEntity) -> Unit = {},
    onDeleteQuiz: (QuizEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isStreaming = queryState is AiQueryState.Loading
    var showOptionsMenu by remember { mutableStateOf(false) }
    var viewingFlashcardMessageId by remember { mutableStateOf<String?>(null) }
    var viewingQuizMessageId by remember { mutableStateOf<String?>(null) }
    var showQuestionerConfig by remember { mutableStateOf(false) }
    var showConvMenu by remember { mutableStateOf(false) }
    var showModelMenu by remember { mutableStateOf(false) }
    val notebookActions = remember(documentTitle) { notebookActions(documentTitle) }

    val viewingFlashcards = viewingFlashcardMessageId?.let { id -> messages.find { it.id == id }?.flashcards }
    val viewingQuizzes = viewingQuizMessageId?.let { id -> messages.find { it.id == id }?.quizzes }

    LaunchedEffect(messages.size) {
        val lastIndex = listState.layoutInfo.totalItemsCount - 1
        if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
    }

    if (showQuestionerConfig) {
        QuestionerConfigPanel(
            onStart = { config -> showQuestionerConfig = false; onStartQuestioner(config) },
            onCancel = { showQuestionerConfig = false }
        )
        return
    }

    viewingFlashcards?.let { flashcards ->
        InlineFlashcardViewer(flashcards = flashcards, onBackToChat = { viewingFlashcardMessageId = null },
            onReview = onReviewFlashcard, onDelete = { onDeleteFlashcard(it); viewingFlashcardMessageId = null })
        return
    }

    viewingQuizzes?.let { quizzes ->
        InlineQuizViewer(quizzes = quizzes, onBackToChat = { viewingQuizMessageId = null },
            onDelete = { onDeleteQuiz(it); viewingQuizMessageId = null })
        return
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(
            tonalElevation = 0.dp,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            conversations.find { it.id == currentConversationId }?.title ?: "New chat",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${selectedModel?.displayName ?: "Model"} · $documentTitle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (availableModels.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showModelMenu = true }, modifier = Modifier.size(44.dp)) {
                                Icon(
                                    Icons.Outlined.SmartToy,
                                    contentDescription = "Select model",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showModelMenu,
                                onDismissRequest = { showModelMenu = false },
                                modifier = Modifier.width(220.dp)
                            ) {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                model.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        onClick = {
                                            onModelSelected(model)
                                            showModelMenu = false
                                        },
                                        trailingIcon = {
                                            if (model.id == selectedModel?.id) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { showConvMenu = true }, modifier = Modifier.size(44.dp)) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Conversation options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showConvMenu,
                            onDismissRequest = { showConvMenu = false },
                            modifier = Modifier.width(240.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("New chat") },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                onClick = {
                                    onCreateConversation()
                                    showConvMenu = false
                                }
                            )
                            if (conversations.isNotEmpty()) {
                                HorizontalDivider()
                                conversations.forEach { conv ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                conv.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        onClick = {
                                            onSwitchConversation(conv.id)
                                            showConvMenu = false
                                        },
                                        trailingIcon = {
                                            if (conversations.size > 1) {
                                                IconButton(
                                                    onClick = {
                                                        onDeleteConversation(conv.id)
                                                        showConvMenu = false
                                                    },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        AiEmptyIllustration()
                        Spacer(Modifier.height(20.dp))
                        Text("Ready for this document", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text("Ask anything, generate study materials, or turn the source into a structured guide.", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        NotebookPromptGrid(
                            actions = notebookActions,
                            enabled = !isStreaming,
                            onAction = { action -> onNotebookAction(action.label, action.prompt) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        if (message.role == Role.USER) {
                            // User bubble — right aligned
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Card(
                                    modifier = Modifier.widthIn(max = 280.dp),
                                    shape = RoundedCornerShape(20.dp, 6.dp, 20.dp, 20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Text(
                                        text = message.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        } else {
                            // AI response — rendered directly, no bubble
                            if (message.content.isNotBlank()) {
                                ModernAiMessage(content = message.content)
                            }
                        }

                        // Generated content cards
                        when (message.type) {
                            MessageType.FLASHCARD_GENERATED -> GeneratedFlashcardCard(count = message.flashcards.size, onClick = { viewingFlashcardMessageId = message.id })
                            MessageType.QUIZ_GENERATED -> GeneratedQuizCard(count = message.quizzes.size, onClick = { viewingQuizMessageId = message.id })
                            else -> {}
                        }
                    }

                    // Typing indicator
                    if (isStreaming) {
                        item { TypingIndicator() }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        Surface(
            tonalElevation = 0.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                AnimatedVisibility(visible = showOptionsMenu) {
                    QuestionOptionsMenu(
                        notebookActions = notebookActions,
                        onNotebookAction = { action ->
                            showOptionsMenu = false
                            onNotebookAction(action.label, action.prompt)
                        },
                        onGenerateFlashcards = { showOptionsMenu = false; onGenerateFullDocFlashcards() },
                        onGenerateQuiz = { showOptionsMenu = false; onGenerateFullDocQuiz() },
                        onStartGuidedLearning = { showOptionsMenu = false; onStartGuidedLearning() },
                        onShowQuestionerConfig = { showOptionsMenu = false; showQuestionerConfig = true },
                        onNavigateFlashcards = { showOptionsMenu = false; onNavigateFlashcards() },
                        onNavigateQuizzes = { showOptionsMenu = false; onNavigateQuizzes() },
                        onNavigateMatch = { showOptionsMenu = false; onNavigateMatch() }
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .padding(start = 4.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showOptionsMenu = !showOptionsMenu }, modifier = Modifier.size(44.dp)) {
                            Icon(
                                if (showOptionsMenu) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Tools",
                                tint = if (showOptionsMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(if (questionerState.awaitingAnswer) "Type your answer" else "Message")
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (inputText.isNotBlank()) {
                                    if (questionerState.awaitingAnswer) onSubmitAnswer(inputText.trim())
                                    else onSendMessage(inputText.trim())
                                    inputText = ""
                                }
                            }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )

                        Spacer(Modifier.width(4.dp))

                        FloatingActionButton(
                            onClick = {
                                if (isStreaming) onStopStreaming()
                                else if (inputText.isNotBlank()) {
                                    if (questionerState.awaitingAnswer) onSubmitAnswer(inputText.trim())
                                    else onSendMessage(inputText.trim())
                                    inputText = ""
                                }
                            },
                            containerColor = if (isStreaming) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            contentColor = if (isStreaming) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape
                        ) {
                            AnimatedContent(targetState = isStreaming, label = "sendStop") { streaming ->
                                if (streaming) Icon(Icons.Outlined.Stop, contentDescription = "Stop", modifier = Modifier.size(20.dp))
                                else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class NotebookAction(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val prompt: String
)

private fun notebookActions(documentTitle: String): List<NotebookAction> = listOf(
    NotebookAction(
        label = "Briefing Doc",
        description = "Source summary",
        icon = Icons.Default.Description,
        prompt = """
            Create a NotebookLM-style briefing document for "$documentTitle".
            Include:
            - Source overview
            - Main ideas and claims
            - Key facts, names, dates, formulas, or terms
            - Important short quotes or exact phrases from the source
            - Open questions or unclear areas
            - Five high-signal takeaways
        """.trimIndent()
    ),
    NotebookAction(
        label = "Study Guide",
        description = "Learn this source",
        icon = Icons.Default.School,
        prompt = """
            Turn "$documentTitle" into a study guide.
            Include:
            - Learning objectives
            - Section-by-section explanation
            - Key terms with simple definitions
            - Common misconceptions
            - Practice questions with answers
            Keep everything grounded in the source.
        """.trimIndent()
    ),
    NotebookAction(
        label = "FAQ",
        description = "Likely questions",
        icon = Icons.AutoMirrored.Filled.Help,
        prompt = """
            Create a source-grounded FAQ for "$documentTitle".
            Write the questions a student is most likely to ask, then answer them clearly.
            If the source does not contain enough evidence for an answer, say that directly.
        """.trimIndent()
    ),
    NotebookAction(
        label = "Source Map",
        description = "Structure and links",
        icon = Icons.Default.Search,
        prompt = """
            Map the structure of "$documentTitle".
            Identify the major topics, how they connect, dependencies between ideas, and the best reading order.
            If the source includes events or dates, add a timeline. Otherwise, add a concept map in markdown.
        """.trimIndent()
    ),
    NotebookAction(
        label = "Timeline",
        description = "Events and sequence",
        icon = Icons.Default.Timeline,
        prompt = """
            Build a source-grounded timeline for "$documentTitle".
            Include dates, phases, process steps, cause-and-effect links, and any missing chronology the source leaves unclear.
            If the source is not historical, convert the material into a learning sequence with prerequisite ideas first.
        """.trimIndent()
    ),
    NotebookAction(
        label = "Audio Brief",
        description = "Podcast-style script",
        icon = Icons.Default.GraphicEq,
        prompt = """
            Create a NotebookLM-style audio overview script for "$documentTitle".
            Format it as a concise two-host discussion with host names, natural transitions, and source-grounded explanations.
            Include a short version for a quick listen and a deeper version for review.
        """.trimIndent()
    ),
    NotebookAction(
        label = "Critique",
        description = "Strengths and gaps",
        icon = Icons.Default.RateReview,
        prompt = """
            Critique "$documentTitle" constructively.
            Identify the strongest ideas, weak or unsupported claims, missing evidence, confusing structure, and ways to improve the material.
            Keep every critique tied to details from the source.
        """.trimIndent()
    ),
    NotebookAction(
        label = "Debate",
        description = "Two perspectives",
        icon = Icons.Default.Groups,
        prompt = """
            Turn "$documentTitle" into a formal source-grounded debate.
            Present two opposing perspectives, the best evidence each side can use from the source, rebuttals, and a balanced conclusion.
        """.trimIndent()
    ),
    NotebookAction(
        label = "Quote Finder",
        description = "Evidence bank",
        icon = Icons.Default.FormatQuote,
        prompt = """
            Create an evidence bank for "$documentTitle".
            Pull the most important short source phrases, explain why each matters, and group them by topic.
            Do not invent quotes; if exact wording is unavailable, label it as a paraphrase.
        """.trimIndent()
    )
)

@Composable
private fun NotebookQuickActions(
    actions: List<NotebookAction>,
    enabled: Boolean,
    onAction: (NotebookAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(actions, key = { it.label }) { action ->
            AssistChip(
                onClick = { onAction(action) },
                enabled = enabled,
                leadingIcon = {
                    Icon(action.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                label = {
                    Column {
                        Text(action.label, style = MaterialTheme.typography.labelMedium)
                        Text(
                            action.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun NotebookPromptGrid(
    actions: List<NotebookAction>,
    enabled: Boolean,
    onAction: (NotebookAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { action ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 86.dp)
                            .clickable(enabled = enabled) { onAction(action) },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                action.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                action.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                action.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Minimalist AI illustration for the empty chat state.
 * Geometric teal shapes — no emoji, no external assets.
 */
@Composable
private fun AiEmptyIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier.size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Surface(
            modifier = Modifier.size(88.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = primaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(2.dp, primary.copy(alpha = 0.3f))
        ) {}
        // Inner circle
        Surface(
            modifier = Modifier.size(64.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = primary.copy(alpha = 0.15f)
        ) {}
        // AI icon — Sparkles
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = primary,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Modern AI message renderer with collapsible thinking blocks.
 */
@Composable
private fun ModernAiMessage(content: String, modifier: Modifier = Modifier) {
    val segments = remember(content) { parseAiContent(content) }

    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEach { segment ->
            when (segment) {
                is AiContentSegment.Thinking -> {
                    CollapsibleThinkingBlock(thought = segment.content)
                }
                is AiContentSegment.Text -> {
                    MarkdownRenderer(content = segment.content)
                }
            }
        }
    }
}

/**
 * Parses AI response to separate thinking blocks from regular text.
 * Supports: <think>...</think>, <think>...</think>, [Thinking]...[/Thinking]
 */
private fun parseAiContent(content: String): List<AiContentSegment> {
    val segments = mutableListOf<AiContentSegment>()

    val patterns = listOf(
        Regex("(?s)<think>(.*?)</think>"),
        Regex("(?s)<think>(.*?)</think>"),
        Regex("(?s)\\[Thinking\\](.*?)\\[/Thinking\\]"),
        Regex("(?s)^Thinking:.*?\n\n(.*?)(?=\n\n|$)")
    )

    var remaining = content
    var processed = false

    for (pattern in patterns) {
        val match = pattern.find(remaining)
        if (match != null) {
            val before = remaining.substring(0, match.range.first).trim()
            val thinking = match.groupValues[1].trim()
            val after = remaining.substring(match.range.last + 1).trim()

            if (before.isNotEmpty()) segments.add(AiContentSegment.Text(before))
            segments.add(AiContentSegment.Thinking(thinking))
            if (after.isNotEmpty()) {
                segments.addAll(parseAiContent(after))
            }
            processed = true
            break
        }
    }

    if (!processed) {
        if (content.isNotEmpty()) segments.add(AiContentSegment.Text(content))
    }

    return segments
}

private sealed class AiContentSegment {
    data class Text(val content: String) : AiContentSegment()
    data class Thinking(val content: String) : AiContentSegment()
}

/**
 * Collapsible thinking block — minimal, no visible container.
 */
@Composable
private fun CollapsibleThinkingBlock(thought: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp)) {
        // Header — just icon + text, no background
        Row(
            modifier = Modifier.clickable { expanded = !expanded }.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Thinking",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(14.dp)
            )
        }

        // Content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(200)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut()
        ) {
            Text(
                text = thought,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }
    }
}

/**
 * Bouncing dots typing indicator — modern AI chat style.
 */
@Composable
private fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        // Avatar
        Surface(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(Modifier.width(10.dp))

        // Bouncing dots
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { i ->
                BouncingDot(delay = i * 150)
            }
        }
    }
}

@Composable
private fun BouncingDot(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(scale)
            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(50)
            )
    )
}

@Composable
private fun QuestionOptionsMenu(
    notebookActions: List<NotebookAction>,
    onNotebookAction: (NotebookAction) -> Unit,
    onGenerateFlashcards: () -> Unit,
    onGenerateQuiz: () -> Unit,
    onStartGuidedLearning: () -> Unit,
    onShowQuestionerConfig: () -> Unit,
    onNavigateFlashcards: () -> Unit,
    onNavigateQuizzes: () -> Unit,
    onNavigateMatch: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Notebook", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
        NotebookQuickActions(
            actions = notebookActions,
            enabled = true,
            onAction = onNotebookAction,
            modifier = Modifier.padding(horizontal = 0.dp)
        )
        HorizontalDivider()
        Text("Generate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Card(modifier = Modifier.weight(1f).clickable(onClick = onGenerateFlashcards), shape = MaterialTheme.shapes.medium) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardMembership, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Column { Text("Flashcards", style = MaterialTheme.typography.labelMedium); Text("From full document", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Card(modifier = Modifier.weight(1f).clickable(onClick = onGenerateQuiz), shape = MaterialTheme.shapes.medium) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Column { Text("Quiz", style = MaterialTheme.typography.labelMedium); Text("From full document", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onStartGuidedLearning), shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column { Text("Guided Learning", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer);
                    Text("AI teaches you everything", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)) }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onShowQuestionerConfig), shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column { Text("Questioner", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer);
                    Text("AI quizzes you on every topic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)) }
            }
        }
        HorizontalDivider()
        Text("Review", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Card(modifier = Modifier.weight(1f).clickable(onClick = onNavigateFlashcards), shape = MaterialTheme.shapes.medium) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardMembership, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Column { Text("Flashcards", style = MaterialTheme.typography.labelMedium); Text("Review Q/A pairs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Card(modifier = Modifier.weight(1f).clickable(onClick = onNavigateQuizzes), shape = MaterialTheme.shapes.medium) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Column { Text("Quizzes", style = MaterialTheme.typography.labelMedium); Text("Self-check questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateMatch), shape = MaterialTheme.shapes.medium) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Match", style = MaterialTheme.typography.labelMedium)
                    Text("Timed-style pairing practice from your flashcards", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun GeneratedFlashcardCard(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CardMembership, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$count Flashcard${if (count != 1) "s" else ""} Generated", style = MaterialTheme.typography.labelLarge)
                Text("Tap to review and study", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open flashcards", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun GeneratedQuizCard(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$count Quiz Question${if (count != 1) "s" else ""} Generated", style = MaterialTheme.typography.labelLarge)
                Text("Tap to test yourself", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open quiz", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}
