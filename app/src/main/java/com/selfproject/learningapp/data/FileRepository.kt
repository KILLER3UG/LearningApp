package com.selfproject.learningapp.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Handles file reading operations via Android Storage Access Framework.
 */
class FileRepository(private val context: Context) {

    /**
     * Reads the content of a file given its URI.
     */
    suspend fun readFileContent(uri: Uri): FileData = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
        val displayName = getDisplayName(uri)
        FileData(content, displayName)
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
}

/**
 * Data class holding file content and display name.
 */
data class FileData(
    val content: String,
    val displayName: String,
)
