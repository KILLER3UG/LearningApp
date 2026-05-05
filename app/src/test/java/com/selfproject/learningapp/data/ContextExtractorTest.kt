package com.selfproject.learningapp.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ContextExtractor — pure Kotlin, no Android dependencies.
 */
class ContextExtractorTest {

    @Test
    fun `extractSurroundingContext returns selected text when not found in content`() {
        val selectedText = "not found"
        val content = "This is some content without the text"

        val result = ContextExtractor.extractSurroundingContext(selectedText, content)

        assertEquals("not found", result)
    }

    @Test
    fun `extractSurroundingContext includes 2 paragraphs before selection`() {
        val content = """
            Paragraph one.

            Paragraph two.

            Paragraph three with SELECTED TEXT here.

            After paragraph.
        """.trimIndent()

        val result = ContextExtractor.extractSurroundingContext("SELECTED TEXT", content)

        assertTrue(result.contains("Paragraph two"))
        assertTrue(result.contains("Paragraph three with"))
    }

    @Test
    fun `extractSurroundingContext includes paragraphs after selection`() {
        val content = "Before.\n\nText with SELECTED here.\n\nAfter one.\n\nAfter two.\n\nAfter three."

        val result = ContextExtractor.extractSurroundingContext("SELECTED", content)

        // "SELECTED" is in the middle of a paragraph, so afterText starts with " here."
        // Then "After one." and "After two." are the next 2 paragraphs
        assertTrue("Should contain 'After one'. Actual: $result", result.contains("After one"))
    }

    @Test
    fun `extractSurroundingContext handles full paragraph selection`() {
        val content = "Before.\n\nFull paragraph SELECTED.\n\nAfter one.\n\nAfter two."

        // Select the entire paragraph text (without trailing period to test boundary)
        val result = ContextExtractor.extractSurroundingContext("Full paragraph SELECTED.", content)

        // Clean paragraph boundary: after text is "After one.\n\nAfter two."
        assertTrue("Should contain 'After one'. Actual: $result", result.contains("After one"))
        assertTrue("Should contain 'After two'. Actual: $result", result.contains("After two"))
    }

    @Test
    fun `extractSurroundingContext marks selected text with delimiters`() {
        val content = "Before\n\nSelected text here\n\nAfter"

        val result = ContextExtractor.extractSurroundingContext("Selected text here", content)

        assertTrue(result.contains("[SELECTED TEXT]"))
        assertTrue(result.contains("[/SELECTED TEXT]"))
    }

    @Test
    fun `extractSurroundingContext handles selection at start of document`() {
        // Use a selection that covers the full first paragraph
        val content = "First paragraph.\n\nSecond paragraph.\n\nThird paragraph."

        val result = ContextExtractor.extractSurroundingContext("First paragraph.", content)

        // After "First paragraph." the remaining text splits into clean paragraphs
        assertTrue("Should contain second paragraph. Actual: $result", result.contains("Second paragraph"))
        assertTrue("Should contain third paragraph. Actual: $result", result.contains("Third paragraph"))
    }

    @Test
    fun `extractSurroundingContext handles selection at end of document`() {
        val content = "First paragraph.\n\nSecond paragraph.\n\nLast paragraph with END."

        val result = ContextExtractor.extractSurroundingContext("END", content)

        assertTrue(result.contains("Second paragraph"))
        assertTrue(result.contains("Last paragraph with"))
    }

    @Test
    fun `extractSurroundingContext handles single paragraph document`() {
        val content = "Only one paragraph with TEXT."

        val result = ContextExtractor.extractSurroundingContext("TEXT", content)

        assertTrue(result.contains("TEXT"))
        assertTrue(result.contains("[SELECTED TEXT]"))
    }

    @Test
    fun `detectHeadings finds level 1 headings`() {
        val content = """
            # Heading One
            Some text

            # Heading Two
            More text
        """.trimIndent()

        val headings = ContextExtractor.detectHeadings(content)

        assertEquals(2, headings.size)
        assertEquals("Heading One", headings[0].first)
        assertEquals("Heading Two", headings[1].first)
    }

    @Test
    fun `detectHeadings finds level 2 headings`() {
        val content = """
            ## Subsection A
            ## Subsection B
        """.trimIndent()

        val headings = ContextExtractor.detectHeadings(content)

        assertEquals(2, headings.size)
        assertEquals("Subsection A", headings[0].first)
        assertEquals("Subsection B", headings[1].first)
    }

    @Test
    fun `detectHeadings returns empty for no headings`() {
        val content = "Just regular text\n\nNo headings here."

        val headings = ContextExtractor.detectHeadings(content)

        assertTrue(headings.isEmpty())
    }

    @Test
    fun `detectHeadings ignores lines that are not headings`() {
        val content = """
            Not a heading
            ## Real Heading
            ###Also not a heading (no space after #)
            # Valid
        """.trimIndent()

        val headings = ContextExtractor.detectHeadings(content)

        assertEquals(2, headings.size)
        assertEquals("Real Heading", headings[0].first)
        assertEquals("Valid", headings[1].first)
    }

    @Test
    fun `sanitizeFileName removes special characters`() {
        val name = "My Document (2024) [Notes]!.md"

        val result = ContextExtractor.sanitizeFileName(name)

        // Each special char is replaced with underscore individually
        assertEquals("My_Document__2024___Notes__.md", result)
    }

    @Test
    fun `sanitizeFileName truncates to 100 characters`() {
        val name = "A".repeat(150) + ".md"

        val result = ContextExtractor.sanitizeFileName(name)

        assertEquals(100, result.length)
    }

    @Test
    fun `sanitizeFileName preserves valid characters`() {
        val name = "valid-file_name.2024.md"

        val result = ContextExtractor.sanitizeFileName(name)

        assertEquals("valid-file_name.2024.md", result)
    }
}
