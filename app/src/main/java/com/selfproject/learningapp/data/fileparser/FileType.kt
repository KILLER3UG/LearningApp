package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Supported file types for universal study-material support.
 * MIME types are authoritative; extensions are a fallback.
 */
enum class FileType(
    val displayName: String,
    val mimeTypes: Set<String>,
    val extensions: Set<String>,
    val iconRes: String,
    val isTextual: Boolean
) {
    PDF(
        "PDF", setOf("application/pdf"),
        setOf("pdf"), "ic_file_pdf", false
    ),
    DOC(
        "Word", setOf("application/msword"),
        setOf("doc"), "ic_file_doc", false
    ),
    DOCX(
        "Word", setOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        setOf("docx"), "ic_file_doc", false
    ),
    PPTX(
        "PowerPoint", setOf(
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-powerpoint"
        ), setOf("pptx", "ppt"), "ic_file_ppt", false
    ),
    EPUB(
        "EPUB", setOf("application/epub+zip"),
        setOf("epub"), "ic_file_epub", true
    ),
    HTML(
        "HTML", setOf("text/html"),
        setOf("html", "htm", "xhtml"), "ic_file_html", true
    ),
    RTF(
        "RTF", setOf("application/rtf", "text/rtf"),
        setOf("rtf"), "ic_file_rtf", true
    ),
    IMAGE(
        "Image", setOf("image/png", "image/jpeg", "image/jpg", "image/webp", "image/gif", "image/heic", "image/heif", "image/bmp"),
        setOf("png", "jpg", "jpeg", "webp", "gif", "heic", "heif", "bmp"), "ic_file_image", false
    ),
    SVG(
        "SVG", setOf("image/svg+xml"),
        setOf("svg"), "ic_file_svg", true
    ),
    MARKDOWN(
        "Markdown", setOf("text/markdown"),
        setOf("md", "markdown"), "ic_file_md", true
    ),
    PLAIN_TEXT(
        "Text", setOf("text/plain"),
        setOf("txt", "text", "log", "yaml", "yml", "xml", "gradle", "properties", "ini"), "ic_file_text", true
    ),
    PY(
        "Python", setOf("text/x-python"),
        setOf("py"), "ic_file_code", true
    ),
    JS(
        "JavaScript", setOf("text/javascript", "application/javascript", "application/x-javascript"),
        setOf("js"), "ic_file_code", true
    ),
    TS(
        "TypeScript", setOf("text/typescript"),
        setOf("ts"), "ic_file_code", true
    ),
    JSX(
        "JSX", setOf("text/jsx"),
        setOf("jsx"), "ic_file_code", true
    ),
    CPP(
        "C++", setOf("text/x-c++src", "text/x-c"),
        setOf("cpp", "cc", "cxx", "c", "h", "hpp"), "ic_file_code", true
    ),
    JAVA(
        "Java", setOf("text/x-java-source"),
        setOf("java"), "ic_file_code", true
    ),
    JSON(
        "JSON", setOf("application/json", "text/json"),
        setOf("json"), "ic_file_json", true
    ),
    CSV(
        "CSV", setOf("text/csv"),
        setOf("csv"), "ic_file_csv", true
    ),
    XLSX(
        "Spreadsheet", setOf(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
        ),
        setOf("xlsx", "xls"), "ic_file_csv", false
    ),
    SQL(
        "SQL", setOf("application/sql", "text/sql"),
        setOf("sql"), "ic_file_code", true
    ),
    MP3(
        "Audio", setOf("audio/mpeg", "audio/mp3"),
        setOf("mp3"), "ic_file_audio", false
    ),
    M4A(
        "Audio", setOf("audio/mp4", "audio/m4a"),
        setOf("m4a"), "ic_file_audio", false
    ),
    UNKNOWN("Unknown", emptySet(), emptySet(), "ic_file_text", true);

    companion object {
        private const val MAX_SIZE_BYTES = 50L * 1024 * 1024  // 50 MB

        fun fromMimeOrExtension(mime: String?, uri: Uri, context: Context): FileType {
            mime?.let { m ->
                entries.find { it != UNKNOWN && it.mimeTypes.contains(m) }?.let { return it }
                if (m.startsWith("text/")) return PLAIN_TEXT
            }

            val ext = resolveDisplayName(uri, context)
                ?.substringAfterLast('.', missingDelimiterValue = "")
                ?.lowercase()
                .orEmpty()
            return entries.find { it != UNKNOWN && it.extensions.contains(ext) } ?: UNKNOWN
        }

        /** Returns null if size is OK, returns error message if too large. */
        fun validateSize(sizeBytes: Long): String? =
            if (sizeBytes > MAX_SIZE_BYTES) "File too large. Try under 50MB." else null

        private fun resolveDisplayName(uri: Uri, context: Context): String? {
            var name: String? = null
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
            }
            return name ?: uri.lastPathSegment
        }
    }
}
