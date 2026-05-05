package com.selfproject.learningapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.selfproject.learningapp.BuildConfig
import com.selfproject.learningapp.data.AiRepository
import com.selfproject.learningapp.data.ContextExtractor
import com.selfproject.learningapp.data.DocumentChunker
import com.selfproject.learningapp.data.FileRepository
import com.selfproject.learningapp.data.SearchEngine
import com.selfproject.learningapp.data.StudyItemParser
import com.selfproject.learningapp.data.StudyRepository
import com.selfproject.learningapp.data.local.BookmarkEntity
import com.selfproject.learningapp.data.local.FlashcardEntity
import com.selfproject.learningapp.data.local.QuizEntity
import com.selfproject.learningapp.model.HighlightRange
import com.selfproject.learningapp.model.AiConversation
import com.selfproject.learningapp.model.AiModel
import com.selfproject.learningapp.model.AiQueryState
import com.selfproject.learningapp.model.ChatMessage
import com.selfproject.learningapp.model.Document
import com.selfproject.learningapp.model.DocumentUiState
import com.selfproject.learningapp.model.MessageType as MsgType
import com.selfproject.learningapp.model.ModelCatalog
import com.selfproject.learningapp.model.QuestionerConfig
import com.selfproject.learningapp.model.QuestionerState
import com.selfproject.learningapp.model.Role
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Job

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)
    private val aiRepository by lazy { AiRepository(BuildConfig.GEMINI_API_KEY) }
    private val studyRepository = StudyRepository(application)

    var uiState by mutableStateOf<DocumentUiState>(DocumentUiState.Empty)
        private set

    var aiQueryState by mutableStateOf<AiQueryState>(AiQueryState.Idle)
        private set

    var isApiConfigured: Boolean = BuildConfig.GEMINI_API_KEY.isNotEmpty() &&
            BuildConfig.GEMINI_API_KEY != "YOUR_API_KEY_HERE"
        private set

    // Phase 2: Bookmarks
    val bookmarks = mutableStateListOf<BookmarkEntity>()

    // Phase 2: Flashcards
    val flashcards = mutableStateListOf<FlashcardEntity>()

    // Phase 2: Quizzes
    val quizzes = mutableStateListOf<QuizEntity>()

    // Model selection
    var selectedModel by mutableStateOf<AiModel>(ModelCatalog.defaultModel)
        private set

    val availableModels = ModelCatalog.models

    // Phase 2: Search
    var searchQuery by mutableStateOf("")
        private set
    var searchResults by mutableStateOf<List<SearchEngine.SearchResult>>(emptyList())
        private set
    var currentSearchMatchIndex by mutableStateOf(-1)
        private set

    // Full-document AI chat
    var conversations by mutableStateOf(listOf<AiConversation>())
        private set
    var currentConversationId by mutableStateOf<String?>(null)
        private set
    var showFullDocumentChat by mutableStateOf(false)
        private set

    // Streaming control
    @Volatile
    private var isStreaming = false
    private var streamingJob: Job? = null

    // Text highlights from AI responses
    var currentHighlights by mutableStateOf<List<HighlightRange>>(emptyList())
        private set

    // Questioner state
    var questionerState by mutableStateOf(QuestionerState())
        private set

    // Get current conversation messages
    val currentMessages: List<ChatMessage>
        get() {
            val conv = conversations.find { it.id == currentConversationId }
            return conv?.messages ?: emptyList()
        }

    val conversationTitles: List<Pair<String, String>>
        get() = conversations.map { it.id to it.title }

    /**
     * Sets the active AI model.
     */
    fun selectModel(model: AiModel) {
        selectedModel = model
        aiRepository.setModel(model)
    }

    /**
     * Loads a markdown file from the given URI.
     */
    fun loadDocument(uri: Uri) {
        uiState = DocumentUiState.Loading
        viewModelScope.launch {
            try {
                val cachedContent = fileRepository.getCachedContent(uri)
                if (cachedContent != null) {
                    val displayName = fileRepository.getDisplayName(uri)
                    uiState = DocumentUiState.Success(
                        Document(uri.toString(), displayName, cachedContent)
                    )
                    refreshDocument(uri)
                    loadStudyData(uri.toString())
                    return@launch
                }

                val fileData = fileRepository.readFileContent(uri)
                uiState = DocumentUiState.Success(
                    Document(uri.toString(), fileData.displayName, fileData.content)
                )
                fileRepository.cacheContent(uri, fileData.content)
                loadStudyData(uri.toString())
            } catch (e: Exception) {
                uiState = DocumentUiState.Error("Failed to load file: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun refreshDocument(uri: Uri) {
        try {
            val fileData = fileRepository.readFileContent(uri)
            uiState = DocumentUiState.Success(
                Document(uri.toString(), fileData.displayName, fileData.content)
            )
            fileRepository.cacheContent(uri, fileData.content)
        } catch (e: Exception) {
            // Silently fail
        }
    }

    private fun loadStudyData(documentUri: String) {
        viewModelScope.launch {
            studyRepository.getBookmarks(documentUri).collectLatest { bookmarks ->
                this@MainViewModel.bookmarks.clear()
                this@MainViewModel.bookmarks.addAll(bookmarks)
            }
        }
        viewModelScope.launch {
            studyRepository.getFlashcards(documentUri).collectLatest { cards ->
                this@MainViewModel.flashcards.clear()
                this@MainViewModel.flashcards.addAll(cards)
            }
        }
        viewModelScope.launch {
            studyRepository.getQuizzes(documentUri).collectLatest { qs ->
                this@MainViewModel.quizzes.clear()
                this@MainViewModel.quizzes.addAll(qs)
            }
        }
    }

    /**
     * Opens the full-document AI chat, creating a new conversation if needed.
     */
    fun openFullDocumentChat() {
        showFullDocumentChat = true
        if (currentConversationId == null || conversations.isEmpty()) {
            createNewConversation()
        }
    }

    /**
     * Creates a new conversation and switches to it.
     */
    fun createNewConversation() {
        val newConv = AiConversation()
        conversations = conversations + newConv
        currentConversationId = newConv.id
    }

    /**
     * Switches to an existing conversation.
     */
    fun switchConversation(conversationId: String) {
        currentConversationId = conversationId
    }

    /**
     * Deletes a conversation.
     */
    fun deleteConversation(conversationId: String) {
        conversations = conversations.filter { it.id != conversationId }
        if (currentConversationId == conversationId) {
            currentConversationId = conversations.lastOrNull()?.id
        }
        if (conversations.isEmpty() && showFullDocumentChat) {
            createNewConversation()
        }
    }

    /**
     * Stops the current AI streaming.
     */
    fun stopStreaming() {
        isStreaming = false
        streamingJob?.cancel()
        streamingJob = null
        if (aiQueryState is AiQueryState.Loading) {
            aiQueryState = AiQueryState.Success("Response stopped.")
        }
    }

    private fun addMessageToCurrentConversation(message: ChatMessage) {
        val convId = currentConversationId ?: return
        val idx = conversations.indexOfFirst { it.id == convId }
        if (idx >= 0) {
            val updated = conversations[idx].copy(
                messages = conversations[idx].messages + message,
                title = if (conversations[idx].messages.size == 1 && message.role == Role.USER) {
                    message.content.take(30) + if (message.content.length > 30) "..." else ""
                } else conversations[idx].title
            )
            val newList = conversations.toMutableList()
            newList[idx] = updated
            conversations = newList
        }
    }

    private fun updateLastAiMessage(content: String) {
        val convId = currentConversationId ?: return
        val idx = conversations.indexOfFirst { it.id == convId }
        if (idx >= 0) {
            val msgs = conversations[idx].messages.toMutableList()
            val lastAiIdx = msgs.indexOfLast { it.role == Role.AI }
            if (lastAiIdx >= 0) {
                msgs[lastAiIdx] = msgs[lastAiIdx].copy(content = content)
            } else {
                msgs.add(ChatMessage(role = Role.AI, content = content))
            }
            conversations = conversations.toMutableList().also {
                it[idx] = it[idx].copy(messages = msgs)
            }
        }
    }

    /**
     * Closes the full-document AI chat.
     */
    fun closeFullDocumentChat() {
        showFullDocumentChat = false
        stopStreaming()
    }

    /**
     * Sends a message in the full-document chat.
     */
    fun sendChatMessage(message: String) {
        val doc = (uiState as? DocumentUiState.Success)?.document ?: return

        // Add user message
        addMessageToCurrentConversation(ChatMessage(role = Role.USER, content = message))

        // Create placeholder for AI response
        addMessageToCurrentConversation(ChatMessage(role = Role.AI, content = ""))

        // Start streaming AI response with full document context
        aiQueryState = AiQueryState.Loading
        isStreaming = true
        streamingJob = viewModelScope.launch {
            try {
                val systemPrompt = """
                    You are an expert study assistant analyzing the following document.

                    DOCUMENT: ${doc.displayName}

                    FULL CONTENT:
                    ${doc.content}

                    Guidelines:
                    - Always ground your responses in the document content
                    - When explaining concepts, reference specific sections or quote relevant passages
                    - Use formatting: **bold** for key terms, \`code\` for technical terms, tables for comparisons
                    - If the user asks something not covered in the document, say so and offer what IS covered
                    - Be concise but thorough
                """.trimIndent()

                val fullPrompt = "$systemPrompt\n\nUSER QUESTION: $message"

                aiRepository.queryAiDirect(fullPrompt)
                    .catch { e ->
                        if (isStreaming) {
                            aiQueryState = AiQueryState.Error("AI request failed: ${e.localizedMessage}")
                        }
                    }
                    .collect { response ->
                        if (!isStreaming) return@collect
                        aiQueryState = AiQueryState.Success(response)
                        updateLastAiMessage(response)
                    }
            } catch (e: Exception) {
                if (isStreaming) {
                    aiQueryState = AiQueryState.Error("AI request failed: ${e.localizedMessage}")
                }
            } finally {
                isStreaming = false
                streamingJob = null
            }
        }
    }

    /**
     * Highlights text in the document based on AI response keywords.
     */
    fun highlightFromAiResponse(aiResponse: String, documentContent: String) {
        val highlights = mutableListOf<HighlightRange>()

        // Extract key terms from AI response (bold terms, headings, quoted text)
        val keywords = extractKeyTerms(aiResponse)

        for (keyword in keywords) {
            val results = SearchEngine.search(documentContent, keyword)
            for (result in results.take(3)) { // Limit highlights to avoid overwhelming
                highlights.add(
                    HighlightRange(
                        start = result.startIndex,
                        end = result.endIndex,
                        color = Color(0xFFFFF59D), // Yellow highlight
                    )
                )
            }
        }

        currentHighlights = highlights
    }

    /**
     * Clears document highlights.
     */
    fun clearHighlights() {
        currentHighlights = emptyList()
    }

    private fun extractKeyTerms(text: String): List<String> {
        val terms = mutableListOf<String>()

        // Extract bold terms: **term**
        val boldPattern = Regex("\\*\\*(.+?)\\*\\*")
        for (match in boldPattern.findAll(text)) {
            val term = match.groupValues[1].trim()
            if (term.length in 3..40 && !terms.contains(term)) {
                terms.add(term)
            }
        }

        // Extract inline code: `term`
        val codePattern = Regex("`(.+?)`")
        for (match in codePattern.findAll(text)) {
            val term = match.groupValues[1].trim()
            if (term.length in 3..40 && !terms.contains(term)) {
                terms.add(term)
            }
        }

        // If no formatted terms found, use first few nouns/phrases
        if (terms.isEmpty()) {
            val words = text.split(Regex("\\s+"))
                .filter { it.length > 4 }
                .map { it.replace(Regex("[^a-zA-Z0-9]"), "") }
                .filter { it.isNotBlank() }
                .take(5)
            terms.addAll(words)
        }

        return terms.take(8)
    }

    /**
     * Phase 2: Add bookmark at current position.
     */
    fun addBookmark(documentUri: String, heading: String, position: Int) {
        viewModelScope.launch {
            if (!studyRepository.hasBookmark(documentUri, heading)) {
                studyRepository.addBookmark(
                    BookmarkEntity(
                        documentUri = documentUri,
                        heading = heading,
                        position = position,
                    )
                )
            }
        }
    }

    /**
     * Phase 2: Remove a bookmark.
     */
    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            studyRepository.removeBookmark(bookmark)
        }
    }

    /**
     * Phase 2: Detect headings from document content.
     */
    fun detectHeadings(content: String): List<Pair<String, Int>> {
        return ContextExtractor.detectHeadings(content)
    }

    /**
     * Phase 2: Generate flashcards from selected text via AI.
     */
    fun generateFlashcards(content: String, documentUri: String) {
        aiQueryState = AiQueryState.Loading
        viewModelScope.launch {
            try {
                val response = aiRepository.generateFlashcards(content)
                val flashcards = StudyItemParser.parseFlashcards(response, documentUri)
                studyRepository.addFlashcards(flashcards)
                aiQueryState = AiQueryState.Success("Generated ${flashcards.size} flashcards!")
            } catch (e: Exception) {
                aiQueryState = AiQueryState.Error("Failed to generate flashcards: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Generate flashcards from the ENTIRE document via AI.
     * Chunks large documents to ensure full coverage.
     */
    fun generateFlashcardsFromFullDoc(content: String, documentUri: String) {
        aiQueryState = AiQueryState.Loading
        viewModelScope.launch {
            try {
                val chunks = DocumentChunker.chunkContent(content)
                val allFlashcards = mutableListOf<com.selfproject.learningapp.data.local.FlashcardEntity>()
                val maxFlashcards = 30 // Limit to prevent memory overload

                for (chunk in chunks) {
                    if (allFlashcards.size >= maxFlashcards) break
                    val response = aiRepository.generateFlashcards(chunk)
                    val flashcards = StudyItemParser.parseFlashcards(response, documentUri)
                    allFlashcards.addAll(flashcards.take(maxFlashcards - allFlashcards.size))
                }

                if (allFlashcards.isNotEmpty()) {
                    studyRepository.addFlashcards(allFlashcards)
                }

                // Add a special chat message for the generated flashcards
                addMessageToCurrentConversation(
                    ChatMessage(
                        role = Role.AI,
                        content = "📚 I've created **${allFlashcards.size} flashcards** covering the entire document. Tap the card above to study!",
                        type = MsgType.FLASHCARD_GENERATED,
                        flashcards = allFlashcards,
                    )
                )
                aiQueryState = AiQueryState.Success("Generated ${allFlashcards.size} flashcards!")
            } catch (e: Exception) {
                aiQueryState = AiQueryState.Error("Failed to generate flashcards: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Generate quiz from the ENTIRE document via AI.
     * Chunks large documents to ensure full coverage.
     */
    fun generateQuizFromFullDoc(content: String, documentUri: String) {
        aiQueryState = AiQueryState.Loading
        viewModelScope.launch {
            try {
                val chunks = DocumentChunker.chunkContent(content)
                val allQuizzes = mutableListOf<com.selfproject.learningapp.data.local.QuizEntity>()
                val maxQuizzes = 30 // Limit to prevent memory overload

                for (chunk in chunks) {
                    if (allQuizzes.size >= maxQuizzes) break
                    val response = aiRepository.generateQuiz(chunk)
                    val quizzes = StudyItemParser.parseQuizzes(response, documentUri, "Full document")
                    allQuizzes.addAll(quizzes.take(maxQuizzes - allQuizzes.size))
                }

                if (allQuizzes.isNotEmpty()) {
                    studyRepository.addQuizzes(allQuizzes)
                }

                // Add a special chat message for the generated quiz
                addMessageToCurrentConversation(
                    ChatMessage(
                        role = Role.AI,
                        content = "❓ I've created a **${allQuizzes.size}-question quiz** covering the entire document. Tap the card above to test yourself!",
                        type = MsgType.QUIZ_GENERATED,
                        quizzes = allQuizzes,
                    )
                )
                aiQueryState = AiQueryState.Success("Generated ${allQuizzes.size} quiz questions!")
            } catch (e: Exception) {
                aiQueryState = AiQueryState.Error("Failed to generate quiz: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Start a guided learning session. AI teaches the entire document systematically.
     */
    fun startGuidedLearning() {
        val doc = (uiState as? DocumentUiState.Success)?.document ?: return

        addMessageToCurrentConversation(ChatMessage(role = Role.USER, content = "Start guided learning for this document"))

        aiQueryState = AiQueryState.Loading
        isStreaming = true
        streamingJob = viewModelScope.launch {
            try {
                val prompt = """
                    You are an expert teacher. Your task is to systematically teach the user everything in the following document.

                    DOCUMENT: ${doc.displayName}

                    FULL CONTENT:
                    ${doc.content}

                    Guidelines for guided learning:
                    1. Start by analyzing the document structure (headings, topics, sections)
                    2. Create a structured learning plan with clear modules/topics
                    3. Present the plan first, then teach each module one at a time
                    4. Use clear explanations, examples, analogies, and real-world applications
                    5. After each module, ask the user a quick check question to verify understanding
                    6. Wait for the user's response before moving to the next module
                    7. Use formatting: headings, bullet points, tables, bold for key terms
                    8. Be thorough but engaging - cover every topic in the document

                    Start by presenting your learning plan with module names and brief descriptions.
                """.trimIndent()

                aiRepository.queryAiDirect(prompt)
                    .catch { e ->
                        if (isStreaming) aiQueryState = AiQueryState.Error("Guided learning failed: ${e.localizedMessage}")
                    }
                    .collect { response ->
                        aiQueryState = AiQueryState.Success(response)
                        updateLastAiMessage(response)
                    }
            } catch (e: Exception) {
                if (isStreaming) aiQueryState = AiQueryState.Error("Guided learning failed: ${e.localizedMessage}")
            } finally {
                isStreaming = false
                streamingJob = null
            }
        }
    }

    /**
     * Start the Questioner session.
     */
    fun startQuestioner(config: QuestionerConfig) {
        questionerState = QuestionerState(
            isActive = true,
            config = config,
            totalQuestions = config.questionCount,
        )
    }

    /**
     * Send first question from AI to user.
     */
    fun askFirstQuestion() {
        val doc = (uiState as? DocumentUiState.Success)?.document ?: return

        addMessageToCurrentConversation(ChatMessage(role = Role.USER, content = "Start questioner session"))

        questionerState = questionerState.copy(
            isActive = true,
            currentQuestion = 1,
            awaitingAnswer = true,
        )

        aiQueryState = AiQueryState.Loading
        isStreaming = true
        streamingJob = viewModelScope.launch {
            try {
                val prompt = """
                    You are a rigorous examiner testing the user's understanding of the following document.

                    DOCUMENT: ${doc.displayName}

                    FULL CONTENT:
                    ${doc.content}

                    MODE: ${questionerState.config.mode.systemPrompt}

                    INSTRUCTIONS:
                    1. Ask EXACTLY ${questionerState.config.questionCount} questions total, covering ALL topics in the document systematically
                    2. Start with Question 1 of ${questionerState.config.questionCount}
                    3. Format: "Question X of Y: [your question]"
                    4. After the user answers, you will be given their response in the next message
                    5. For now, ONLY ask Question 1. Do NOT provide the answer.
                    6. Base questions strictly on the document content. Cover every section.
                """.trimIndent()

                aiRepository.queryAiDirect(prompt)
                    .catch { e ->
                        aiQueryState = AiQueryState.Error("Questioner failed: ${e.localizedMessage}")
                    }
                    .collect { response ->
                        aiQueryState = AiQueryState.Success(response)
                        updateLastAiMessage(response)
                    }
            } catch (e: Exception) {
                aiQueryState = AiQueryState.Error("Questioner failed: ${e.localizedMessage}")
            } finally {
                isStreaming = false
                streamingJob = null
            }
        }
    }

    /**
     * Submit user's answer to the current question. AI critiques it and asks the next.
     */
    fun submitQuestionerAnswer(userAnswer: String) {
        val doc = (uiState as? DocumentUiState.Success)?.document ?: return

        addMessageToCurrentConversation(ChatMessage(role = Role.USER, content = userAnswer))

        aiQueryState = AiQueryState.Loading
        isStreaming = true
        streamingJob = viewModelScope.launch {
            try {
                val nextQ = questionerState.currentQuestion + 1
                val isLast = nextQ > questionerState.config.questionCount

                val mode = questionerState.config.mode
                val taskInstructions = if (mode == com.selfproject.learningapp.model.QuestionerMode.CRITIC) {
                    "1. Identify SPECIFIC errors, omissions, or inaccuracies in their answer\n" +
                    "2. Quote the relevant document passage that contradicts or supports their answer\n" +
                    "3. Explain exactly WHY their answer is wrong, incomplete, or misleading\n" +
                    "4. Provide the corrected version based on the document\n" +
                    "5. Score their answer out of 10\n" +
                    "6. Be direct and honest - don't soften criticism"
                } else {
                    "1. Acknowledge what they got right\n" +
                    "2. Identify a gap, assumption, or unstated implication in their reasoning\n" +
                    "3. Ask a probing follow-up question that forces deeper thinking\n" +
                    "4. Score their answer out of 10\n" +
                    "5. Guide them to discover the missing piece themselves - don't just give the answer"
                }

                val finalInstructions = if (isLast) {
                    "$taskInstructions\n" +
                    "6. This is the FINAL question. After your critique/response, say \"Session Complete! Your average score: X/10\" and provide a summary of strengths and areas for improvement based on ALL their answers.\n" +
                    "7. Do NOT ask another question."
                } else {
                    "$taskInstructions\n" +
                    "6. Then ask the next question. Format: \"Question $nextQ of ${questionerState.config.questionCount}: [your question]\"\n" +
                    "7. Move to a NEW topic from the document that hasn't been covered yet\n" +
                    "8. Cover ALL topics systematically - don't repeat areas"
                }

                val prompt = """
                    You are a rigorous examiner. The user just answered your question about the document.

                    DOCUMENT CONTENT (for reference):
                    ${doc.content}

                    MODE: ${mode.systemPrompt}

                    USER'S ANSWER:
                    $userAnswer

                    YOUR TASK:
                    $finalInstructions
                """.trimIndent()

                aiRepository.queryAiDirect(prompt)
                    .catch { e ->
                        if (isStreaming) aiQueryState = AiQueryState.Error("Questioner failed: ${e.localizedMessage}")
                    }
                    .collect { response ->
                        if (!isStreaming) return@collect
                        aiQueryState = AiQueryState.Success(response)
                        updateLastAiMessage(response)

                        // Update state
                        if (!isLast) {
                            questionerState = questionerState.copy(
                                currentQuestion = nextQ,
                                awaitingAnswer = true,
                            )
                        } else {
                            questionerState = questionerState.copy(
                                currentQuestion = questionerState.config.questionCount,
                                awaitingAnswer = false,
                                completed = true,
                            )
                        }
                    }
            } catch (e: Exception) {
                if (isStreaming) aiQueryState = AiQueryState.Error("Questioner failed: ${e.localizedMessage}")
            } finally {
                isStreaming = false
                streamingJob = null
            }
        }
    }

    /**
     * Cancel the Questioner session.
     */
    fun cancelQuestioner() {
        questionerState = QuestionerState()
    }

    /**
     * Phase 2: Generate quiz from selected text via AI.
     */
    fun generateQuiz(selectedText: String, fullContent: String, documentUri: String) {
        aiQueryState = AiQueryState.Loading
        viewModelScope.launch {
            try {
                val context = ContextExtractor.extractSurroundingContext(selectedText, fullContent)
                val response = aiRepository.generateQuiz(context)
                val quizzes = StudyItemParser.parseQuizzes(response, documentUri, selectedText)
                studyRepository.addQuizzes(quizzes)
                aiQueryState = AiQueryState.Success("Generated ${quizzes.size} quiz questions!")
            } catch (e: Exception) {
                aiQueryState = AiQueryState.Error("Failed to generate quiz: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Phase 2: Record flashcard review result.
     */
    fun recordFlashcardResult(flashcard: FlashcardEntity, correct: Boolean) {
        viewModelScope.launch {
            studyRepository.recordFlashcardResult(flashcard, correct)
        }
    }

    /**
     * Phase 2: Search within current document.
     */
    fun searchInDocument(query: String) {
        searchQuery = query
        val doc = (uiState as? DocumentUiState.Success)?.document?.content ?: return
        searchResults = SearchEngine.search(doc, query)
        currentSearchMatchIndex = if (searchResults.isNotEmpty()) 0 else -1
    }

    /**
     * Phase 2: Navigate to next search result.
     */
    fun nextSearchResult() {
        if (searchResults.isNotEmpty()) {
            currentSearchMatchIndex = (currentSearchMatchIndex + 1) % searchResults.size
        }
    }

    /**
     * Phase 2: Navigate to previous search result.
     */
    fun previousSearchResult() {
        if (searchResults.isNotEmpty()) {
            currentSearchMatchIndex = if (currentSearchMatchIndex <= 0) searchResults.size - 1 else currentSearchMatchIndex - 1
        }
    }

    /**
     * Phase 2: Clear search.
     */
    fun clearSearch() {
        searchQuery = ""
        searchResults = emptyList()
        currentSearchMatchIndex = -1
    }

    /**
     * Sends selected text to AI with surrounding context.
     */
    fun askAi(selectedText: String, fullDocumentContent: String, promptTemplate: String) {
        aiQueryState = AiQueryState.Loading
        viewModelScope.launch {
            val surroundingContext = ContextExtractor.extractSurroundingContext(
                selectedText,
                fullDocumentContent
            )

            aiRepository.queryAi(selectedText, surroundingContext, promptTemplate)
                .catch { e ->
                    aiQueryState = AiQueryState.Error("AI request failed: ${e.localizedMessage}")
                }
                .collect { response ->
                    aiQueryState = AiQueryState.Success(response)
                    // Auto-highlight relevant terms in the document
                    highlightFromAiResponse(response, fullDocumentContent)
                }
        }
    }

    fun resetAiState() {
        aiQueryState = AiQueryState.Idle
    }

    /**
     * Saves AI response to sidecar file.
     */
    fun saveResponse(documentUri: String, selectedText: String, response: String) {
        viewModelScope.launch {
            try {
                val uri = Uri.parse(documentUri)
                val sidecarContent = buildString {
                    appendLine("---")
                    appendLine("**AI Response** - ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
                    appendLine("> Selected: $selectedText")
                    appendLine()
                    appendLine(response)
                    appendLine()
                    appendLine("---")
                }
                fileRepository.appendToSidecarFile(uri, sidecarContent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
