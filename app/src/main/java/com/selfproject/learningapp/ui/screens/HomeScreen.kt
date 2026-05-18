package com.selfproject.learningapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.model.Note
import com.selfproject.learningapp.viewmodel.HomeViewModel

/**
 * Home screen — note list with subject filter pills, swipe-to-pin/delete.
 * Pull-to-refresh, clean list view (Apple Notes / Bear Notes aesthetic).
 * Issue 1: Note list with subjects and swipe actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNoteClick: (Note) -> Unit,
    onOpenDocumentClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val notes by viewModel.notes.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "StudyNotes",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onOpenDocumentClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Open material",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Subject filter pills
            item {
                if (subjects.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(subjects) { subject ->
                            FilterChip(
                                selected = selectedSubject == subject,
                                onClick = {
                                    viewModel.selectSubject(
                                        if (selectedSubject == subject) null else subject
                                    )
                                },
                                label = {
                                    Text(
                                        subject,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }
                }
            }

            items(
                items = notes,
                key = { it.id }
            ) { note ->
                NoteListItem(
                    note = note,
                    onClick = { onNoteClick(note) },
                    onPin = { viewModel.pinNote(note) },
                    onDelete = { viewModel.deleteNote(note) }
                )
            }

            if (notes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Start with a file",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Open a document and ask questions from a clean AI workspace.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = onOpenDocumentClick) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Material")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                SwipeToDismissBoxValue.StartToEnd -> { onPin(); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isDelete = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val color = if (isDelete) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Icon(
                    imageVector = if (isDelete) Icons.Default.Delete else Icons.Default.PushPin,
                    contentDescription = if (isDelete) "Delete" else "Pin",
                    tint = if (isDelete) MaterialTheme.colorScheme.onErrorContainer
                           else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (note.pinned) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (note.preview.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (note.subjects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            note.subjects.take(3).forEach { subject ->
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            subject,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (note.attachmentCount > 0) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${note.attachmentCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
