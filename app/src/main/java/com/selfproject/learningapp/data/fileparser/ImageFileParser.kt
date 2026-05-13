package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Image file parser.
 *
 * Strategy:
 * 1. If the AI model supports vision → return ImageFile(uri) to be sent directly to AI.
 *    The AI interprets the image natively (Claude, GPT-4o, Gemini Pro Vision, LLaVA, Qwen2 VL).
 *
 * 2. If the AI model does NOT support vision → fall back to ML Kit OCR.
 *    The caller decides which path to use based on the selected model's capabilities.
 *
 * Call sites should check `supportsVision` on the selected AiModel and pick:
 *   - vision path → send the Uri directly to AI with vision-capable provider
 *   - fallback path → call ImageFileParser.extractText(uri) for OCR text
 */
class ImageFileParser : FileParser {
    override val supportedType = FileType.IMAGE

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Returns an ImageFile result that the caller should route to the appropriate handler:
     * - Vision-capable model: send directly to AI
     * - Non-vision model: call extractText() for OCR fallback
     */
    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                // Return the image URI for vision-capable models to handle directly
                FileParseResult.ImageFile(
                    uri = uri,
                    metadata = DocumentMetadata(
                        sourceFileName = uri.lastPathSegment,
                        fileType = FileType.IMAGE
                    )
                )
            } catch (e: Exception) {
                FileParseResult.Error("Image processing failed: ${e.message}")
            }
        }

    /**
     * Fallback: extract text from an image using ML Kit OCR.
     * Use this when the selected AI model does NOT support vision.
     */
    suspend fun extractText(uri: Uri, context: Context): String =
        withContext(Dispatchers.IO) {
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: throw IllegalStateException("Could not decode image")

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            suspendCancellableCoroutine { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener { e ->
                        continuation.resume("")
                    }
            }
        }
}