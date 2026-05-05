package com.selfproject.learningapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.model.QuestionerConfig
import com.selfproject.learningapp.model.QuestionerMode

/**
 * Questioner configuration panel - lets user select count and mode before starting.
 */
@Composable
fun QuestionerConfigPanel(
    onStart: (QuestionerConfig) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCount by remember { mutableStateOf(25) }
    var selectedMode by remember { mutableStateOf(QuestionerMode.CRITIC) }
    val countOptions = listOf(10, 15, 20, 25, 30, 40, 50, 75, 100)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 35.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
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
                    Text("Back", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.width(12.dp))
                Text("Questioner", style = MaterialTheme.typography.titleLarge)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Description
            Text(
                text = "The AI will question you on every topic in your document. Choose how you want to be questioned:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Mode selection
            Text(
                text = "Questioning Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            QuestionerMode.entries.forEach { mode ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMode = mode }
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = if (mode == selectedMode)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == selectedMode,
                            onClick = { selectedMode = mode }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${mode.icon}  ${mode.label}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (mode == selectedMode) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Question Count
            Text(
                text = "Number of Questions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(countOptions) { count ->
                    FilterChip(
                        selected = selectedCount == count,
                        onClick = { selectedCount = count },
                        label = { Text(count.toString()) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Start button
            Button(
                onClick = { onStart(QuestionerConfig(selectedCount, selectedMode)) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("${selectedMode.icon} Start ${selectedMode.label}", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
