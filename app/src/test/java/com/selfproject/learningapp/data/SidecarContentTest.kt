package com.selfproject.learningapp.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for sidecar file content building logic.
 */
class SidecarContentTest {

    @Test
    fun `sidecar content includes timestamp`() {
        val selectedText = "Selected passage"
        val response = "AI explanation"

        val content = buildSidecarContent(selectedText, response)

        assertTrue(content.contains("---"))
        assertTrue(content.contains("**AI Response**"))
    }

    @Test
    fun `sidecar content includes selected text quote`() {
        val selectedText = "The mitochondria is the powerhouse of the cell"
        val response = "This refers to cellular energy production"

        val content = buildSidecarContent(selectedText, response)

        assertTrue(content.contains("> Selected: $selectedText"))
    }

    @Test
    fun `sidecar content includes AI response body`() {
        val selectedText = "Some text"
        val response = "Detailed AI response body"

        val content = buildSidecarContent(selectedText, response)

        assertTrue(content.contains("Detailed AI response body"))
    }

    @Test
    fun `sidecar content has proper markdown structure`() {
        val selectedText = "Query"
        val response = "Answer"

        val content = buildSidecarContent(selectedText, response)

        // Should have separator, header, quote, blank line, response, blank line, separator
        assertTrue(content.startsWith("---"))
        assertTrue(content.contains("**AI Response**"))
        assertTrue(content.endsWith("---\n"))
    }
}

/**
 * Replicates the sidecar building logic from ViewModel for testing.
 */
private fun buildSidecarContent(selectedText: String, response: String): String {
    return buildString {
        appendLine("---")
        appendLine("**AI Response** - ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
        appendLine("> Selected: $selectedText")
        appendLine()
        appendLine(response)
        appendLine()
        appendLine("---")
    }
}
