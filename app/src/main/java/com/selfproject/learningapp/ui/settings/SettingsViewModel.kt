package com.selfproject.learningapp.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.selfproject.learningapp.data.repository.ApiProvider
import com.selfproject.learningapp.data.repository.ApiConfig
import com.selfproject.learningapp.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val apiKey: String = "",
    val modelName: String = "",
    val provider: ApiProvider = ApiProvider.GOOGLE_AI_STUDIO,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.baseUrl,
                settingsRepository.modelName,
                settingsRepository.provider
            ) { baseUrl, modelName, provider ->
                Triple(baseUrl, modelName, provider)
            }.collect { (baseUrl, modelName, provider) ->
                _uiState.value = _uiState.value.copy(
                    baseUrl = baseUrl,
                    modelName = modelName,
                    provider = provider
                )
                // Load API key separately from encrypted storage
                val apiKey = settingsRepository.getApiKey()
                _uiState.value = _uiState.value.copy(apiKey = apiKey)
            }
        }
    }

    fun setBaseUrl(url: String) {
        _uiState.value = _uiState.value.copy(
            baseUrl = url,
            provider = ApiProvider.detectFromUrl(url),
            saveSuccess = false,
            saveError = null
        )
    }

    fun setApiKey(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key, saveSuccess = false, saveError = null)
    }

    fun setModelName(name: String) {
        _uiState.value = _uiState.value.copy(modelName = name, saveSuccess = false, saveError = null)
    }

    fun setProvider(provider: ApiProvider) {
        _uiState.value = _uiState.value.copy(
            provider = provider,
            // Apply defaults when switching provider
            baseUrl = if (_uiState.value.baseUrl.isBlank()) provider.defaultBaseUrl else _uiState.value.baseUrl,
            modelName = if (_uiState.value.modelName.isBlank()) provider.defaultModel else _uiState.value.modelName,
            saveSuccess = false,
            saveError = null
        )
    }

    fun applyPreset(provider: ApiProvider) {
        _uiState.value = _uiState.value.copy(
            provider = provider,
            baseUrl = provider.defaultBaseUrl,
            modelName = provider.defaultModel
        )
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveSuccess = false, saveError = null)
            try {
                val state = _uiState.value
                settingsRepository.saveBaseUrl(state.baseUrl)
                settingsRepository.saveApiKey(state.apiKey)
                settingsRepository.saveModelName(state.modelName)
                settingsRepository.saveProvider(state.provider)
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, saveError = e.message)
            }
        }
    }
}
