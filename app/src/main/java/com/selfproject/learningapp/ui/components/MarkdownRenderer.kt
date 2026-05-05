package com.selfproject.learningapp.ui.components

import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.compose.foundation.layout.padding
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
 */
@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.toArgb() < 0xFF800000.toInt()

    val markwon = Markwon.builder(context)
        .usePlugin(CorePlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TaskListPlugin.create(context))
        .usePlugin(HtmlPlugin.create())
        .build()

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setPadding(8.dpToPx(ctx), 8.dpToPx(ctx), 8.dpToPx(ctx), 8.dpToPx(ctx))
                textSize = 15f
                setLineSpacing(4f, 1f)
                setTextIsSelectable(true)
                setTextColor(if (isDark) 0xFFE6E1E5.toInt() else 0xFF1C1B1F.toInt())
            }
        },
        update = { textView ->
            val processedContent = MarkdownTableConverter.convertTablesToHtml(content)
            markwon.setMarkdown(textView, processedContent)
        },
        modifier = modifier
    )
}
