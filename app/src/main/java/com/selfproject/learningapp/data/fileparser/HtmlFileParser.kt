package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Parses HTML files using Jsoup.
 */
class HtmlFileParser : FileParser {
    override val supportedType = FileType.HTML

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                val html: String = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                } ?: return@withContext FileParseResult.Error("Could not open stream")

                val doc: Document = Jsoup.parse(html)

                // Remove noise elements
                doc.select("script, style, nav, header, footer, aside, .nav, .menu, .sidebar").remove()

                val title = doc.title().ifBlank { doc.select("title").firstOrNull()?.text() }
                val bodyText = doc.body().text()

                if (bodyText.isBlank()) {
                    FileParseResult.Error("HTML file appears to have no text content")
                } else {
                    FileParseResult.Success(
                        text = bodyText,
                        metadata = DocumentMetadata(
                            title = title,
                            sourceFileName = uri.lastPathSegment,
                            fileType = FileType.HTML
                        )
                    )
                }
            } catch (e: Exception) {
                FileParseResult.Error("HTML parsing failed: ${e.message}")
            }
        }
}