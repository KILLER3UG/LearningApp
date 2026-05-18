package com.selfproject.learningapp.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.local.NoteAttachmentEntity
import com.selfproject.learningapp.data.fileparser.FileType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Renders a note's attachments as a clean materials section.
 * Apple Notes style — icon, filename, size, date, delete button.
 * Only renders when there are attachments.
 */
@Composable
fun MaterialsSection(
    attachments: List<NoteAttachmentEntity>,
    onAttachmentClick: (NoteAttachmentEntity) -> Unit,
    onAttachmentDelete: (NoteAttachmentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Materials (${attachments.size})",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        attachments.forEach { attachment ->
            MaterialAttachmentItem(
                attachment = attachment,
                onClick = { onAttachmentClick(attachment) },
                onDelete = { onAttachmentDelete(attachment) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun MaterialAttachmentItem(
    attachment: NoteAttachmentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val fileType = try { FileType.valueOf(attachment.fileType) } catch (e: Exception) { FileType.UNKNOWN }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = materialFileIcon(fileType),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${formatAttachmentSize(attachment.fileSizeBytes)} · ${
                        Instant.ofEpochMilli(attachment.uploadTimestamp)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove attachment",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun materialFileIcon(fileType: FileType) = when (fileType) {
    FileType.PDF      -> Icons.Default.Description
    FileType.DOC,
    FileType.DOCX     -> Icons.Default.Description
    FileType.PPTX,
    FileType.XLSX     -> Icons.Default.TableChart
    FileType.EPUB,
    FileType.HTML,
    FileType.RTF,
    FileType.MARKDOWN,
    FileType.PLAIN_TEXT,
    FileType.CSV,
    FileType.SQL      -> Icons.Default.Description
    FileType.IMAGE,
    FileType.SVG      -> Icons.Default.Image
    FileType.PY,
    FileType.JS,
    FileType.TS,
    FileType.JSX,
    FileType.CPP,
    FileType.JAVA,
    FileType.JSON     -> Icons.Default.Code
    FileType.MP3,
    FileType.M4A      -> Icons.Default.AudioFile
    FileType.UNKNOWN  -> Icons.Default.Description
}

private fun formatAttachmentSize(bytes: Long): String = when {
    bytes <= 0 -> "Unknown size"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
}
