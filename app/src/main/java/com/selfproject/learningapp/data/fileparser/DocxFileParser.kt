package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument

/**
 * Parses .docx Word documents using Apache POI.
 */
class DocxFileParser : FileParser {
    override val supportedType = FileType.DOCX

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    XWPFDocument(inputStream).use { doc ->
                        val paragraphs = doc.paragraphs
                        val text = paragraphs.joinToString("\n\n") { it.text }

                        if (text.isBlank()) {
                            FileParseResult.Error("DOCX file appears to be empty")
                        } else {
                            FileParseResult.Success(
                                text = text,
                                metadata = DocumentMetadata(
                                    sourceFileName = uri.lastPathSegment,
                                    fileType = FileType.DOCX
                                )
                            )
                        }
                    }
                } ?: FileParseResult.Error("Could not open stream")
            } catch (e: Exception) {
                FileParseResult.Error("DOCX parsing failed: ${e.message}")
            }
        }
}
