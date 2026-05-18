package com.selfproject.learningapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.StudyPracticeEngine
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (flashcards.isEmpty()) {
                Text(
                    text = "No flashcards yet. Select text and ask AI to generate flashcards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val card = flashcards[currentIndex]
                val stats = remember(flashcards) { StudyPracticeEngine.buildReviewStats(flashcards) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlashcardStat("Memory", "${stats.memoryScore}%", Modifier.weight(1f))
                    FlashcardStat("Due", stats.due.toString(), Modifier.weight(1f))
                    FlashcardStat("Mastered", stats.mastered.toString(), Modifier.weight(1f))
                }

                // Progress indicator
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / flashcards.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Flashcard with 3D flip animation
                val rotation by animateFloatAsState(
                    targetValue = if (showBack) 180f else 0f,
                    animationSpec = tween(400),
                    label = "cardFlip"
                )
                val cardRotation = if (rotation > 90f) rotation - 180f else rotation
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clickable { showBack = !showBack }
                        .graphicsLayer {
                            rotationY = cardRotation
                            cameraDistance = 12.dp.toPx()
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = if (rotation > 90f) "Answer" else "Question",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (rotation > 90f) card.answer else card.question,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
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
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }

                // Review buttons
                if (showBack) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onReview(card, false)
                                showBack = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Still Learning")
                        }
                        Button(
                            onClick = {
                                onReview(card, true)
                                showBack = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
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

@Composable
private fun FlashcardStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall)
        }
    }
}
