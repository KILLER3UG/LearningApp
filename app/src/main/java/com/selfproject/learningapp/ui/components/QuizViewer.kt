package com.selfproject.learningapp.ui.components

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
import com.selfproject.learningapp.data.local.QuizEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizViewer(
    quizzes: List<QuizEntity>,
    onDelete: (QuizEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val answeredStates = remember { mutableStateMapOf<Long, QuizAnswerState>() }

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
                val answered = answeredStates.values.count { it != QuizAnswerState.Unanswered }
                Text(
                    text = "Quiz (${answered}/${quizzes.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (quizzes.isEmpty()) {
                Text(
                    text = "No quizzes yet. Select text and ask AI to generate a quiz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 500.dp)
                ) {
                    items(quizzes, key = { it.id }) { quiz ->
                        QuizItem(
                            quiz = quiz,
                            state = answeredStates[quiz.id] ?: QuizAnswerState.Unanswered,
                            onAnswer = { correct ->
                                answeredStates[quiz.id] = if (correct) {
                                    QuizAnswerState.Correct
                                } else {
                                    QuizAnswerState.Incorrect
                                }
                            },
                            onDelete = { onDelete(quiz) }
                        )
                    }
                }
            }
        }
    }
}

enum class QuizAnswerState {
    Unanswered, Correct, Incorrect
}

@Composable
private fun QuizItem(
    quiz: QuizEntity,
    state: QuizAnswerState,
    onAnswer: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAnswer by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                QuizAnswerState.Correct -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                QuizAnswerState.Incorrect -> MaterialTheme.colorScheme.surfaceVariant
                QuizAnswerState.Unanswered -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = quiz.question,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
            }

            if (state == QuizAnswerState.Unanswered && !showAnswer) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { showAnswer = true }) {
                    Text("Reveal Answer")
                }
            } else if (showAnswer || state != QuizAnswerState.Unanswered) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = quiz.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (quiz.explanation.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = quiz.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state == QuizAnswerState.Unanswered) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { onAnswer(false) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Wrong")
                        }
                        Button(
                            onClick = { onAnswer(true) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Correct")
                        }
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state == QuizAnswerState.Correct) "Correct" else "Incorrect",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state == QuizAnswerState.Correct)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
