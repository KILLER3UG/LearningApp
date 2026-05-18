package com.selfproject.learningapp.data

enum class MarkdownTableAlignment {
    Start,
    Center,
    End
}

data class MarkdownTable(
    val headers: List<String>,
    val rows: List<List<String>>,
    val alignments: List<MarkdownTableAlignment>
) {
    val columnCount: Int = headers.size
}

sealed class MarkdownContentBlock {
    data class Text(val content: String) : MarkdownContentBlock()
    data class Table(val table: MarkdownTable) : MarkdownContentBlock()
}

/**
 * Splits Markdown into plain Markdown blocks and table blocks.
 *
 * Markwon's TextView renderer does not lay out wide tables reliably in this app,
 * so table blocks are rendered by Compose while the rest still goes through Markwon.
 */
object MarkdownContentParser {

    fun parse(content: String): List<MarkdownContentBlock> {
        if (content.isEmpty()) return emptyList()

        val normalized = content.replace("\r\n", "\n").replace("\r", "\n")
        val lines = normalized.split("\n")
        val blocks = mutableListOf<MarkdownContentBlock>()
        val textBuffer = StringBuilder()
        var insideFence = false
        var fenceMarker: String? = null
        var i = 0

        fun flushText() {
            if (textBuffer.isNotEmpty()) {
                blocks.add(MarkdownContentBlock.Text(textBuffer.toString()))
                textBuffer.clear()
            }
        }

        while (i < lines.size) {
            val marker = fenceBoundary(lines[i])
            if (marker != null) {
                if (!insideFence) {
                    insideFence = true
                    fenceMarker = marker
                } else if (marker == fenceMarker) {
                    insideFence = false
                    fenceMarker = null
                }
                appendLine(textBuffer, lines[i])
                i++
                continue
            }

            if (!insideFence) {
                val table = parseTable(lines, i)
                if (table != null) {
                    flushText()
                    blocks.add(MarkdownContentBlock.Table(table.table))
                    i = table.nextIndex
                    continue
                }
            }

            appendLine(textBuffer, lines[i])
            i++
        }

        flushText()
        return blocks
    }

    private data class TableResult(
        val table: MarkdownTable,
        val nextIndex: Int
    )

    private fun parseTable(lines: List<String>, startIndex: Int): TableResult? {
        if (startIndex + 1 >= lines.size) return null

        val headerCells = parseTableRow(lines[startIndex])
        if (headerCells.size < 2) return null

        val alignments = parseSeparatorRow(lines[startIndex + 1], headerCells.size) ?: return null
        val rows = mutableListOf<List<String>>()
        var i = startIndex + 2

        while (i < lines.size) {
            val line = lines[i]
            if (line.isBlank() || fenceBoundary(line) != null) break

            val rowCells = parseTableRow(line)
            if (rowCells.size < 2) break

            rows.add(normalizeCells(rowCells, headerCells.size))
            i++
        }

        return TableResult(
            table = MarkdownTable(
                headers = headerCells,
                rows = rows,
                alignments = alignments
            ),
            nextIndex = i
        )
    }

    private fun parseSeparatorRow(line: String, expectedColumns: Int): List<MarkdownTableAlignment>? {
        val cells = parseTableRow(line)
        if (cells.size != expectedColumns) return null

        val alignments = mutableListOf<MarkdownTableAlignment>()
        for (cell in cells) {
            alignments.add(parseSeparatorCell(cell) ?: return null)
        }
        return alignments
    }

    private fun parseSeparatorCell(cell: String): MarkdownTableAlignment? {
        val trimmed = cell.trim().replace(" ", "")
        if (trimmed.length < 2) return null

        val startsWithColon = trimmed.startsWith(":")
        val endsWithColon = trimmed.endsWith(":")
        val hyphens = trimmed.removePrefix(":").removeSuffix(":")

        if (hyphens.length < 2 || !hyphens.all { it == '-' }) return null

        return when {
            startsWithColon && endsWithColon -> MarkdownTableAlignment.Center
            endsWithColon -> MarkdownTableAlignment.End
            else -> MarkdownTableAlignment.Start
        }
    }

    private fun parseTableRow(row: String): List<String> {
        var content = row.trim()
        if (!containsTablePipe(content)) return listOf(content)

        if (content.startsWith("|")) content = content.drop(1)
        if (content.endsWith("|") && !isEscaped(content, content.lastIndex)) {
            content = content.dropLast(1)
        }

        val cells = mutableListOf<String>()
        val current = StringBuilder()
        var insideCodeSpan = false
        var i = 0

        while (i < content.length) {
            val ch = content[i]
            when {
                ch == '\\' && i + 1 < content.length && content[i + 1] == '|' -> {
                    current.append('|')
                    i += 2
                }
                ch == '`' && !isEscaped(content, i) -> {
                    insideCodeSpan = !insideCodeSpan
                    current.append(ch)
                    i++
                }
                ch == '|' && !insideCodeSpan && !isEscaped(content, i) -> {
                    cells.add(current.toString().trim())
                    current.clear()
                    i++
                }
                else -> {
                    current.append(ch)
                    i++
                }
            }
        }

        cells.add(current.toString().trim())
        return cells
    }

    private fun normalizeCells(cells: List<String>, expectedColumns: Int): List<String> {
        return when {
            cells.size == expectedColumns -> cells
            cells.size < expectedColumns -> cells + List(expectedColumns - cells.size) { "" }
            expectedColumns == 1 -> listOf(cells.joinToString(" | "))
            else -> cells.take(expectedColumns - 1) +
                cells.drop(expectedColumns - 1).joinToString(" | ")
        }
    }

    private fun containsTablePipe(line: String): Boolean {
        var insideCodeSpan = false
        for (i in line.indices) {
            val ch = line[i]
            if (ch == '`' && !isEscaped(line, i)) {
                insideCodeSpan = !insideCodeSpan
            } else if (ch == '|' && !insideCodeSpan && !isEscaped(line, i)) {
                return true
            }
        }
        return false
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

    private fun fenceBoundary(line: String): String? {
        val trimmed = line.trimStart()
        return when {
            trimmed.startsWith("```") -> "```"
            trimmed.startsWith("~~~") -> "~~~"
            else -> null
        }
    }

    private fun appendLine(buffer: StringBuilder, line: String) {
        buffer.append(line).append('\n')
    }
}
