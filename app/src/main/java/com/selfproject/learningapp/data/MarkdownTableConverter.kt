package com.selfproject.learningapp.data

/**
 * Converts markdown tables to HTML tables for proper rendering.
 *
 * Key fixes from Issue 2:
 * - Accepts isDark parameter for theme-aware colors (no more hardcoded purple)
 * - Wraps table in overflow-x:auto div for mobile horizontal scrolling
 * - No zebra striping — clean hairline borders only
 * - Fixes code-block bleed: tables inside ``` blocks render as monospace text
 */
object MarkdownTableConverter {

    // Light mode
    private const val L_STYLES = "overflow-x:auto;max-width:100%;display:block;"
    private const val L_HEADER_BG = "#F8F9FA"
    private const val L_BORDER = "#E9ECEF"
    private const val L_TEXT = "#1C1C1E"

    // Dark mode
    private const val D_STYLES = "overflow-x:auto;max-width:100%;display:block;"
    private const val D_HEADER_BG = "#212529"
    private const val D_BORDER = "#343A40"
    private const val D_TEXT = "#FFFFFF"

    /**
     * Converts markdown tables to HTML, theme-aware.
     * Code fences (```) are protected — tables inside them render as monospace text.
     */
    fun convertTablesToHtml(content: String, isDark: Boolean): String {
        val (styles, headerBg, border, textColor) = if (isDark) {
            Quad(D_STYLES, D_HEADER_BG, D_BORDER, D_TEXT)
        } else {
            Quad(L_STYLES, L_HEADER_BG, L_BORDER, L_TEXT)
        }

        // Step 1: Extract and protect fenced code blocks
        val codeFences = mutableListOf<String>()
        val protected = content.replace(
            Regex("""```[\s\S]*?```""")
        ) { match ->
            val placeholder = "<!--CODEFENCE_${codeFences.size}-->"
            // Escape pipe chars inside code fences so they don't trigger table parsing
            codeFences.add(match.value.replace("|", "&#124;"))
            placeholder
        }

        // Step 2: Find and convert tables (only outside code fences)
        val result = StringBuilder()
        var i = 0
        val lines = protected.split("\n")

        while (i < lines.size) {
            val trimmedLine = lines[i].trim()

            // Detect table start: | cell | cell | with at least 3 pipes
            if (trimmedLine.startsWith("|") && trimmedLine.endsWith("|") && trimmedLine.count { it == '|' } >= 3) {
                // Check next line is separator
                if (i + 1 < lines.size && isTableSeparator(lines[i + 1].trim())) {
                    val tableLines = mutableListOf<String>()
                    var j = i
                    while (j < lines.size) {
                        val t = lines[j].trim()
                        if (t.startsWith("|") && t.endsWith("|")) {
                            if (isTableSeparator(t)) { j++; continue }
                            tableLines.add(t)
                            j++
                        } else break
                    }

                    if (tableLines.isNotEmpty()) {
                        result.append(buildHtmlTable(tableLines, styles, headerBg, border, textColor))
                        i = j
                        continue
                    }
                }
            }

            result.append(lines[i]).append("\n")
            i++
        }

        val converted = result.toString()

        // Step 3: Restore code fences (with escaped pipes now in place)
        return codeFences.foldIndexed(converted) { idx, text, fence ->
            text.replace("<!--CODEFENCE_$idx-->", "<pre><code>$fence</code></pre>")
        }
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun isTableSeparator(line: String): Boolean {
        val trimmed = line.replace(" ", "")
        if (!trimmed.startsWith("|")) return false
        val cells = trimmed.substring(1).split("|")
        for (cell in cells) {
            if (cell.isEmpty()) continue
            val cleaned = cell.replace(":", "").replace("-", "")
            if (cleaned.isNotEmpty()) return false
            if (cell.count { it == '-' } < 2) return false
        }
        return true
    }

    private fun buildHtmlTable(
        tableLines: List<String>,
        styles: String,
        headerBg: String,
        border: String,
        textColor: String
    ): String {
        val sb = StringBuilder()

        // Wrapping div for mobile horizontal scroll
        sb.append("<div style=\"$styles\">")
        sb.append("<table style=\"border-collapse:collapse;width:100%;\">")

        // Header row — distinct background, semibold
        val headerCells = parseCells(tableLines[0])
        sb.append("<thead><tr>")
        for (cell in headerCells) {
            sb.append("<th style=\"")
                .append("padding:12px 16px;")
                .append("text-align:left;")
                .append("font-weight:600;")
                .append("background-color:$headerBg;")
                .append("color:$textColor;")
                .append("border-bottom:1px solid $border;")
                .append("border-right:1px solid $border;")
                .append("\">")
                .append(inlineFormat(cell.trim()))
                .append("</th>")
        }
        sb.append("</tr></thead>")

        // Body rows — no zebra striping, hairline borders only
        if (tableLines.size > 1) {
            sb.append("<tbody>")
            for (rowIdx in 1 until tableLines.size) {
                val cells = parseCells(tableLines[rowIdx])
                sb.append("<tr>")
                for (cell in cells) {
                    sb.append("<td style=\"")
                        .append("padding:12px 16px;")
                        .append("color:$textColor;")
                        .append("border-bottom:1px solid $border;")
                        .append("border-right:1px solid $border;")
                        .append("\">")
                        .append(inlineFormat(cell.trim()))
                        .append("</td>")
                }
                sb.append("</tr>")
            }
            sb.append("</tbody>")
        }

        sb.append("</table>")
        sb.append("</div>")
        return sb.toString()
    }

    private fun parseCells(row: String): List<String> {
        var content = row.trim()
        if (content.startsWith("|")) content = content.substring(1)
        if (content.endsWith("|")) content = content.substring(0, content.length - 1)
        return content.split("|").map { it.trim() }
    }

    private fun inlineFormat(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
            .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
            .replace(Regex("`(.+?)`"), "<code style=\"background:#F0EDF6;padding:2px 6px;border-radius:4px;font-size:0.9em;\">$1</code>")
    }
}