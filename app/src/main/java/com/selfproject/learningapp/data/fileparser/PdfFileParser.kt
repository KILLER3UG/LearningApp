package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Parses PDF files using Android's built-in PdfRenderer.
 * Renders pages to bitmaps and applies ML Kit OCR.
 */
class PdfFileParser : FileParser {
    override val supportedType = FileType.PDF

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        val pageCount = renderer.pageCount
                        val pagesToScan = minOf(pageCount, MAX_OCR_PAGES)
                        val allText = StringBuilder()

                        for (i in 0 until pagesToScan) {
                            renderer.openPage(i).use { page ->
                                allText.appendLine("--- Page ${i + 1} of $pageCount ---")
                                val scale = calculateRenderScale(page.width, page.height)
                                val bitmap = Bitmap.createBitmap(
                                    (page.width * scale).toInt().coerceAtLeast(1),
                                    (page.height * scale).toInt().coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888
                                )
                                bitmap.eraseColor(Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                                val pageText = recognizeText(bitmap).trim()
                                if (pageText.isNotBlank()) {
                                    allText.appendLine(pageText)
                                } else {
                                    allText.appendLine("(No text recognized on this page.)")
                                }
                                allText.appendLine()
                                bitmap.recycle()
                            }
                        }

                        if (pageCount > pagesToScan) {
                            allText.appendLine("(Only the first $pagesToScan pages were scanned for on-device OCR.)")
                        }

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

    private fun calculateRenderScale(width: Int, height: Int): Float {
        val largestSide = maxOf(width, height).coerceAtLeast(1)
        val scale = MAX_RENDER_SIDE.toFloat() / largestSide.toFloat()
        return scale.coerceIn(1f, 2f)
    }

    private suspend fun recognizeText(bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (continuation.isActive) continuation.resume(visionText.text)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume("")
                }
        }

    private companion object {
        private const val MAX_OCR_PAGES = 25
        private const val MAX_RENDER_SIDE = 1600
    }
}
