package com.selfproject.learningapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.data.local.QuizEntity

/**
 * Inline quiz viewer - lightweight rendering to avoid memory issues.
 */
@Composable
fun InlineQuizViewer(
    quizzes: List<QuizEntity>,
    onBackToChat: () -> Unit,
    onDelete: (QuizEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val answeredStates = remember { mutableStateMapOf<Long, InlineQuizAnswerState>() }

    if (quizzes.isEmpty()) {
        Column(modifier = modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onBackToChat) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to chat")
            }
            Spacer(Modifier.height(24.dp))
            Text("No quiz questions generated yet", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val answered = answeredStates.values.count { it != InlineQuizAnswerState.Unanswered }

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
                Text("Quiz ($answered/${quizzes.size})", style = MaterialTheme.typography.titleLarge)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(quizzes, key = { it.id }) { quiz ->
                QuizQuestionCard(
                    quiz = quiz,
                    state = answeredStates[quiz.id] ?: InlineQuizAnswerState.Unanswered,
                    onAnswer = { correct ->
                        answeredStates[quiz.id] = if (correct) InlineQuizAnswerState.Correct else InlineQuizAnswerState.Incorrect
                    },
                    onDelete = { onDelete(quiz) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

enum class InlineQuizAnswerState { Unanswered, Correct, Incorrect }

@Composable
private fun QuizQuestionCard(
    quiz: QuizEntity,
    state: InlineQuizAnswerState,
    onAnswer: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAnswer by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                InlineQuizAnswerState.Correct -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                InlineQuizAnswerState.Incorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                InlineQuizAnswerState.Unanswered -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = quiz.question,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                }
            }

            if (state == InlineQuizAnswerState.Unanswered && !showAnswer) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = { showAnswer = true }) { Text("Reveal Answer") }
            } else if (showAnswer || state != InlineQuizAnswerState.Unanswered) {
                Spacer(Modifier.height(8.dp))
                Text(text = "Answer: ${quiz.answer}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (quiz.explanation.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(text = quiz.explanation,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (state == InlineQuizAnswerState.Unanswered) {
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { onAnswer(false) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Wrong")
                        }
                        Button(onClick = { onAnswer(true) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Correct")
                        }
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (state == InlineQuizAnswerState.Correct) "✓ Correct" else "✗ Incorrect",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state == InlineQuizAnswerState.Correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
