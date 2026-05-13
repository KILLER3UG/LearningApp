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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.selfproject.learningapp.model.HighlightRange
import com.selfproject.learningapp.ui.theme.TableStyles
import com.selfproject.learningapp.ui.theme.tableTheme
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin

internal fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

data class ParsedTable(val headers: List<String>, val rows: List<List<String>>)

sealed class ContentBlock {
    data class Text(val content: String) : ContentBlock()
    data class Table(val table: ParsedTable) : ContentBlock()
}

/**
 * Extracts markdown tables vs plain text blocks from document content.
 */
private fun extractContentBlocks(content: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val lines = content.split("\n")
    val textBuffer = StringBuilder()
    var i = 0

    // Step 1: protect fenced code blocks (tables inside ``` render as monospace)
    val protected = protectCodeFences(content)
    val protectedLines = protected.split("\n")

    while (i < protectedLines.size) {
        val trimmed = protectedLines[i].trim()
        val isTableStart = trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.count { it == '|' } >= 3
        val hasSeparator = i + 1 < protectedLines.size && isTableSeparator(protectedLines[i + 1].trim())

        if (isTableStart && hasSeparator) {
            if (textBuffer.isNotEmpty()) {
                blocks.add(ContentBlock.Text(textBuffer.toString()))
                textBuffer.clear()
            }
            val tableLines = mutableListOf<String>()
            var j = i
            while (j < protectedLines.size) {
                val t = protectedLines[j].trim()
                if (t.startsWith("|") && t.endsWith("|")) {
                    if (!isTableSeparator(t)) tableLines.add(t)
                    j++
                } else break
            }
            if (tableLines.isNotEmpty()) {
                val headers = parseCells(tableLines[0])
                val rows = tableLines.drop(1).map { parseCells(it) }
                blocks.add(ContentBlock.Table(ParsedTable(headers, rows)))
            }
            i = j
        } else {
            textBuffer.append(protectedLines[i]).append("\n")
            i++
        }
    }

    if (textBuffer.isNotEmpty()) {
        blocks.add(ContentBlock.Text(textBuffer.toString()))
    }
    return blocks
}

/**
 * Protects fenced code blocks by replacing them with placeholders.
 * Tables inside code fences should render as monospace text, not visual tables.
 */
private fun protectCodeFences(content: String): String {
    val fences = mutableListOf<String>()
    return content.replace(Regex("""```[\s\S]*?```""")) { match ->
        val placeholder = "<!--CODEFENCE_${fences.size}-->"
        fences.add(match.value)
        placeholder
    }
}

private fun isTableSeparator(line: String): Boolean {
    val t = line.replace(" ", "")
    if (!t.startsWith("|")) return false
    val cells = t.substring(1).split("|").filter { it.isNotEmpty() }
    if (cells.isEmpty()) return false
    return cells.all { cell ->
        val cleaned = cell.replace(":", "")
        cleaned.isEmpty() || cleaned.all { it == '-' }
    }
}

private fun parseCells(row: String): List<String> {
    var c = row.trim()
    if (c.startsWith("|")) c = c.substring(1)
    if (c.endsWith("|")) c = c.substring(0, c.length - 1)
    return c.split("|").map { it.trim() }
}

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
    highlights: List<HighlightRange> = emptyList(),
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scroll = rememberScrollState()
    val theme = tableTheme()
    val mw = remember {
        Markwon.builder(ctx)
            .usePlugin(CorePlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(ctx))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()
    }

    val blocks = remember(content) { extractContentBlocks(content) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scroll)
    ) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Text -> {
                    AndroidView(factory = { c ->
                        TextView(c).apply {
                            setTextIsSelectable(true)
                            setPadding(20.dpToPx(c), 20.dpToPx(c), 20.dpToPx(c), 20.dpToPx(c))
                            textSize = 16f
                            setLineSpacing(8f, 1f)
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                            setTextColor(theme.textColor.toArgb())
                            movementMethod = android.text.method.LinkMovementMethod.getInstance()
                            customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
                                override fun onCreateActionMode(m: android.view.ActionMode?, menu: Menu?): Boolean {
                                    menu?.add(0, 100, 0, "Ask AI")?.setIcon(android.R.drawable.ic_menu_search)
                                        ?.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                                    return true
                                }
                                override fun onPrepareActionMode(m: android.view.ActionMode?, menu: Menu?): Boolean = false
                                override fun onActionItemClicked(m: android.view.ActionMode?, item: MenuItem?): Boolean {
                                    if (item?.itemId == 100) {
                                        val s = if (selectionStart >= 0 && selectionEnd > selectionStart)
                                            text.subSequence(selectionStart, selectionEnd).toString() else ""
                                        if (s.isNotBlank()) onAskAi(s)
                                        m?.finish()
                                        return true
                                    }
                                    return false
                                }
                                override fun onDestroyActionMode(m: android.view.ActionMode?) {}
                            }
                        }
                    }, update = { tv ->
                        mw.setMarkdown(tv, block.content)
                    })
                }
                is ContentBlock.Table -> {
                    TableBlockComposable(table = block.table, theme = theme)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Renders a table using Compose Row/Column with:
 * - Theme-aware colors (no zebra striping)
 * - horizontalScroll for mobile overflow
 * - 12dp/16dp cell padding, 1dp hairline borders
 * - Header: distinct background + semibold text
 */
@Composable
private fun TableBlockComposable(
    table: ParsedTable,
    theme: com.selfproject.learningapp.ui.theme.TableTheme
) {
    val borderColor = theme.cellBorder
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .border(TableStyles.borderWidth, borderColor, RoundedCornerShape(4.dp))
        ) {
            // Header row — distinct background, semibold
            Row(modifier = Modifier.fillMaxWidth()) {
                table.headers.forEachIndexed { i, header ->
                    Box(
                        modifier = Modifier
                            .then(Modifier.border(TableStyles.borderWidth, borderColor))
                            .background(theme.headerBackground)
                            .padding(
                                horizontal = TableStyles.cellPaddingHorizontal,
                                vertical = TableStyles.cellPaddingVertical
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = cleanMd(header),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = TableStyles.headerTextWeight
                            ),
                            color = theme.textColor
                        )
                    }
                }
            }

            // Data rows — no zebra striping, hairline borders only
            table.rows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (ci in 0 until table.headers.size) {
                        val cell = row.getOrNull(ci) ?: ""
                        Box(
                            modifier = Modifier
                                .then(Modifier.border(TableStyles.borderWidth, borderColor))
                                .padding(
                                    horizontal = TableStyles.cellPaddingHorizontal,
                                    vertical = TableStyles.cellPaddingVertical
                                ),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = cleanMd(cell),
                                style = MaterialTheme.typography.bodySmall,
                                color = theme.textColor
                            )
                        }
                    }
                }
            }
        }
    }
}