package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Parses .epub e-book files using Jsoup.
 *
 * EPUB files are ZIP archives containing XHTML content files. We:
 * 1. Open as ZipInputStream to read entries
 * 2. Filter for XHTML/HTML content
 * 3. Parse with Jsoup and extract text
 */
class EpubFileParser : FileParser {
    override val supportedType = FileType.EPUB

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                var title: String? = null
                var author: String? = null
                val content = StringBuilder()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            val name = entry.name.lowercase()
                            if (name.endsWith(".xhtml") || name.endsWith(".html") || name.endsWith(".htm")) {
                                val bytes = zip.readBytes()
                                val html = String(bytes, Charsets.UTF_8)

                                // Try to extract title from first <title> or <h1>
                                if (title == null) {
                                    val doc = Jsoup.parse(html)
                                    title = doc.selectFirst("title")?.text()
                                        ?: doc.selectFirst("h1")?.text()
                                }

                                // Parse and extract text
                                val doc = Jsoup.parse(html)
                                val text = doc.body()?.text()?.trim() ?: ""
                                if (text.isNotBlank()) {
                                    content.appendLine(text)
                                    content.appendLine()
                                }
                            }

                            // Also look for container.xml to find rootfile path
                            if (name == "META-INF/container.xml") {
                                val bytes = zip.readBytes()
                                val xml = String(bytes, Charsets.UTF_8)
                                // Extract full-path from container.xml if needed
                                val rootfileMatch = Regex("full-path=\"([^\"]+)\"").find(xml)
                                // rootfileMatch?.groupValues?.get(1) gives path to .opf
                            }

                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                } ?: return@withContext FileParseResult.Error("Could not open EPUB stream")

                val text = content.toString().trim()
                if (text.isBlank()) {
                    FileParseResult.Error("EPUB file appears to have no extractable text")
                } else {
                    FileParseResult.Success(
                        text = text,
                        metadata = DocumentMetadata(
                            title = title,
                            author = author,
                            sourceFileName = uri.lastPathSegment,
                            fileType = FileType.EPUB
                        )
                    )
                }
            } catch (e: Exception) {
                FileParseResult.Error("EPUB parsing failed: ${e.message}")
            }
        }
}
