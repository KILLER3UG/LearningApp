package com.selfproject.learningapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "studynotes_settings")

/**
 * Manages API settings persistence.
 * Uses DataStore for non-sensitive settings and EncryptedSharedPreferences for API keys.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val BASE_URL   = stringPreferencesKey("api_base_url")
        val MODEL_NAME = stringPreferencesKey("model_name")
        val PROVIDER   = stringPreferencesKey("api_provider")

        const val ENCRYPTED_PREFS_NAME = "studynotes_secure_prefs"
        const val API_KEY_ALIAS        = "api_key"
    }

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            Keys.ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.BASE_URL] ?: ApiProvider.GOOGLE_AI_STUDIO.defaultBaseUrl
    }

    val modelName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.MODEL_NAME] ?: ApiProvider.GOOGLE_AI_STUDIO.defaultModel
    }

    val provider: Flow<ApiProvider> = context.dataStore.data.map { prefs ->
        prefs[Keys.PROVIDER]?.let {
            try { ApiProvider.valueOf(it) } catch (_: Exception) { ApiProvider.GOOGLE_AI_STUDIO }
        } ?: ApiProvider.GOOGLE_AI_STUDIO
    }

    /**
     * Gets the API key. Returns empty string if not set.
     */
    suspend fun getApiKey(): String = encryptedPrefs.getString(Keys.API_KEY_ALIAS, "") ?: ""

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BASE_URL] = url
            // Auto-detect provider when URL changes
            prefs[Keys.PROVIDER] = ApiProvider.detectFromUrl(url).name
        }
    }

    suspend fun saveModelName(name: String) {
        context.dataStore.edit { prefs -> prefs[Keys.MODEL_NAME] = name }
    }

    suspend fun saveApiKey(key: String) {
        if (key.isBlank()) {
            encryptedPrefs.edit().remove(Keys.API_KEY_ALIAS).apply()
        } else {
            encryptedPrefs.edit().putString(Keys.API_KEY_ALIAS, key).apply()
        }
    }

    suspend fun saveProvider(provider: ApiProvider) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PROVIDER] = provider.name
            // Also update base URL and model to match provider defaults if currently unset
            if (prefs[Keys.BASE_URL].isNullOrBlank()) {
                prefs[Keys.BASE_URL] = provider.defaultBaseUrl
            }
            if (prefs[Keys.MODEL_NAME].isNullOrBlank()) {
                prefs[Keys.MODEL_NAME] = provider.defaultModel
            }
        }
    }

    /**
     * Returns all current settings as an ApiConfig.
     */
    suspend fun getConfig(): ApiConfig = ApiConfig(
        baseUrl   = baseUrl.first(),
        apiKey    = getApiKey(),
        modelName = modelName.first(),
        provider  = provider.first()
    )
}

data class ApiConfig(
    val baseUrl: String,
    val apiKey: String,
    val modelName: String,
    val provider: ApiProvider
)
