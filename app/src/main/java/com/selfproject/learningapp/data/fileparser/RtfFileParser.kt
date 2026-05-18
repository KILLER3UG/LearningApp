package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts readable text from simple RTF files.
 */
class RtfFileParser : FileParser {
    override val supportedType = FileType.RTF

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                } ?: return@withContext FileParseResult.Error("Could not open RTF stream")

                val text = raw
                    .replace(Regex("""\\'[0-9a-fA-F]{2}""")) { match ->
                        match.value.removePrefix("\\'").toInt(16).toChar().toString()
                    }
                    .replace(Regex("""\\par[d]?"""), "\n")
                    .replace(Regex("""\\tab"""), "\t")
                    .replace(Regex("""\\[a-zA-Z]+\d* ?"""), "")
                    .replace(Regex("""[{}]"""), "")
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .joinToString("\n")

                if (text.isBlank()) {
                    FileParseResult.Error("RTF file appears to have no extractable text")
                } else {
                    FileParseResult.Success(
                        text = text,
                        metadata = DocumentMetadata(
                            sourceFileName = uri.lastPathSegment,
                            fileType = FileType.RTF
                        )
                    )
                }
            } catch (e: Exception) {
                FileParseResult.Error("RTF parsing failed: ${e.message}")
            }
        }
}
