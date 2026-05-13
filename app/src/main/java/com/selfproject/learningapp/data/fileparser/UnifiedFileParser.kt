package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri

/**
 * Unified file parser — detects file type and delegates to the appropriate parser.
 * Returns plain text that can be fed into the existing markdown pipeline.
 */
class UnifiedFileParser(
    private val parsers: List<FileParser> = listOf(
        ImageFileParser(),
        DocxFileParser(),
        EpubFileParser(),
        HtmlFileParser(),
        PlainTextFileParser() // also handles Markdown and PDF (basic fallback)
    )
) {
    private val parserMap: Map<FileType, FileParser> by lazy {
        parsers.associateBy { it.supportedType }
    }

    /**
     * Parses a file from a SAF URI and returns plain text.
     * Falls back to PlainTextFileParser for unknown types.
     */
    suspend fun parse(uri: Uri, context: Context): FileParseResult {
        val mime = context.contentResolver.getType(uri)
        val fileType = FileType.fromMimeOrExtension(mime, uri, context)
        val parser = parserMap[fileType] ?: parserMap[FileType.PLAIN_TEXT]
            ?: return FileParseResult.Error("No parser available for file type: ${fileType.displayName}")

        return try {
            parser.parse(uri, context)
        } catch (e: Exception) {
            FileParseResult.Error("Failed to parse ${fileType.displayName}: ${e.message}")
        }
    }

    /**
     * Returns the detected file type without parsing.
     */
    fun detectType(uri: Uri, context: Context): FileType {
        val mime = context.contentResolver.getType(uri)
        return FileType.fromMimeOrExtension(mime, uri, context)
    }
}
