package com.selfproject.learningapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.fileparser.FileType
import com.selfproject.learningapp.viewmodel.UploadState

/**
 * Apple Notes–style attachment chip.
 * Shows a file-type icon (or image thumbnail), filename, size, and upload state.
 * Issue 1: Universal file upload UI.
 */
@Composable
fun AttachmentChip(
    fileName: String,
    fileType: FileType,
    fileSizeBytes: Long,
    uploadState: UploadState,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 2.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // File-type icon (no external image library — use icons for all file types)
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (uploadState) {
                    is UploadState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = fileTypeIcon(fileType),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.widthIn(max = 160.dp)
            ) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (uploadState) {
                        is UploadState.Loading -> "Uploading…"
                        is UploadState.Success -> formatFileSize(fileSizeBytes)
                        is UploadState.Error -> uploadState.message
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uploadState is UploadState.Error)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uploadState !is UploadState.Loading) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove attachment",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun fileTypeIcon(fileType: FileType) = when (fileType) {
    FileType.PDF    -> Icons.Default.Description
    FileType.DOC,
    FileType.DOCX   -> Icons.Default.Description
    FileType.PPTX,
    FileType.XLSX   -> Icons.Default.TableChart
    FileType.EPUB,
    FileType.HTML,
    FileType.RTF,
    FileType.MARKDOWN,
    FileType.PLAIN_TEXT,
    FileType.CSV,
    FileType.SQL    -> Icons.Default.Description
    FileType.IMAGE,
    FileType.SVG    -> Icons.Default.Image
    FileType.PY,
    FileType.JS,
    FileType.TS,
    FileType.JSX,
    FileType.CPP,
    FileType.JAVA,
    FileType.JSON   -> Icons.Default.Code
    FileType.MP3,
    FileType.M4A    -> Icons.Default.AudioFile
    FileType.UNKNOWN -> Icons.Default.Description
}

private fun formatFileSize(bytes: Long): String = when {
    bytes <= 0 -> "Unknown size"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
