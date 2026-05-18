package com.selfproject.learningapp.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.selfproject.learningapp.data.fileparser.FileParseResult
import com.selfproject.learningapp.data.fileparser.FileType
import com.selfproject.learningapp.data.fileparser.ImageFileParser
import com.selfproject.learningapp.data.fileparser.UnifiedFileParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles file reading operations via Android Storage Access Framework.
 */
class FileRepository(private val context: Context) {
    private val unifiedFileParser = UnifiedFileParser()
    private val imageFileParser = ImageFileParser()

    /**
     * Reads the content of a file given its URI.
     */
    suspend fun readFileContent(uri: Uri): FileData = withContext(Dispatchers.IO) {
        val displayName = getDisplayName(uri)

        when (val result = unifiedFileParser.parse(uri, context)) {
            is FileParseResult.Success -> {
                val title = result.metadata.title?.takeIf { it.isNotBlank() } ?: displayName
                FileData(result.text, title)
            }

            is FileParseResult.ImageFile -> {
                val extractedText = runCatching {
                    imageFileParser.extractText(uri, context).trim()
                }.getOrDefault("")
                val content = if (extractedText.isNotBlank()) {
                    buildString {
                        appendLine("# $displayName")
                        appendLine()
                        appendLine("Extracted text from image:")
                        appendLine()
                        appendLine(extractedText)
                    }
                } else {
                    buildNonTextDocument(displayName, FileType.IMAGE)
                }
                FileData(content, displayName)
            }

            is FileParseResult.Error -> {
                val fileType = unifiedFileParser.detectType(uri, context)
                FileData(buildNonTextDocument(displayName, fileType, result.message), displayName)
            }
        }
    }

    /**
     * Gets the display name of a file from its URI.
     */
    suspend fun getDisplayName(uri: Uri): String = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        var name = uri.lastPathSegment ?: "Unknown"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
        name
    }

    /**
     * Appends content to a sidecar file. Creates the file if it doesn't exist.
     * For SAF URIs, we write to the app's internal storage instead.
     */
    suspend fun appendToSidecarFile(
        documentUri: Uri,
        content: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val sidecarDir = android.content.ContextWrapper(context).getDir(
                "sidecars",
                android.content.Context.MODE_PRIVATE
            )
            val displayName = getDisplayName(documentUri)
            val safeFileName = displayName
                .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .take(100)
            val sidecarFile = java.io.File(sidecarDir, "${safeFileName}.notes.md")

            sidecarFile.appendText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads cached document content from internal storage.
     */
    suspend fun getCachedContent(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = android.content.ContextWrapper(context).getDir(
                "cache",
                android.content.Context.MODE_PRIVATE
            )
            val key = uri.toString().hashCode().toString()
            val cacheFile = java.io.File(cacheDir, key)
            if (cacheFile.exists()) cacheFile.readText() else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Caches document content to internal storage.
     */
    suspend fun cacheContent(uri: Uri, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheDir = android.content.ContextWrapper(context).getDir(
                "cache",
                android.content.Context.MODE_PRIVATE
            )
            val key = uri.toString().hashCode().toString()
            val cacheFile = java.io.File(cacheDir, key)
            cacheFile.writeText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun buildNonTextDocument(
        displayName: String,
        fileType: FileType,
        parseMessage: String? = null
    ): String = buildString {
        appendLine("# $displayName")
        appendLine()
        appendLine("This ${fileType.displayName.lowercase()} file was added as a study source.")
        appendLine()
        appendLine("StudyNotes can open the file, but it could not extract readable text from it on this device yet.")
        parseMessage?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine("Parser note: $it")
        }
        appendLine()
        appendLine("You can still keep it in the source list, or use a text-based export of the file for richer AI summaries, study guides, quizzes, and flashcards.")
    }
}

/**
 * Data class holding file content and display name.
 */
data class FileData(
    val content: String,
    val displayName: String,
)
