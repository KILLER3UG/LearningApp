package com.selfproject.learningapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.viewmodel.PendingAttachment
import com.selfproject.learningapp.viewmodel.UploadState

/**
 * Study-first composer — floating bottom input bar with:
 * - Attachment button (44x44pt minimum touch target)
 * - TextField with 44dp min height, rounded corners
 * - Send FAB as filled circular accent CTA
 * - Safe-area padding above keyboard
 *
 * Issue 3: Clean surfaces, no glassmorphism, shadowElevation 8dp
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyComposer(
    text: String,
    onTextChange: (String) -> Unit = { _ -> },
    onSend: () -> Unit,
    onAttach: () -> Unit,
    pendingAttachments: List<PendingAttachment>,
    onRemoveAttachment: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Attachment chip row — shown when files are pending
            if (pendingAttachments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pendingAttachments.forEach { att ->
                        AttachmentChip(
                            fileName = att.fileName,
                            fileType = att.fileType,
                            fileSizeBytes = att.sizeBytes,
                            uploadState = att.uploadState,
                            onRemove = { onRemoveAttachment(att.id) },
                            onClick = { /* preview */ }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attachment button — 44x44pt minimum touch target
                IconButton(
                    onClick = onAttach,
                    modifier = Modifier
                        .size(44.dp)
                        .semantics { contentDescription = "Attach file" }
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Attach file",
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text input — expands, min height 44dp
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .semantics { contentDescription = "Message input" },
                    placeholder = {
                        Text(
                            "Ask about your materials",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(22.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (text.isNotBlank()) {
                                onSend()
                            }
                        }
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send FAB — filled circular accent CTA
                FloatingActionButton(
                    onClick = {
                        if (text.isNotBlank() && !isLoading) {
                            onSend()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Send message" },
                    containerColor = if (text.isNotBlank() && !isLoading) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (text.isNotBlank() && !isLoading) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    shape = CircleShape
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
