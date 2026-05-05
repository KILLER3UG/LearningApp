package com.selfproject.learningapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfproject.learningapp.model.AiModel
import com.selfproject.learningapp.model.ModelFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelector(
    selectedModel: AiModel,
    availableModels: List<AiModel>,
    onModelSelected: (AiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.width(170.dp)
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = selectedModel.displayName,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select model")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp)
        ) {
            ModelFamily.entries.forEach { family ->
                val familyModels = availableModels.filter { it.family == family }
                if (familyModels.isNotEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = family.name.replace("_", " "),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = { /* No-op, just a header */ },
                        enabled = false,
                    )

                    familyModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = model.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onModelSelected(model)
                                expanded = false
                            },
                            trailingIcon = {
                                if (model.id == selectedModel.id) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                        )
                    }

                    // Divider between families
                    HorizontalDivider()
                }
            }
        }
    }
}
