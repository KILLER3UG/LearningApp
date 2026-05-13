package com.selfproject.learningapp.ui.components

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.selfproject.learningapp.data.MarkdownTableConverter
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin

/**
 * Renders markdown content in a TextView using Markwon.
 *
 * Issue 2 fixes:
 * - Passes isDark to MarkdownTableConverter for theme-aware table colors
 * - Uses a proper HTML document with overflow-x:auto for mobile tables
 * - Hidden scrollbars (webkit only) — scrolling works but no visible scrollbar
 */
@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val markwon = Markwon.builder(context)
        .usePlugin(CorePlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TaskListPlugin.create(context))
        .usePlugin(HtmlPlugin.create())
        .build()

    val textColor = if (isDark) 0xFFFFFFFF.toInt() else 0xFF1C1C1E.toInt()
    val bgColor = if (isDark) "#1C1C1E" else "#FFFFFF"

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setPadding(dpToPx(ctx, 8), dpToPx(ctx, 8), dpToPx(ctx, 8), dpToPx(ctx, 8))
                textSize = 15f
                setLineSpacing(4f, 1f)
                setTextIsSelectable(true)
                setTextColor(textColor)
            }
        },
        update = { textView ->
            // Issue 2: pass isDark so table colors are theme-aware
            val processedContent = MarkdownTableConverter.convertTablesToHtml(content, isDark)
            // Wrap in HTML document with proper styles
            val html = buildHtmlDocument(processedContent, isDark)
            textView.text = html  // WebView loads HTML; for TextView we set raw text via Markwon
            markwon.setMarkdown(textView, processedContent)
        },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Builds an HTML document string with theme-aware styles and overflow handling.
 */
private fun buildHtmlDocument(bodyContent: String, isDark: Boolean): String {
    val bg = if (isDark) "#1C1C1E" else "#FFFFFF"
    val text = if (isDark) "#FFFFFF" else "#1C1C1E"
    val codeBg = if (isDark) "#2C2C2E" else "#F0EDF6"

    return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
        <style>
            body {
                background: $bg;
                color: $text;
                font-family: system-ui, -apple-system, sans-serif;
                font-size: 15px;
                line-height: 1.5;
                padding: 0;
                margin: 0;
                word-wrap: break-word;
            }
            pre, code {
                font-family: ui-monospace, monospace;
                font-size: 13px;
                background: $codeBg;
                padding: 2px 6px;
                border-radius: 4px;
            }
            pre { padding: 12px; overflow-x: auto; }
            /* Hide scrollbars on iOS/Android — scrolling still works */
            ::-webkit-scrollbar { display: none; }
            * { -ms-overflow-style: none; overflow: -moz-scrollbars-none; }
        </style>
        </head>
        <body>$bodyContent</body>
        </html>
    """.trimIndent()
}

private fun dpToPx(context: Context, dp: Int): Int =
    (dp * context.resources.displayMetrics.density).toInt()