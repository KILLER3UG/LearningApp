package com.selfproject.learningapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.MarkdownContentBlock
import com.selfproject.learningapp.data.MarkdownContentParser
import com.selfproject.learningapp.ui.theme.tableTheme
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin

/**
 * Renders markdown content for AI responses.
 * Text still uses Markwon; tables use the same Compose table renderer as documents.
 */
@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = tableTheme()
    val blocks = remember(content) { MarkdownContentParser.parse(content) }
    val markwon = remember(context) {
        Markwon.builder(context)
            .usePlugin(CorePlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownContentBlock.Text -> {
                    MarkdownTextBlock(
                        content = block.content,
                        markwon = markwon,
                        textColor = theme.textColor,
                        horizontalPadding = 8.dp,
                        verticalPadding = 6.dp,
                        textSize = 15f,
                        lineSpacingExtra = 4f
                    )
                }
                is MarkdownContentBlock.Table -> {
                    MarkdownTableBlock(table = block.table, theme = theme)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
