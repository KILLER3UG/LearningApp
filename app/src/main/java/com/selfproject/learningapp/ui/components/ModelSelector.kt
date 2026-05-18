package com.selfproject.learningapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.widthIn(min = 118.dp, max = 184.dp)
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = selectedModel.displayName,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Select model",
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp)
        ) {
            ModelFamily.entries.forEach { family ->
                val familyModels = availableModels.filter { it.family == family }
                if (familyModels.isNotEmpty()) {
                    Text(
                        text = family.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
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
