package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Extracts slide text from Office Open XML PowerPoint files.
 */
class PptxFileParser : FileParser {
    override val supportedType = FileType.PPTX

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                val slides = mutableListOf<Pair<Int, String>>()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            val name = entry.name.lowercase()
                            if (name.startsWith("ppt/slides/slide") && name.endsWith(".xml")) {
                                val slideNumber = Regex("""slide(\d+)\.xml""")
                                    .find(name)
                                    ?.groupValues
                                    ?.getOrNull(1)
                                    ?.toIntOrNull()
                                    ?: (slides.size + 1)
                                val text = extractSlideText(zip.readBytes())
                                if (text.isNotBlank()) {
                                    slides.add(slideNumber to text)
                                }
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                } ?: return@withContext FileParseResult.Error("Could not open PowerPoint stream")

                if (slides.isEmpty()) {
                    FileParseResult.Error("PowerPoint file appears to have no extractable slide text")
                } else {
                    val text = slides
                        .sortedBy { it.first }
                        .joinToString("\n\n") { (slideNumber, slideText) ->
                            "## Slide $slideNumber\n\n$slideText"
                        }

                    FileParseResult.Success(
                        text = text,
                        metadata = DocumentMetadata(
                            pageCount = slides.size,
                            sourceFileName = uri.lastPathSegment,
                            fileType = FileType.PPTX
                        )
                    )
                }
            } catch (e: Exception) {
                FileParseResult.Error("PowerPoint parsing failed: ${e.message}")
            }
        }

    private fun extractSlideText(bytes: ByteArray): String {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
            runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
            runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        }
        val document = factory.newDocumentBuilder().parse(ByteArrayInputStream(bytes))
        val textNodes = document.getElementsByTagNameNS("*", "t")

        return buildString {
            for (index in 0 until textNodes.length) {
                val value = textNodes.item(index)?.textContent?.trim().orEmpty()
                if (value.isNotBlank()) {
                    appendLine(value)
                }
            }
        }.trim()
    }
}
