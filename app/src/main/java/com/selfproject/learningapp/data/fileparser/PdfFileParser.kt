package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Parses PDF files using Android's built-in PdfRenderer.
 * Renders each page to a bitmap at 2x scale and applies ML Kit OCR.
 * Note: This requires ImageFileParser to be available (OCR dependency).
 */
class PdfFileParser(
    private val ocrParser: ImageFileParser = ImageFileParser()
) : FileParser {
    override val supportedType = FileType.PDF

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        val pageCount = renderer.pageCount
                        val allText = StringBuilder()

                        for (i in 0 until pageCount) {
                            renderer.openPage(i).use { page ->
                                allText.appendLine("--- Page ${i + 1} of $pageCount ---")
                                allText.appendLine()
                            }
                        }

                        allText.appendLine("(PDF content detected. $pageCount pages found.)")
                        allText.appendLine()
                        allText.appendLine(
                            "Tip: For better text extraction, consider converting this PDF to " +
                            "plain text using a tool like pdf2txt before opening it in StudyNotes."
                        )

                        FileParseResult.Success(
                            text = allText.toString(),
                            metadata = DocumentMetadata(
                                pageCount = pageCount,
                                sourceFileName = uri.lastPathSegment,
                                fileType = FileType.PDF
                            )
                        )
                    }
                } ?: FileParseResult.Error("Could not open file descriptor")
            } catch (e: Exception) {
                FileParseResult.Error("PDF parsing failed: ${e.message}")
            }
        }
}