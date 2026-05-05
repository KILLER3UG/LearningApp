package com.selfproject.learningapp.data

/**
 * Pure Kotlin utility for extracting surrounding context from document text.
 * Testable without Android dependencies.
 */
object ContextExtractor {

    /**
     * Extracts ±2 paragraphs around the selected text.
     */
    fun extractSurroundingContext(
        selectedText: String,
        fullContent: String
    ): String {
        val startIndex = fullContent.indexOf(selectedText)
        if (startIndex == -1) return selectedText

        val beforeText = fullContent.substring(0, startIndex)
        val afterStartIndex = startIndex + selectedText.length
        val afterText = if (afterStartIndex < fullContent.length) {
            fullContent.substring(afterStartIndex)
        } else ""

        val beforeParagraphs = beforeText.split("\n\n")
            .filter { it.isNotBlank() }
            .takeLast(2)
            .joinToString("\n\n")

        val afterParagraphs = afterText.split("\n\n")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("\n\n")

        return buildString {
            if (beforeParagraphs.isNotEmpty()) {
                appendLine(beforeParagraphs)
                appendLine()
            }
            appendLine("[SELECTED TEXT]")
            appendLine(selectedText)
            appendLine("[/SELECTED TEXT]")
            if (afterParagraphs.isNotEmpty()) {
                appendLine()
                append(afterParagraphs)
            }
        }
    }

    /**
     * Detects headings in markdown content.
     * Returns list of pairs: (heading text, position index).
     */
    fun detectHeadings(content: String): List<Pair<String, Int>> {
        val lines = content.split("\n")
        val headings = mutableListOf<Pair<String, Int>>()
        var currentIndex = 0

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") && trimmed.any { it != '#' } && trimmed.first { it != '#' } == ' ') {
                val headingText = trimmed.trimStart('#').trim()
                headings.add(headingText to currentIndex)
            }
            currentIndex += line.length + 1 // +1 for newline
        }

        return headings
    }

    /**
     * Sanitizes a filename from document name.
     */
    fun sanitizeFileName(name: String): String {
        return name
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(100)
    }
}
