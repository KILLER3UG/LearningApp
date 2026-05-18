package com.selfproject.learningapp.data.fileparser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory

/**
 * Extracts readable table text from Excel workbooks.
 */
class SpreadsheetFileParser : FileParser {
    override val supportedType = FileType.XLSX

    override suspend fun parse(uri: Uri, context: Context): FileParseResult =
        withContext(Dispatchers.IO) {
            try {
                val formatter = DataFormatter()
                val content = StringBuilder()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    WorkbookFactory.create(inputStream).use { workbook ->
                        for (sheetIndex in 0 until workbook.numberOfSheets) {
                            val sheet = workbook.getSheetAt(sheetIndex)
                            content.appendLine("## ${sheet.sheetName}")
                            for (row in sheet) {
                                val cells = row.map { cell ->
                                    formatter.formatCellValue(cell).trim()
                                }
                                if (cells.any { it.isNotBlank() }) {
                                    content.appendLine(cells.joinToString("\t"))
                                }
                            }
                            content.appendLine()
                        }
                    }
                } ?: return@withContext FileParseResult.Error("Could not open spreadsheet stream")

                val text = content.toString().trim()
                if (text.isBlank()) {
                    FileParseResult.Error("Spreadsheet appears to have no extractable text")
                } else {
                    FileParseResult.Success(
                        text = text,
                        metadata = DocumentMetadata(
                            sourceFileName = uri.lastPathSegment,
                            fileType = FileType.XLSX
                        )
                    )
                }
            } catch (e: Exception) {
                FileParseResult.Error("Spreadsheet parsing failed: ${e.message}")
            }
        }
}
