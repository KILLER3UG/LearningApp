package com.selfproject.learningapp.data.fileparser

import android.net.Uri

/**
 * Result of parsing a file.
 */
sealed class FileParseResult {
    data class Success(
        val text: String,
        val metadata: DocumentMetadata = DocumentMetadata()
    ) : FileParseResult()

    data class ImageFile(
        val uri: android.net.Uri,
        val metadata: DocumentMetadata = DocumentMetadata()
    ) : FileParseResult()

    data class Error(val message: String) : FileParseResult()
}

/**
 * Metadata extracted from a document.
 */
data class DocumentMetadata(
    val title: String? = null,
    val author: String? = null,
    val pageCount: Int? = null,
    val sourceFileName: String? = null,
    val fileType: FileType = FileType.UNKNOWN
)

/**
 * Interface for file parsers. Each parser handles one FileType.
 */
interface FileParser {
    val supportedType: FileType
    suspend fun parse(uri: android.net.Uri, context: android.content.Context): FileParseResult
}