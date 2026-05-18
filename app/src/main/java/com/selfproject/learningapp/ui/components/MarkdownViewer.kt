package com.selfproject.learningapp.ui.components

import android.content.Context
import android.graphics.Typeface
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.selfproject.learningapp.data.MarkdownContentBlock
import com.selfproject.learningapp.data.MarkdownContentParser
import com.selfproject.learningapp.data.MarkdownTable
import com.selfproject.learningapp.data.MarkdownTableAlignment
import com.selfproject.learningapp.model.HighlightRange
import com.selfproject.learningapp.ui.theme.TableStyles
import com.selfproject.learningapp.ui.theme.TableTheme
import com.selfproject.learningapp.ui.theme.tableTheme
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin

internal fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

private fun cleanMd(text: String): String =
    text.replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("\\*(.+?)\\*"), "$1")
        .replace("`", "")
        .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1")

/**
 * Renders markdown document content with study-optimized table support.
 *
 * Issue 2 fixes applied:
 * - Uses tableTheme() for theme-aware colors
 * - Uses TableStyles constants (12dp/16dp padding, 1dp borders)
 * - No zebra striping
 * - horizontalScroll on outer box for mobile overflow-x
 * - Removed maxLines=3 from cells — full content visible
 * - Code block tables protected (render as monospace)
 */
@Composable
fun MarkdownViewer(
    content: String,
    onTextSelected: (String) -> Unit,
    onAskAi: (String) -> Unit,
    onBookmarkText: (String, Int) -> Unit = { _, _ -> },
    highlights: List<HighlightRange> = emptyList(),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scroll = rememberScrollState()
    val theme = tableTheme()
    val mw = remember(ctx) {
        Markwon.builder(ctx)
            .usePlugin(CorePlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(ctx))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()
    }

    val blocks = remember(content) { MarkdownContentParser.parse(content) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scroll)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownContentBlock.Text -> {
                    MarkdownTextBlock(
                        content = block.content,
                        markwon = mw,
                        textColor = theme.textColor,
                        onAskAi = { selected ->
                            onTextSelected(selected)
                            onAskAi(selected)
                        },
                        onBookmark = { selected, position ->
                            onBookmarkText(selected, position)
                        }
                    )
                }
                is MarkdownContentBlock.Table -> {
                    MarkdownTableBlock(table = block.table, theme = theme)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
internal fun MarkdownTextBlock(
    content: String,
    markwon: Markwon,
    textColor: Color,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    verticalPadding: Dp = 20.dp,
    textSize: Float = 16f,
    lineSpacingExtra: Float = 8f,
    onAskAi: ((String) -> Unit)? = null,
    onBookmark: ((String, Int) -> Unit)? = null
) {
    AndroidView(
        factory = { c ->
            TextView(c).apply {
                setTextIsSelectable(true)
                setPadding(
                    horizontalPadding.value.toInt().dpToPx(c),
                    verticalPadding.value.toInt().dpToPx(c),
                    horizontalPadding.value.toInt().dpToPx(c),
                    verticalPadding.value.toInt().dpToPx(c)
                )
                this.textSize = textSize
                setLineSpacing(lineSpacingExtra, 1f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                movementMethod = android.text.method.LinkMovementMethod.getInstance()

                if (onAskAi != null || onBookmark != null) {
                    customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
                        override fun onCreateActionMode(m: android.view.ActionMode?, menu: Menu?): Boolean {
                            if (onAskAi != null) {
                                menu?.add(0, 100, 0, "Ask AI")?.setIcon(android.R.drawable.ic_menu_search)
                                    ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                            }
                            if (onBookmark != null) {
                                menu?.add(0, 101, 1, "Bookmark")?.setIcon(android.R.drawable.star_big_on)
                                    ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                            }
                            return true
                        }

                        override fun onPrepareActionMode(m: android.view.ActionMode?, menu: Menu?): Boolean = false

                        override fun onActionItemClicked(m: android.view.ActionMode?, item: MenuItem?): Boolean {
                            val selected = if (selectionStart >= 0 && selectionEnd > selectionStart) {
                                text.subSequence(selectionStart, selectionEnd).toString()
                            } else {
                                ""
                            }
                            when (item?.itemId) {
                                100 -> {
                                    if (selected.isNotBlank()) onAskAi?.invoke(selected)
                                    m?.finish()
                                    return true
                                }
                                101 -> {
                                    if (selected.isNotBlank()) onBookmark?.invoke(selected, selectionStart)
                                    m?.finish()
                                    return true
                                }
                            }
                            return false
                        }

                        override fun onDestroyActionMode(m: android.view.ActionMode?) {}
                    }
                }
            }
        },
        update = { tv ->
            tv.setTextColor(textColor.toArgb())
            markwon.setMarkdown(tv, content)
        },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Renders a table using Compose Row/Column with:
 * - Theme-aware colors (no zebra striping)
 * - horizontalScroll for mobile overflow
 * - 12dp/16dp cell padding, 1dp hairline borders
 * - Header: distinct background + semibold text
 */
@Composable
internal fun MarkdownTableBlock(
    table: MarkdownTable,
    theme: TableTheme,
    modifier: Modifier = Modifier
) {
    val borderColor = theme.cellBorder
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        val naturalWidths = remember(table) { tableColumnWidths(table) }
        val naturalWidth = naturalWidths.sumDp()
        val availableWidth = if (maxWidth > 24.dp) maxWidth - 24.dp else maxWidth
        val extraPerColumn = if (naturalWidth < availableWidth && table.columnCount > 0) {
            (availableWidth - naturalWidth) / table.columnCount.toFloat()
        } else {
            0.dp
        }
        val columnWidths = naturalWidths.map { it + extraPerColumn }
        val tableWidth = columnWidths.sumDp()

        SelectionContainer {
            Box(modifier = Modifier.horizontalScroll(scrollState)) {
                Column(
                    modifier = Modifier
                        .width(tableWidth)
                        .border(TableStyles.borderWidth, borderColor, RoundedCornerShape(6.dp))
                ) {
                    Row {
                        table.headers.forEachIndexed { columnIndex, header ->
                            TableCell(
                                text = header,
                                width = columnWidths[columnIndex],
                                background = theme.headerBackground,
                                borderColor = borderColor,
                                textColor = theme.textColor,
                                fontWeight = TableStyles.headerTextWeight,
                                alignment = table.alignments.getOrNull(columnIndex) ?: MarkdownTableAlignment.Start
                            )
                        }
                    }

                    table.rows.forEach { row ->
                        Row {
                            for (columnIndex in 0 until table.columnCount) {
                                TableCell(
                                    text = row.getOrNull(columnIndex) ?: "",
                                    width = columnWidths[columnIndex],
                                    background = Color.Transparent,
                                    borderColor = borderColor,
                                    textColor = theme.textColor,
                                    alignment = table.alignments.getOrNull(columnIndex)
                                        ?: MarkdownTableAlignment.Start
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: Dp,
    background: Color,
    borderColor: Color,
    textColor: Color,
    alignment: MarkdownTableAlignment,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Box(
        modifier = Modifier
            .width(width)
            .defaultMinSize(minHeight = 44.dp)
            .background(background)
            .border(TableStyles.borderWidth, borderColor)
            .padding(
                horizontal = TableStyles.cellPaddingHorizontal,
                vertical = TableStyles.cellPaddingVertical
            ),
        contentAlignment = alignment.toComposeAlignment()
    ) {
        val codeBackground = MaterialTheme.colorScheme.surfaceVariant
        val inlineText = remember(text, codeBackground) {
            markdownInlineText(text, codeBackground = codeBackground)
        }
        Text(
            text = inlineText,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = fontWeight),
            color = textColor,
            textAlign = alignment.toTextAlign(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun markdownInlineText(text: String, codeBackground: Color): AnnotatedString {
    return buildAnnotatedString {
        var i = 0

        fun appendStyledUntil(
            marker: String,
            start: Int,
            style: SpanStyle
        ): Int? {
            val end = findUnescaped(text, marker, start + marker.length)
            if (end == -1) return null
            withStyle(style) {
                append(text.substring(start + marker.length, end))
            }
            return end + marker.length
        }

        while (i < text.length) {
            when {
                text[i] == '\\' && i + 1 < text.length -> {
                    append(text[i + 1])
                    i += 2
                }
                text.startsWith("**", i) -> {
                    val next = appendStyledUntil(
                        marker = "**",
                        start = i,
                        style = SpanStyle(fontWeight = FontWeight.SemiBold)
                    )
                    if (next != null) i = next else append(text[i++])
                }
                text.startsWith("__", i) -> {
                    val next = appendStyledUntil(
                        marker = "__",
                        start = i,
                        style = SpanStyle(fontWeight = FontWeight.SemiBold)
                    )
                    if (next != null) i = next else append(text[i++])
                }
                text[i] == '`' -> {
                    val end = findUnescaped(text, "`", i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                background = codeBackground
                            )
                        ) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i++])
                    }
                }
                text[i] == '[' -> {
                    val close = findUnescaped(text, "]", i + 1)
                    val openParen = if (close != -1 && close + 1 < text.length && text[close + 1] == '(') close + 1 else -1
                    val closeParen = if (openParen != -1) findUnescaped(text, ")", openParen + 1) else -1
                    if (closeParen != -1) {
                        append(text.substring(i + 1, close))
                        i = closeParen + 1
                    } else {
                        append(text[i++])
                    }
                }
                text[i] == '*' -> {
                    val next = appendStyledUntil(
                        marker = "*",
                        start = i,
                        style = SpanStyle(fontStyle = FontStyle.Italic)
                    )
                    if (next != null) i = next else append(text[i++])
                }
                text[i] == '_' -> {
                    val next = appendStyledUntil(
                        marker = "_",
                        start = i,
                        style = SpanStyle(fontStyle = FontStyle.Italic)
                    )
                    if (next != null) i = next else append(text[i++])
                }
                else -> append(text[i++])
            }
        }
    }
}

private fun findUnescaped(text: String, needle: String, startIndex: Int): Int {
    var index = text.indexOf(needle, startIndex)
    while (index != -1) {
        if (!isEscaped(text, index)) return index
        index = text.indexOf(needle, index + needle.length)
    }
    return -1
}

private fun isEscaped(text: String, index: Int): Boolean {
    var slashCount = 0
    var i = index - 1
    while (i >= 0 && text[i] == '\\') {
        slashCount++
        i--
    }
    return slashCount % 2 == 1
}

private fun tableColumnWidths(table: MarkdownTable): List<Dp> {
    return List(table.columnCount) { columnIndex ->
        val maxLength = buildList {
            add(table.headers.getOrNull(columnIndex).orEmpty())
            table.rows.forEach { row -> add(row.getOrNull(columnIndex).orEmpty()) }
        }.maxOfOrNull { plainCellLength(it) } ?: 0

        when {
            maxLength <= 8 -> 96.dp
            maxLength <= 18 -> 132.dp
            maxLength <= 32 -> 184.dp
            maxLength <= 56 -> 232.dp
            else -> 280.dp
        }
    }
}

private fun plainCellLength(text: String): Int {
    return cleanMd(text)
        .replace(Regex("\\s+"), " ")
        .trim()
        .length
}

private fun List<Dp>.sumDp(): Dp = fold(0.dp) { total, width -> total + width }

private fun MarkdownTableAlignment.toComposeAlignment(): Alignment {
    return when (this) {
        MarkdownTableAlignment.Start -> Alignment.CenterStart
        MarkdownTableAlignment.Center -> Alignment.Center
        MarkdownTableAlignment.End -> Alignment.CenterEnd
    }
}

private fun MarkdownTableAlignment.toTextAlign(): TextAlign {
    return when (this) {
        MarkdownTableAlignment.Start -> TextAlign.Start
        MarkdownTableAlignment.Center -> TextAlign.Center
        MarkdownTableAlignment.End -> TextAlign.End
    }
}
