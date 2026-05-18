package com.selfproject.learningapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.MatchTile
import com.selfproject.learningapp.data.StudyPracticeEngine
import com.selfproject.learningapp.data.local.FlashcardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchGameViewer(
    flashcards: List<FlashcardEntity>,
    onReview: (FlashcardEntity, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var roundSeed by remember { mutableIntStateOf(1) }
    var mistakes by remember(roundSeed) { mutableIntStateOf(0) }
    var selectedTile by remember(roundSeed) { mutableStateOf<MatchTile?>(null) }
    var matchedPairKeys by remember(roundSeed) { mutableStateOf<Set<String>>(emptySet()) }

    val tiles = remember(flashcards, roundSeed) {
        StudyPracticeEngine.buildMatchTiles(flashcards, maxPairs = 6, seed = roundSeed)
    }
    val totalPairs = remember(tiles) { tiles.map { it.pairKey }.distinct().size }
    val roundComplete = totalPairs > 0 && matchedPairKeys.size == totalPairs

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(10.dp))
                    Column {
                        Text("Match", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Pair questions with answers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (tiles.size < 4) {
                Text(
                    "Generate at least two flashcards to start Match mode.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MatchStat(label = "Matched", value = "${matchedPairKeys.size}/$totalPairs", modifier = Modifier.weight(1f))
                MatchStat(label = "Misses", value = mistakes.toString(), modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))

            tiles.chunked(2).forEach { rowTiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTiles.forEach { tile ->
                        MatchTileCard(
                            tile = tile,
                            selected = selectedTile?.id == tile.id,
                            matched = tile.pairKey in matchedPairKeys,
                            onClick = {
                                if (tile.pairKey in matchedPairKeys) return@MatchTileCard
                                val first = selectedTile
                                if (first == null) {
                                    selectedTile = tile
                                } else if (first.id == tile.id) {
                                    selectedTile = null
                                } else if (StudyPracticeEngine.isMatch(first, tile)) {
                                    matchedPairKeys = matchedPairKeys + tile.pairKey
                                    selectedTile = null
                                    findMatchedFlashcard(flashcards, first, tile)?.let { card ->
                                        onReview(card, true)
                                    }
                                } else {
                                    mistakes += 1
                                    selectedTile = tile
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowTiles.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (roundComplete) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(10.dp))
                        Text(
                            "Round complete. Your matched cards were marked as known.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        roundSeed += 1
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("New Round")
                }
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun MatchStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun MatchTileCard(
    tile: MatchTile,
    selected: Boolean,
    matched: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = when {
        matched -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        selected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val border = when {
        matched -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        selected -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        modifier = modifier
            .height(104.dp)
            .clickable(enabled = !matched, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = border,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tile.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun findMatchedFlashcard(
    flashcards: List<FlashcardEntity>,
    first: MatchTile,
    second: MatchTile
): FlashcardEntity? {
    return flashcards.firstOrNull { card ->
        (card.question == first.text && card.answer == second.text) ||
            (card.answer == first.text && card.question == second.text)
    }
}
