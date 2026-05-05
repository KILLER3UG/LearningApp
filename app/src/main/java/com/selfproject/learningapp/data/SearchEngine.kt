package com.selfproject.learningapp.data

/**
 * Handles in-document text search and highlighting positions.
 */
object SearchEngine {

    data class SearchResult(
        val text: String,
        val startIndex: Int,
        val endIndex: Int,
    )

    /**
     * Searches for a query in the document content (case-insensitive).
     */
    fun search(content: String, query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val results = mutableListOf<SearchResult>()
        val lowerContent = content.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0

        while (true) {
            val found = lowerContent.indexOf(lowerQuery, startIndex)
            if (found == -1) break

            results.add(
                SearchResult(
                    text = content.substring(found, found + query.length),
                    startIndex = found,
                    endIndex = found + query.length,
                )
            )
            startIndex = found + query.length
        }

        return results
    }

    /**
     * Gets a snippet around a position with context.
     */
    fun getSnippet(
        content: String,
        position: Int,
        contextChars: Int = 100
    ): SnippetResult {
        val start = maxOf(0, position - contextChars)
        val end = minOf(content.length, position + contextChars)
        val snippet = content.substring(start, end)
        val isTruncatedStart = start > 0
        val isTruncatedEnd = end < content.length

        return SnippetResult(snippet, isTruncatedStart, isTruncatedEnd)
    }

    data class SnippetResult(
        val text: String,
        val isTruncatedStart: Boolean,
        val isTruncatedEnd: Boolean,
    )
}
