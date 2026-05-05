package com.selfproject.learningapp.data

/**
 * Chunks document content into overlapping sections for full-coverage AI processing.
 */
object DocumentChunker {

    private const val CHUNK_SIZE = 8000
    private const val OVERLAP = 1000

    /**
     * Splits content into chunks that fit within API limits.
     */
    fun chunkContent(content: String): List<String> {
        if (content.length <= CHUNK_SIZE) return listOf(content)

        val chunks = mutableListOf<String>()
        var start = 0

        while (start < content.length) {
            val end = minOf(start + CHUNK_SIZE, content.length)
            val chunk = content.substring(start, end)

            // Try to break at paragraph boundary
            val breakPoint = if (end < content.length) {
                val lastDoubleNewline = chunk.lastIndexOf("\n\n")
                if (lastDoubleNewline > CHUNK_SIZE / 2) lastDoubleNewline else chunk.length
            } else {
                chunk.length
            }

            chunks.add(chunk.substring(0, breakPoint))
            start = start + breakPoint - OVERLAP
            if (start <= 0) start = end
        }

        return chunks
    }
}
