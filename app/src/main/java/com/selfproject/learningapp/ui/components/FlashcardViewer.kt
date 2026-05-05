package com.selfproject.learningapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.local.FlashcardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardViewer(
    flashcards: List<FlashcardEntity>,
    onReview: (FlashcardEntity, Boolean) -> Unit,
    onDelete: (FlashcardEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBack by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Flashcards (${flashcards.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (flashcards.isEmpty()) {
                Text(
                    text = "No flashcards yet. Select text and ask AI to generate flashcards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val card = flashcards[currentIndex]

                // Progress indicator
                LinearProgressIndicator(
                    progress = (currentIndex + 1).toFloat() / flashcards.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Flashcard
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clickable { showBack = !showBack },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = if (showBack) "Answer" else "Question",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (showBack) card.answer else card.question,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                // Navigation row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            currentIndex = maxOf(0, currentIndex - 1)
                            showBack = false
                        },
                        enabled = currentIndex > 0
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                    }

                    Text(
                        text = "${currentIndex + 1} / ${flashcards.size}",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    IconButton(
                        onClick = {
                            currentIndex = minOf(flashcards.size - 1, currentIndex + 1)
                            showBack = false
                        },
                        enabled = currentIndex < flashcards.size - 1
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }

                // Review buttons
                if (showBack) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                onReview(card, false)
                                showBack = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Still Learning")
                        }
                        Button(
                            onClick = {
                                onReview(card, true)
                                showBack = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Know It")
                        }
                    }
                } else {
                    Button(
                        onClick = { showBack = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Show Answer")
                    }
                }

                // Delete button
                TextButton(
                    onClick = {
                        onDelete(card)
                        if (currentIndex >= flashcards.size - 1) {
                            currentIndex = maxOf(0, flashcards.size - 2)
                        }
                        showBack = false
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete Card")
                }
            }
        }
    }
}
