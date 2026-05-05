package com.selfproject.learningapp.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SearchEngine — pure Kotlin, no Android dependencies.
 */
class SearchEngineTest {

    @Test
    fun `search finds all occurrences case-insensitive`() {
        val content = "Hello world. HELLO again. hello there."
        val results = SearchEngine.search(content, "hello")

        assertEquals(3, results.size)
    }

    @Test
    fun `search returns empty for blank query`() {
        val content = "Some text here"

        assertTrue(SearchEngine.search(content, "").isEmpty())
        assertTrue(SearchEngine.search(content, "   ").isEmpty())
    }

    @Test
    fun `search returns empty when not found`() {
        val content = "Hello world"
        val results = SearchEngine.search(content, "xyz")

        assertTrue(results.isEmpty())
    }

    @Test
    fun `search returns correct positions`() {
        val content = "ABC def ABC ghi"
        val results = SearchEngine.search(content, "ABC")

        assertEquals(0, results[0].startIndex)
        assertEquals(3, results[0].endIndex)
        assertEquals(8, results[1].startIndex)
        assertEquals(11, results[1].endIndex)
    }

    @Test
    fun `search preserves original case in result text`() {
        val content = "Hello HELLO hello"
        val results = SearchEngine.search(content, "hello")

        assertEquals("Hello", results[0].text)
        assertEquals("HELLO", results[1].text)
        assertEquals("hello", results[2].text)
    }

    @Test
    fun `getSnippet returns text around position`() {
        val content = "A".repeat(200) + "TARGET" + "B".repeat(200)
        val position = 200

        val snippet = SearchEngine.getSnippet(content, position, contextChars = 10)

        assertTrue(snippet.text.contains("TARGET"))
        assertTrue(snippet.isTruncatedStart)
        assertTrue(snippet.isTruncatedEnd)
    }

    @Test
    fun `getSnippet handles start of document`() {
        val content = "Start of document text"

        val snippet = SearchEngine.getSnippet(content, 5, contextChars = 10)

        assertFalse(snippet.isTruncatedStart)
        assertTrue(snippet.isTruncatedEnd)
    }

    @Test
    fun `getSnippet handles end of document`() {
        val content = "Text at the end of document"

        val snippet = SearchEngine.getSnippet(content, content.length - 3, contextChars = 5)

        assertTrue(snippet.isTruncatedStart)
        assertFalse(snippet.isTruncatedEnd)
    }
}
