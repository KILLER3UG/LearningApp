package com.selfproject.learningapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

    val viewingFlashcards = viewingFlashcardMessageId?.let { id -> messages.find { it.id == id }?.flashcards }
    val viewingQuizzes = viewingQuizMessageId?.let { id -> messages.find { it.id == id }?.quizzes }

    LaunchedEffect(messages.size) {
        if (listState.canScrollForward) listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
    }

    if (showQuestionerConfig) {
        QuestionerConfigPanel(
            onStart = { config -> showQuestionerConfig = false; onStartQuestioner(config) },
            onCancel = { showQuestionerConfig = false }
        )
        return
    }

    if (viewingFlashcards != null) {
        InlineFlashcardViewer(flashcards = viewingFlashcards!!, onBackToChat = { viewingFlashcardMessageId = null },
            onReview = onReviewFlashcard, onDelete = { onDeleteFlashcard(it); viewingFlashcardMessageId = null })
        return
    }

    if (viewingQuizzes != null) {
        InlineQuizViewer(quizzes = viewingQuizzes!!, onBackToChat = { viewingQuizMessageId = null },
            onDelete = { onDeleteQuiz(it); viewingQuizMessageId = null })
        return
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // === HEADER ===
        Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top row: Back | Conversation | Model | Document title
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp).padding(top = 28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    // Conversation dropdown
                    Box {
                        TextButton(onClick = { showConvMenu = !showConvMenu }, modifier = Modifier.height(36.dp)) {
                            Text(
                                conversations.find { it.id == currentConversationId }?.title ?: "New Chat",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showConvMenu, onDismissRequest = { showConvMenu = false }, modifier = Modifier.width(200.dp)) {
                            conversations.forEach { conv ->
                                DropdownMenuItem(
                                    text = { Text(conv.title, maxLines = 1) },
                                    onClick = { onSwitchConversation(conv.id); showConvMenu = false },
                                    trailingIcon = {
                                        if (conversations.size > 1) IconButton(onClick = { onDeleteConversation(conv.id); showConvMenu = false }) {
                                            Icon(Icons.Default.Close, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("+ New Chat", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) },
                                onClick = { onCreateConversation(); showConvMenu = false }
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Model selector
                    if (availableModels.isNotEmpty()) {
                        Box {
                            TextButton(onClick = { showModelMenu = !showModelMenu }, modifier = Modifier.height(36.dp)) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(4.dp))
                                Text(selectedModel?.displayName ?: "Model", style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                            DropdownMenu(expanded = showModelMenu, onDismissRequest = { showModelMenu = false }, modifier = Modifier.width(180.dp)) {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model.displayName, style = MaterialTheme.typography.bodyMedium) },
                                        onClick = { onModelSelected(model); showModelMenu = false },
                                        trailingIcon = {
                                            if (model.id == selectedModel?.id) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Document title
                    Spacer(Modifier.width(8.dp))
                    Text(documentTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        }

        // === MESSAGES ===
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(16.dp))
                        Text("Ask me anything about this document", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text("I have the full document context loaded", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
                                    Text(text = message.content, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
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

        // === INPUT BAR ===
        Surface(tonalElevation = 1.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Options dropdown
                AnimatedVisibility(visible = showOptionsMenu) {
                    QuestionOptionsMenu(
                        onGenerateFlashcards = { showOptionsMenu = false; onGenerateFullDocFlashcards() },
                        onGenerateQuiz = { showOptionsMenu = false; onGenerateFullDocQuiz() },
                        onStartGuidedLearning = { showOptionsMenu = false; onStartGuidedLearning() },
                        onShowQuestionerConfig = { showOptionsMenu = false; showQuestionerConfig = true },
                        onNavigateFlashcards = { showOptionsMenu = false; onNavigateFlashcards() },
                        onNavigateQuizzes = { showOptionsMenu = false; onNavigateQuizzes() }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    // + button
                    IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                        Icon(Icons.Default.Add, contentDescription = "Options",
                            tint = if (showOptionsMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Text input
                    OutlinedTextField(
                        value = inputText, onValueChange = { inputText = it },
                        placeholder = {
                            Text(if (questionerState.awaitingAnswer) "Type your answer..." else "Ask about this document...")
                        },
                        modifier = Modifier.weight(1f), maxLines = 5,
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                if (questionerState.awaitingAnswer) { onSubmitAnswer(inputText.trim()) }
                                else { onSendMessage(inputText.trim()) }
                                inputText = ""
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    // Send/Stop button
                    FloatingActionButton(
                        onClick = {
                            if (isStreaming) onStopStreaming()
                            else if (inputText.isNotBlank()) {
                                if (questionerState.awaitingAnswer) onSubmitAnswer(inputText.trim())
                                else onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        containerColor = if (isStreaming) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        AnimatedContent(targetState = isStreaming, label = "sendStop") { streaming ->
                            if (streaming) Icon(Icons.Outlined.Stop, contentDescription = "Stop")
                            else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
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
    onGenerateFlashcards: () -> Unit,
    onGenerateQuiz: () -> Unit,
    onStartGuidedLearning: () -> Unit,
    onShowQuestionerConfig: () -> Unit,
    onNavigateFlashcards: () -> Unit,
    onNavigateQuizzes: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    Icon(Icons.Default.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Column { Text("Quiz", style = MaterialTheme.typography.labelMedium); Text("From full document", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onStartGuidedLearning), shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column { Text("Guided Learning", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer);
                    Text("AI teaches you everything", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)) }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onShowQuestionerConfig), shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Column { Text("Questioner", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onErrorContainer);
                    Text("AI quizzes you on every topic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)) }
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
                    Icon(Icons.Default.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Column { Text("Quizzes", style = MaterialTheme.typography.labelMedium); Text("Self-check questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
private fun GeneratedFlashcardCard(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$count Quiz Question${if (count != 1) "s" else ""} Generated", style = MaterialTheme.typography.labelLarge)
                Text("Tap to test yourself", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open quiz", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}
