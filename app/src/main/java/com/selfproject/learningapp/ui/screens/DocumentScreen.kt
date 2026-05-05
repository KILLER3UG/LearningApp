package com.selfproject.learningapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.net.Uri
import com.selfproject.learningapp.model.DocumentUiState
import com.selfproject.learningapp.ui.components.*
import com.selfproject.learningapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
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
        uri?.let { viewModel.loadDocument(it) }
    }

    // FullDocumentChat rendered OUTSIDE Scaffold to fully cover everything
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
                    IconButton(onClick = {
                        filePickerLauncher.launch(arrayOf("text/markdown", "text/plain", "text/*"))
                    }) {
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
