package com.selfproject.learningapp.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.OpenableColumns
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.fileparser.FileType
import com.selfproject.learningapp.model.DocumentUiState
import com.selfproject.learningapp.ui.components.*
import com.selfproject.learningapp.viewmodel.MainViewModel
import com.selfproject.learningapp.viewmodel.PendingAttachment

// Keep the picker open to every local source; the parser decides what can be extracted.
private val UNIVERSAL_PICKER_MIME_TYPES = arrayOf("*/*")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val ctx = LocalContext.current

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    var promptTemplate by remember { mutableStateOf("Explain this clearly") }
    var showBookmarks by remember { mutableStateOf(false) }
    var showFlashcards by remember { mutableStateOf(false) }
    var showQuizzes by remember { mutableStateOf(false) }
    var showMatchGame by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    fun handlePickedUris(uris: List<Uri>) {
        if (uris.isEmpty()) return

        var firstAcceptedUri: Uri? = null
        uris.forEach { uri ->
            ctx.takePersistableReadPermission(uri)

            val mime = ctx.contentResolver.getType(uri)
            val size = getFileSize(ctx, uri)
            val sizeError = FileType.validateSize(size)
            if (sizeError != null) {
                Toast.makeText(ctx, "${getDisplayName(ctx, uri)}: $sizeError", Toast.LENGTH_SHORT).show()
            } else {
                val fileName = getDisplayName(ctx, uri)
                val ft = FileType.fromMimeOrExtension(mime, uri, ctx)
                viewModel.addPendingAttachment(
                    PendingAttachment(uri = uri, fileName = fileName, fileType = ft, sizeBytes = size)
                )?.let { error -> Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show() }

                if (firstAcceptedUri == null) {
                    firstAcceptedUri = uri
                }
            }
        }

        if (viewModel.uiState !is DocumentUiState.Success) {
            firstAcceptedUri?.let { viewModel.loadDocument(it) }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        handlePickedUris(uris)
    }

    fun openFilePicker() {
        filePickerLauncher.launch(UNIVERSAL_PICKER_MIME_TYPES)
    }

    val showChat = viewModel.showFullDocumentChat
    var showMoreMenu by remember { mutableStateOf(false) }
    val doc = (viewModel.uiState as? DocumentUiState.Success)?.document

    if (showChat && doc != null) {
        FullDocumentChat(
            documentContent = doc.content,
            documentTitle = doc.displayName,
            queryState = viewModel.aiQueryState,
            messages = viewModel.currentMessages,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onNotebookAction = { label, prompt -> viewModel.runNotebookAction(label, prompt) },
            onStopStreaming = { viewModel.stopStreaming() },
            onSubmitAnswer = { viewModel.submitQuestionerAnswer(it) },
            questionerState = viewModel.questionerState,
            onDismiss = { viewModel.closeFullDocumentChat() },
            conversations = viewModel.conversations,
            currentConversationId = viewModel.currentConversationId,
            onCreateConversation = { viewModel.createNewConversation() },
            onSwitchConversation = { viewModel.switchConversation(it) },
            onDeleteConversation = { viewModel.deleteConversation(it) },
            onModelSelected = { viewModel.selectModel(it) },
            availableModels = viewModel.availableModels,
            selectedModel = viewModel.selectedModel,
            onGenerateFullDocFlashcards = { viewModel.generateFlashcardsFromFullDoc(doc.content, doc.uri) },
            onGenerateFullDocQuiz = { viewModel.generateQuizFromFullDoc(doc.content, doc.uri) },
            onStartGuidedLearning = { viewModel.startGuidedLearning() },
            onStartQuestioner = { config ->
                viewModel.startQuestioner(config)
                viewModel.askFirstQuestion()
            },
            onNavigateFlashcards = { viewModel.closeFullDocumentChat(); showFlashcards = true },
            onNavigateQuizzes = { viewModel.closeFullDocumentChat(); showQuizzes = true },
            onNavigateMatch = { viewModel.closeFullDocumentChat(); showMatchGame = true },
            onReviewFlashcard = { card, correct -> viewModel.recordFlashcardResult(card, correct) },
            onDeleteFlashcard = { viewModel.deleteFlashcard(it) },
            onDeleteQuiz = { viewModel.deleteQuiz(it) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val state = viewModel.uiState
                    val title = if (state is DocumentUiState.Success) state.document.displayName else "StudyNotes"
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Model selector — essential context, stays in header
                    ModelSelector(
                        selectedModel = viewModel.selectedModel,
                        availableModels = viewModel.availableModels,
                        onModelSelected = { viewModel.selectModel(it) }
                    )
                    // Search icon (only when document is loaded)
                    if (viewModel.uiState is DocumentUiState.Success) {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    // Overflow menu — all secondary actions collapsed into one menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Home") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                onClick = { showMoreMenu = false; onNavigateToHome() }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = { showMoreMenu = false; onNavigateToSettings() }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Bookmarks") },
                                leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                                onClick = { showMoreMenu = false; showBookmarks = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Flashcards") },
                                leadingIcon = { Icon(Icons.Default.CardMembership, contentDescription = null) },
                                onClick = { showMoreMenu = false; showFlashcards = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Quizzes") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null) },
                                onClick = { showMoreMenu = false; showQuizzes = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Match") },
                                leadingIcon = { Icon(Icons.Default.Extension, contentDescription = null) },
                                onClick = { showMoreMenu = false; showMatchGame = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Open File") },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                onClick = { showMoreMenu = false; openFilePicker() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (viewModel.uiState is DocumentUiState.Success) {
                FloatingActionButton(
                    onClick = { viewModel.openFullDocumentChat() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Ask AI about this document")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                if (viewModel.pendingAttachments.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.pendingAttachments) { attachment ->
                            AttachmentChip(
                                fileName = attachment.fileName,
                                fileType = attachment.fileType,
                                fileSizeBytes = attachment.sizeBytes,
                                uploadState = attachment.uploadState,
                                onRemove = { viewModel.removePendingAttachment(attachment.id) },
                                onClick = { viewModel.loadDocument(attachment.uri) }
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                if (showSearch) {
                    SearchBar(
                        query = viewModel.searchQuery,
                        resultCount = viewModel.searchResults.size,
                        currentIndex = viewModel.currentSearchMatchIndex,
                        onQueryChange = { viewModel.searchInDocument(it) },
                        onNext = { viewModel.nextSearchResult() },
                        onPrevious = { viewModel.previousSearchResult() },
                        onClose = { showSearch = false; viewModel.clearSearch() }
                    )
                }

                when (val state = viewModel.uiState) {
                    is DocumentUiState.Empty -> DocumentEmptyState(onOpenFileClick = {
                        openFilePicker()
                    })
                    is DocumentUiState.Loading -> DocumentLoadingState()
                    is DocumentUiState.Error -> DocumentErrorState(message = state.message, onRetry = {})
                    is DocumentUiState.Success -> MarkdownViewer(
                        content = state.document.content,
                        onTextSelected = { text -> selectedText = text },
                        onAskAi = { text ->
                            selectedText = text
                            showBottomSheet = true
                            viewModel.askAi(
                                selectedText = text,
                                fullDocumentContent = state.document.content,
                                promptTemplate = promptTemplate
                            )
                        },
                        onBookmarkText = { text, position ->
                            val heading = text
                                .lineSequence()
                                .firstOrNull()
                                ?.trim()
                                ?.take(90)
                                .orEmpty()
                            if (heading.isNotBlank()) {
                                viewModel.addBookmark(state.document.uri, heading, position)
                                Toast.makeText(ctx, "Bookmark saved", Toast.LENGTH_SHORT).show()
                            }
                        },
                        highlights = viewModel.currentHighlights
                    )
                }
            }

            if (showBottomSheet) {
                AiResponseBottomSheet(
                    queryState = viewModel.aiQueryState,
                    onDismiss = { showBottomSheet = false; viewModel.resetAiState(); viewModel.clearHighlights() },
                    onSave = { response ->
                        val d = (viewModel.uiState as? DocumentUiState.Success)?.document
                        d?.let { viewModel.saveResponse(it.uri, selectedText, response) }
                        showBottomSheet = false
                        viewModel.resetAiState()
                    },
                    onGenerateFlashcards = {
                        val d = (viewModel.uiState as? DocumentUiState.Success)?.document
                        d?.let { viewModel.generateFlashcards(it.content, it.uri) }
                    },
                    onGenerateQuiz = {
                        val d = (viewModel.uiState as? DocumentUiState.Success)?.document
                        d?.let { viewModel.generateQuiz(selectedText, it.content, it.uri) }
                    },
                    promptTemplate = promptTemplate,
                    onPromptTemplateChange = { promptTemplate = it }
                )
            }
        }

        if (showBookmarks) {
            BookmarksPanel(
                bookmarks = viewModel.bookmarks.toList(),
                onNavigateToBookmark = { bookmark ->
                    showBookmarks = false
                    showSearch = true
                    viewModel.searchInDocument(bookmark.heading)
                },
                onDeleteBookmark = { viewModel.removeBookmark(it) },
                onDismiss = { showBookmarks = false }
            )
        }
        if (showFlashcards) {
            FlashcardViewer(
                flashcards = viewModel.flashcards.toList(),
                onReview = { card, correct -> viewModel.recordFlashcardResult(card, correct) },
                onDelete = { viewModel.deleteFlashcard(it) },
                onDismiss = { showFlashcards = false }
            )
        }
        if (showQuizzes) {
            QuizViewer(
                quizzes = viewModel.quizzes.toList(),
                onDelete = { viewModel.deleteQuiz(it) },
                onDismiss = { showQuizzes = false }
            )
        }
        if (showMatchGame) {
            MatchGameViewer(
                flashcards = viewModel.flashcards.toList(),
                onReview = { card, correct -> viewModel.recordFlashcardResult(card, correct) },
                onDismiss = { showMatchGame = false }
            )
        }
    }
}

private fun Context.takePersistableReadPermission(uri: Uri) {
    runCatching {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

private fun getFileSize(context: Context, uri: Uri): Long {
    return runCatching {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            descriptor.statSize
        } ?: 0L
    }.getOrDefault(0L)
}

private fun getDisplayName(context: Context, uri: Uri): String {
    var name = uri.lastPathSegment ?: "file"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}
