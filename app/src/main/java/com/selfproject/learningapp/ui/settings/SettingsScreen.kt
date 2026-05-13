package com.selfproject.learningapp.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.selfproject.learningapp.data.repository.ApiProvider
import com.selfproject.learningapp.ui.theme.StudyNotesAnimations
import com.selfproject.learningapp.ui.theme.StudyNotesShapes
import com.selfproject.learningapp.ui.theme.StudyNotesDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = StudyNotesDimens.screenPadding)
        ) {
            Spacer(modifier = Modifier.height(StudyNotesDimens.spacing4))

            // ── AI Configuration ────────────────────────────────
            Text(
                text = "AI Configuration",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = StudyNotesDimens.spacing3)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = StudyNotesShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(StudyNotesDimens.cardPaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(StudyNotesDimens.spacing5)
                ) {
                    // Provider selector
                    Text(
                        text = "Provider",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ProviderSelector(
                        selected = uiState.provider,
                        onSelect = { viewModel.applyPreset(it) }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )

                    // API Base URL
                    var baseUrlFocused by remember { mutableStateOf(false) }
                    SettingsTextField(
                        label = "API Base URL",
                        value = uiState.baseUrl,
                        placeholder = uiState.provider.defaultBaseUrl,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next,
                        isFocused = baseUrlFocused,
                        onFocusChange = { baseUrlFocused = it },
                        onValueChange = { viewModel.setBaseUrl(it) },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )

                    // API Key
                    var apiKeyFocused by remember { mutableStateOf(false) }
                    var apiKeyVisible by remember { mutableStateOf(false) }
                    SettingsTextField(
                        label = "API Key",
                        value = uiState.apiKey,
                        placeholder = "sk-••••••••••••",
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                        isFocused = apiKeyFocused,
                        onFocusChange = { apiKeyFocused = it },
                        onValueChange = { viewModel.setApiKey(it) },
                        visualTransformation = if (apiKeyVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                Icon(
                                    imageVector = if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (apiKeyVisible) "Hide key" else "Show key",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )

                    // Model Name
                    var modelFocused by remember { mutableStateOf(false) }
                    SettingsTextField(
                        label = "Model Name",
                        value = uiState.modelName,
                        placeholder = uiState.provider.defaultModel,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        isFocused = modelFocused,
                        onFocusChange = { modelFocused = it },
                        onValueChange = { viewModel.setModelName(it) },
                        leadingIcon = {
                            Icon(Icons.Default.ModelTraining, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        onImeAction = {
                            focusManager.clearFocus()
                            viewModel.saveSettings()
                        }
                    )

                    Spacer(modifier = Modifier.height(StudyNotesDimens.spacing2))

                    // Save button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.saveSettings()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !uiState.isSaving && uiState.apiKey.isNotBlank(),
                        shape = StudyNotesShapes.small
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (uiState.isSaving) "Saving..." else "Save Settings",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Success / Error feedback
                    AnimatedVisibility(
                        visible = uiState.saveSuccess,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = StudyNotesDimens.spacing2),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Settings saved successfully",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.saveError != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = StudyNotesDimens.spacing2),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                uiState.saveError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(StudyNotesDimens.spacing6))

            // ── Provider Info Card ─────────────────────────────
            AnimatedVisibility(
                visible = true,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                InfoCard(provider = uiState.provider)
            }

            Spacer(modifier = Modifier.height(StudyNotesDimens.spacing6))

            // ── About ────────────────────────────────────────────
            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = StudyNotesDimens.spacing3)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = StudyNotesShapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StudyNotesDimens.cardPaddingLarge),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "StudyNotes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Version 1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(StudyNotesDimens.spacing16))
        }
    }
}

// ── Provider Selector ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderSelector(
    selected: ApiProvider,
    onSelect: (ApiProvider) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = StudyNotesShapes.small,
            colors = OutlinedTextFieldDefaults.colors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ApiProvider.entries.forEach { provider ->
                DropdownMenuItem(
                    text = { Text(provider.displayName) },
                    onClick = {
                        onSelect(provider)
                        expanded = false
                    },
                    trailingIcon = if (provider == selected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null
                )
            }
        }
    }
}

// ── Info Card ─────────────────────────────────────────────────

@Composable
private fun InfoCard(provider: ApiProvider) {
    val (icon, title, message) = when (provider) {
        ApiProvider.GOOGLE_AI_STUDIO -> Triple(
            Icons.Default.Cloud,
            "Google AI Studio",
            "Uses Google's Gemini/Gemma models. Get your API key from aistudio.google.com. Default model: gemini-2.0-flash."
        )
        ApiProvider.OPENAI_COMPATIBLE -> Triple(
            Icons.Default.Terminal,
            "OpenAI Compatible",
            "Works with LM Studio, LocalAI, and other custom backends that expose the OpenAI chat completions API."
        )
        ApiProvider.OLLAMA -> Triple(
            Icons.Default.Devices,
            "Ollama",
            "Connect to a local Ollama server running on your machine. Must have Ollama installed and models downloaded."
        )
        ApiProvider.ANTHROPIC -> Triple(
            Icons.Default.Psychology,
            "Anthropic Claude",
            "Use Anthropic's Claude models. Get your API key from console.anthropic.com. Default model: claude-sonnet-4."
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = StudyNotesShapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(StudyNotesDimens.spacing4),
            horizontalArrangement = Arrangement.spacedBy(StudyNotesDimens.spacing3),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ── Settings TextField ───────────────────────────────────────

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onImeAction: () -> Unit = {}
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = StudyNotesDimens.spacing1)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChange(it.isFocused) },
            shape = StudyNotesShapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

