package com.selfproject.learningapp.data

/**
 * Converts markdown tables to HTML tables for proper rendering.
 */
object MarkdownTableConverter {

    /**
     * Finds all markdown tables in the content and converts them to HTML.
     */
    fun convertTablesToHtml(content: String): String {
        val result = StringBuilder()
        var i = 0
        val lines = content.split("\n")

        while (i < lines.size) {
            val trimmedLine = lines[i].trim()

            // Check if this line could be the start of a table (contains |)
            if (trimmedLine.startsWith("|") && trimmedLine.endsWith("|") && trimmedLine.count { it == '|' } >= 3) {
                // Check if next line is a separator
                if (i + 1 < lines.size && isTableSeparator(lines[i + 1].trim())) {
                    // Found a table - collect all table rows
                    val tableLines = mutableListOf<String>()
                    var j = i
                    while (j < lines.size) {
                        val t = lines[j].trim()
                        if (t.startsWith("|") && t.endsWith("|")) {
                            if (isTableSeparator(t)) {
                                // Skip separator line but keep collecting
                                j++
                                continue
                            }
                            tableLines.add(t)
                            j++
                        } else {
                            break
                        }
                    }

                    if (tableLines.isNotEmpty()) {
                        result.append(buildHtmlTable(tableLines))
                        i = j
                        continue
                    }
                }
            }

            result.append(lines[i]).append("\n")
            i++
        }

        return result.toString()
    }

    private fun isTableSeparator(line: String): Boolean {
        val trimmed = line.replace(" ", "")
        if (!trimmed.startsWith("|")) return false
        val body = trimmed.substring(1)
        // Each cell separator should be ---, :---, ---:, or :---:
        val cells = body.split("|")
        for (cell in cells) {
            if (cell.isEmpty()) continue
            val cleaned = cell.replace(":", "").replace("-", "")
            if (cleaned.isNotEmpty()) return false
            if (cell.count { it == '-' } < 2) return false
        }
        return true
    }

    private fun buildHtmlTable(tableLines: List<String>): String {
        val html = StringBuilder()
        html.append("<table style=\"width:100%%; border-collapse:collapse; margin:12px 0; border-radius:8px; overflow:hidden; border:1px solid #D0BCFF;\">\n")

        // First row is header
        val headerCells = parseCells(tableLines[0])
        html.append("<thead>\n<tr>")
        for (cell in headerCells) {
            html.append("<th style=\"padding:12px 16px; background-color:#EADDFF; font-weight:700; border-bottom:2px solid #D0BCFF; text-align:left; color:#1C1B1F;\">")
                .append(inlineFormat(cell.trim()))
                .append("</th>")
        }
        html.append("</tr>\n</thead>\n")

        // Remaining rows are body
        if (tableLines.size > 1) {
            html.append("<tbody>\n")
            for (rowIdx in 1 until tableLines.size) {
                val cells = parseCells(tableLines[rowIdx])
                val bgColor = if (rowIdx % 2 == 0) "#F8F7FF" else "#FFFFFF"
                html.append("<tr>")
                for (cell in cells) {
                    html.append("<td style=\"padding:10px 16px; border-bottom:1px solid #E7E0EC; background-color:$bgColor;\">")
                        .append(inlineFormat(cell.trim()))
                        .append("</td>")
                }
                html.append("</tr>\n")
            }
            html.append("</tbody>\n")
        }

        html.append("</table>")
        return html.toString()
    }

    private fun parseCells(row: String): List<String> {
        // Remove leading and trailing |
        var content = row.trim()
        if (content.startsWith("|")) content = content.substring(1)
        if (content.endsWith("|")) content = content.substring(0, content.length - 1)
        return content.split("|")
    }

    private fun inlineFormat(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
            .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
            .replace(Regex("`(.+?)`"), "<code style=\"background:#F0EDF6; padding:2px 6px; border-radius:4px; font-size:0.9em;\">$1</code>")
    }
}
