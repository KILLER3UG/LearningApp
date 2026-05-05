package com.selfproject.learningapp.ui.components

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.BackgroundColorSpan
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.selfproject.learningapp.model.HighlightRange
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

private fun extractContentBlocks(content: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val lines = content.split("\n")
    val textBuffer = StringBuilder()
    var i = 0
    while (i < lines.size) {
        val trimmed = lines[i].trim()
        val isTableStart = trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.count { it == '|' } >= 2
        val hasSeparator = i + 1 < lines.size && isTableSeparator(lines[i + 1].trim())
        
        if (isTableStart && hasSeparator) {
            // Flush accumulated text
            if (textBuffer.isNotEmpty()) {
                blocks.add(ContentBlock.Text(textBuffer.toString()))
                textBuffer.clear()
            }
            // Extract table
            val tableLines = mutableListOf<String>()
            var j = i
            while (j < lines.size) {
                val t = lines[j].trim()
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
            textBuffer.append(lines[i]).append("\n")
            i++
        }
    }
    // Flush remaining text
    if (textBuffer.isNotEmpty()) {
        blocks.add(ContentBlock.Text(textBuffer.toString()))
    }
    return blocks
}

private fun isTableSeparator(line: String): Boolean {
    val t = line.replace(" ", "")
    if (!t.startsWith("|")) return false
    val cells = t.substring(1).split("|").filter { it.isNotEmpty() }
    if (cells.isEmpty()) return false
    return cells.all { cell ->
        val cleaned = cell.replace(":", "")
        cleaned.isNotEmpty() && cleaned.all { it == '-' }
    }
}

private fun parseCells(row: String): List<String> {
    var c = row.trim(); if (c.startsWith("|")) c = c.substring(1); if (c.endsWith("|")) c = c.substring(0, c.length - 1)
    return c.split("|").map { it.trim() }
}

private fun cleanMd(text: String): String = text.replace(Regex("\\*\\*(.+?)\\*\\*"), "$1").replace(Regex("\\*(.+?)\\*"), "$1").replace("`", "").replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1")

@Composable
fun MarkdownViewer(content: String, onTextSelected: (String) -> Unit, onAskAi: (String) -> Unit, highlights: List<HighlightRange> = emptyList(), modifier: Modifier = Modifier) {
    val ctx = LocalContext.current; val scroll = rememberScrollState()
    val dark = MaterialTheme.colorScheme.surface.toArgb() < 0xFF800000.toInt()
    val blocks = remember(content) { extractContentBlocks(content) }
    val tc = if (dark) 0xFFE6E1E5.toInt() else 0xFF1C1B1F.toInt()
    val lc = if (dark) 0xFFD0BCFF.toInt() else 0xFF6750A4.toInt()
    val mw = remember { Markwon.builder(ctx).usePlugin(CorePlugin.create()).usePlugin(StrikethroughPlugin.create()).usePlugin(TaskListPlugin.create(ctx)).usePlugin(HtmlPlugin.create()).usePlugin(LinkifyPlugin.create()).build() }
    val tbc = if (dark) 0xFF49454F.toInt() else 0xFFD0BCFF.toInt()
    val thb = if (dark) 0xFF332D41.toInt() else 0xFFEADDFF.toInt()
    val thf = if (dark) 0xFFE6E1E5.toInt() else 0xFF1C1B1F.toInt()
    val reb = if (dark) 0xFF1C1C1E.toInt() else 0xFFFFFFFF.toInt()
    val rob = if (dark) 0xFF252528.toInt() else 0xFFF8F7FF.toInt()
    val tcf = if (dark) 0xFFCAC4D0.toInt() else 0xFF49454F.toInt()
    val bcc = androidx.compose.ui.graphics.Color(tbc)

    Column(modifier = modifier.fillMaxSize().verticalScroll(scroll)) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Text -> {
                    AndroidView(factory = { c ->
                        TextView(c).apply {
                            setTextIsSelectable(true); setPadding(20.dpToPx(c), 20.dpToPx(c), 20.dpToPx(c), 20.dpToPx(c))
                            textSize = 16f; setLineSpacing(8f, 1f); typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                            setTextColor(tc); setLinkTextColor(lc); movementMethod = android.text.method.LinkMovementMethod.getInstance()
                            customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
                                override fun onCreateActionMode(m: android.view.ActionMode?, menu: android.view.Menu?): Boolean {
                                    menu?.add(0, 100, 0, "Ask AI")?.setIcon(android.R.drawable.ic_menu_search)?.setShowAsActionFlags(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM); return true
                                }
                                override fun onPrepareActionMode(m: android.view.ActionMode?, menu: android.view.Menu?): Boolean = false
                                override fun onActionItemClicked(m: android.view.ActionMode?, item: android.view.MenuItem?): Boolean {
                                    if (item?.itemId == 100) { val s = if (selectionStart >= 0 && selectionEnd > selectionStart) text.subSequence(selectionStart, selectionEnd).toString() else ""; if (s.isNotBlank()) onAskAi(s); m?.finish(); return true }; return false
                                }
                                override fun onDestroyActionMode(m: android.view.ActionMode?) {}
                            }
                        }
                    }, update = { tv ->
                        mw.setMarkdown(tv, block.content)
                    })
                }
                is ContentBlock.Table -> {
                    val table = block.table
                    // Table label
                    Text(
                        text = "📊 Table: ${table.headers.size} headers × ${table.rows.size} rows",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    // Table container with horizontal scrolling
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).border(2.dp, bcc, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp))) {
                            // Header row
                            Row(modifier = Modifier.background(androidx.compose.ui.graphics.Color(thb))) {
                                table.headers.forEachIndexed { i, h ->
                                    Box(modifier = Modifier.weight(1f).padding(16.dp, 12.dp)) { Text(text = cleanMd(h), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color(thf), maxLines = 2, overflow = TextOverflow.Ellipsis) }
                                    if (i < table.headers.size - 1) HorizontalDivider(modifier = Modifier.width(1.dp).fillMaxHeight(), color = bcc, thickness = 1.dp)
                                }
                            }
                            HorizontalDivider(color = bcc, thickness = 1.dp)
                            // Data rows
                            table.rows.forEachIndexed { ri, row ->
                                Row(modifier = Modifier.background(androidx.compose.ui.graphics.Color(if (ri % 2 == 0) reb else rob))) {
                                    val mc = maxOf(table.headers.size, row.size)
                                    for (ci in 0 until mc) {
                                        Box(modifier = Modifier.weight(1f).padding(16.dp, 10.dp)) { Text(text = cleanMd(if (ci < row.size) row[ci] else ""), style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color(tcf), maxLines = 3, overflow = TextOverflow.Ellipsis) }
                                        if (ci < mc - 1) HorizontalDivider(modifier = Modifier.width(1.dp).fillMaxHeight(), color = bcc, thickness = 1.dp)
                                    }
                                }
                                if (ri < table.rows.size - 1) HorizontalDivider(color = bcc, thickness = 1.dp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
