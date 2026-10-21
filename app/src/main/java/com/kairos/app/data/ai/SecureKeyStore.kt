package com.kairos.app.data.ai

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds the person's OpenRouter key, encrypted with a key that never leaves the Android Keystore. */
class SecureKeyStore(context: Context) {
    private val prefs: SharedPreferences = runCatching {
        EncryptedSharedPreferences.create(
            context,
            "kairos_secrets",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }.getOrElse {
        // A corrupted keystore entry (rare, after some restores) must not brick the app.
        context.deleteSharedPreferences("kairos_secrets")
        context.getSharedPreferences("kairos_secrets_fallback", Context.MODE_PRIVATE)
    }

    private val _hasKey = MutableStateFlow(apiKey() != null)
    val hasKey: StateFlow<Boolean> = _hasKey.asStateFlow()

    fun apiKey(): String? = prefs.getString(KEY, null)?.takeIf { it.isNotBlank() }

    fun setApiKey(value: String?) {
        prefs.edit().apply { if (value.isNullOrBlank()) remove(KEY) else putString(KEY, value.trim()) }.apply()
        _hasKey.value = !value.isNullOrBlank()
    }

    private companion object { const val KEY = "openrouter_key" }
}
