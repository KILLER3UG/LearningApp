package com.selfproject.learningapp.ui.screens

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
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.fileparser.FileType
import com.selfproject.learningapp.model.DocumentUiState
import com.selfproject.learningapp.ui.components.*
import com.selfproject.learningapp.viewmodel.MainViewModel
import com.selfproject.learningapp.viewmodel.PendingAttachment

// Full MIME array for universal study material support (Issue 1)
private val ALL_SUPPORTED_MIME_TYPES = arrayOf(
    "application/pdf",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.ms-powerpoint",
    "application/epub+zip",
    "application/rtf",
    "image/png", "image/jpeg", "image/heic", "image/webp", "image/heif",
    "image/svg+xml",
    "text/plain", "text/markdown", "text/html", "text/rtf",
    "text/x-python", "text/javascript", "application/javascript",
    "text/typescript", "text/jsx",
    "text/x-c++src", "text/x-c", "text/x-java-source",
    "application/json", "text/csv", "application/sql",
    "audio/mpeg", "audio/mp3", "audio/mp4", "audio/m4a"
)

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
    var showSearch by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mime = ctx.contentResolver.getType(uri)
        val pfd = uri.let { ctx.contentResolver.openFileDescriptor(it, "r") }
        val size = pfd?.statSize ?: 0
        pfd?.close()
        val sizeError = FileType.validateSize(size)
        if (sizeError != null) {
            Toast.makeText(ctx, sizeError, Toast.LENGTH_SHORT).show()
        } else {
            val fileName = uri.lastPathSegment ?: "file"
            val ft = FileType.fromMimeOrExtension(mime, uri, ctx)
            viewModel.addPendingAttachment(
                PendingAttachment(uri = uri, fileName = fileName, fileType = ft, sizeBytes = size)
            )?.let { error -> Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show() }
        }
    }

    fun openFilePicker() {
        filePickerLauncher.launch(ALL_SUPPORTED_MIME_TYPES)
    }

    val showChat = viewModel.showFullDocumentChat
    val doc = (viewModel.uiState as? DocumentUiState.Success)?.document

    if (showChat && doc != null) {
        FullDocumentChat(
            documentContent = doc.content,
            documentTitle = doc.displayName,
            queryState = viewModel.aiQueryState,
            messages = viewModel.currentMessages,
            onSendMessage = { viewModel.sendChatMessage(it) },
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
            onReviewFlashcard = { card, correct -> viewModel.recordFlashcardResult(card, correct) },
            onDeleteFlashcard = { viewModel.flashcards.remove(it) },
            onDeleteQuiz = { viewModel.quizzes.remove(it) }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val state = viewModel.uiState
                    val title = if (state is DocumentUiState.Success) state.document.displayName else "StudyNotes"
                    Text(title)
                },
                actions = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    ModelSelector(
                        selectedModel = viewModel.selectedModel,
                        availableModels = viewModel.availableModels,
                        onModelSelected = { viewModel.selectModel(it) }
                    )
                    if (viewModel.uiState is DocumentUiState.Success) {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    if (viewModel.bookmarks.isNotEmpty()) {
                        BadgedBox(badge = { Badge { Text(viewModel.bookmarks.size.toString()) } }) {
                            IconButton(onClick = { showBookmarks = true }) {
                                Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks")
                            }
                        }
                    } else {
                        IconButton(onClick = { showBookmarks = true }) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks")
                        }
                    }
                    IconButton(onClick = { showFlashcards = true }) {
                        Icon(Icons.Filled.CardMembership, contentDescription = "Flashcards")
                    }
                    IconButton(onClick = { showQuizzes = true }) {
                        Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "Quizzes")
                    }
                    IconButton(onClick = { openFilePicker() }) {
                        Icon(Icons.Default.Add, contentDescription = "Open File")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (viewModel.uiState is DocumentUiState.Success) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openFullDocumentChat() },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    text = { Text("Ask AI about this document") },
                )
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
                        filePickerLauncher.launch(arrayOf("text/markdown", "text/plain", "text/*"))
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
                onNavigateToBookmark = { /* TODO: scroll */ },
                onDeleteBookmark = { viewModel.removeBookmark(it) },
                onDismiss = { showBookmarks = false }
            )
        }
        if (showFlashcards) {
            FlashcardViewer(
                flashcards = viewModel.flashcards.toList(),
                onReview = { card, correct -> viewModel.recordFlashcardResult(card, correct) },
                onDelete = { viewModel.flashcards.remove(it) },
                onDismiss = { showFlashcards = false }
            )
        }
        if (showQuizzes) {
            QuizViewer(
                quizzes = viewModel.quizzes.toList(),
                onDelete = { viewModel.quizzes.remove(it) },
                onDismiss = { showQuizzes = false }
            )
        }
    }
}