package com.selfproject.learningapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.local.FlashcardEntity

/**
 * Inline flashcard viewer - renders a single card at a time to avoid memory issues.
 */
@Composable
fun InlineFlashcardViewer(
    flashcards: List<FlashcardEntity>,
    onBackToChat: () -> Unit,
    onReview: (FlashcardEntity, Boolean) -> Unit,
    onDelete: (FlashcardEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    if (flashcards.isEmpty()) {
        Column(modifier = modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onBackToChat) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to chat")
            }
            Spacer(Modifier.height(24.dp))
            Text("No flashcards generated yet", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val card = flashcards[currentIndex.coerceIn(0, flashcards.size - 1)]

    Column(modifier = modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 35.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackToChat,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Back to Chat", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.width(12.dp))
                Text("Flashcards (${flashcards.size})", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(24.dp))

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / flashcards.size },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text("${currentIndex + 1} / ${flashcards.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp))

        Spacer(Modifier.height(24.dp))

        // 3D flip animation
        val flipRotation by animateFloatAsState(
            targetValue = if (showAnswer) 180f else 0f,
            animationSpec = tween(400),
            label = "cardFlip"
        )
        val cardRotation = if (flipRotation > 90f) flipRotation - 180f else flipRotation

        // Single card only - no LazyColumn
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp).clickable { showAnswer = !showAnswer }
                .graphicsLayer {
                    rotationY = cardRotation
                    cameraDistance = 12.dp.toPx()
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (flipRotation > 90f) "Answer" else "Question",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(text = if (flipRotation > 90f) card.answer else card.question,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                Spacer(Modifier.height(16.dp))
                if (!showAnswer) TextButton(onClick = { showAnswer = true }) { Text("Tap to reveal answer") }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(
                onClick = { if (currentIndex > 0) { currentIndex--; showAnswer = false } },
                enabled = currentIndex > 0
            ) { Text("← Previous") }
            TextButton(
                onClick = { if (currentIndex < flashcards.size - 1) { currentIndex++; showAnswer = false } },
                enabled = currentIndex < flashcards.size - 1
            ) { Text("Next →") }
        }

        Spacer(Modifier.height(8.dp))

        if (showAnswer) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onReview(card, false); showAnswer = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Still Learning")
                }
                Button(
                    onClick = { onReview(card, true); showAnswer = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Know It")
                }
            }
        }

        TextButton(
            onClick = {
                onDelete(card)
                if (currentIndex >= flashcards.size - 1) currentIndex = maxOf(0, flashcards.size - 2)
                showAnswer = false
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp, bottom = 24.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Delete Card")
        }
    }
}
