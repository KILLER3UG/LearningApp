package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.nio.charset.StandardCharsets

/**
 * Reads plain text / markdown / any text file as UTF-8.
 */
class PlainTextFileParser : FileParser {
    override val supportedType = FileType.PLAIN_TEXT

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(stream.reader(StandardCharsets.UTF_8)).use { reader ->
                        reader.readText()
                    }
                } ?: return@withContext FileParseResult.Error("Could not open file")

                FileParseResult.Success(
                    text = content,
                    metadata = DocumentMetadata(
                        sourceFileName = uri.lastPathSegment,
                        fileType = FileType.PLAIN_TEXT
                    )
                )
            } catch (e: Exception) {
                FileParseResult.Error("Text read failed: ${e.message}")
            }
        }
}