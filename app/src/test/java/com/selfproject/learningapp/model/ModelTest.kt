package com.selfproject.learningapp.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for data models.
 */
class ModelTest {

    @Test
    fun `Document creates with expected values`() {
        val doc = Document(
            uri = "content://test/uri",
            displayName = "notes.md",
            content = "# Hello",
            lastModified = 12345L
        )

        assertEquals("content://test/uri", doc.uri)
        assertEquals("notes.md", doc.displayName)
        assertEquals("# Hello", doc.content)
        assertEquals(12345L, doc.lastModified)
    }

    @Test
    fun `Document defaults lastModified to current time`() {
        val before = System.currentTimeMillis()
        val doc = Document("uri", "name", "content")
        val after = System.currentTimeMillis()

        assertTrue(doc.lastModified in before..after)
    }

    @Test
    fun `AIResponse creates with expected values`() {
        val response = AIResponse(
            id = 1L,
            documentUri = "content://test",
            selectedText = "selected",
            prompt = "Explain this",
            response = "AI explanation",
            timestamp = 99999L
        )

        assertEquals(1L, response.id)
        assertEquals("content://test", response.documentUri)
        assertEquals("selected", response.selectedText)
        assertEquals("Explain this", response.prompt)
        assertEquals("AI explanation", response.response)
        assertEquals(99999L, response.timestamp)
    }

    @Test
    fun `Bookmark creates with expected values`() {
        val bookmark = Bookmark(
            id = 5L,
            documentUri = "content://doc",
            heading = "Introduction",
            position = 42,
            timestamp = 77777L
        )

        assertEquals(5L, bookmark.id)
        assertEquals("Introduction", bookmark.heading)
        assertEquals(42, bookmark.position)
    }

    @Test
    fun `Flashcard creates with expected values`() {
        val card = Flashcard(
            id = 10L,
            documentUri = "content://doc",
            question = "What is X?",
            answer = "X is Y",
            nextReview = 50000L,
            interval = 3,
            easeFactor = 2.7
        )

        assertEquals(10L, card.id)
        assertEquals("What is X?", card.question)
        assertEquals("X is Y", card.answer)
        assertEquals(3, card.interval)
        assertEquals(2.7, card.easeFactor, 0.001)
    }

    @Test
    fun `DocumentUiState Empty represents no document`() {
        val state: DocumentUiState = DocumentUiState.Empty

        assertTrue(state is DocumentUiState.Empty)
    }

    @Test
    fun `DocumentUiState Loading represents loading state`() {
        val state: DocumentUiState = DocumentUiState.Loading

        assertTrue(state is DocumentUiState.Loading)
    }

    @Test
    fun `DocumentUiState Error holds message`() {
        val state: DocumentUiState = DocumentUiState.Error("File not found")

        assertTrue(state is DocumentUiState.Error)
        assertEquals("File not found", (state as DocumentUiState.Error).message)
    }

    @Test
    fun `DocumentUiState Success holds document`() {
        val doc = Document("uri", "test.md", "content")
        val state: DocumentUiState = DocumentUiState.Success(doc)

        assertTrue(state is DocumentUiState.Success)
        assertEquals(doc, (state as DocumentUiState.Success).document)
    }

    @Test
    fun `AiQueryState states work correctly`() {
        assertTrue(AiQueryState.Idle is AiQueryState.Idle)
        assertTrue(AiQueryState.Loading is AiQueryState.Loading)
        assertTrue(AiQueryState.Error("msg") is AiQueryState.Error)
        assertTrue(AiQueryState.Success("resp") is AiQueryState.Success)
    }

    @Test
    fun `Data classes implement equality`() {
        val doc1 = Document("uri", "name", "content", 100L)
        val doc2 = Document("uri", "name", "content", 100L)

        assertEquals(doc1, doc2)
        assertEquals(doc1.hashCode(), doc2.hashCode())
    }
}
