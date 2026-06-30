package com.markdowneditor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.apiKeyDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_settings")

@Singleton
class ApiKeyRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val apiKeyKey = stringPreferencesKey("deepseek_api_key")

    /**
     * Flow 形式的 API Key（用于 UI 观察）
     */
    val apiKeyFlow: Flow<String> = context.apiKeyDataStore.data.map { prefs ->
        prefs[apiKeyKey] ?: ""
    }

    /**
     * 挂起函数形式获取 API Key（用于一次性读取）
     */
    suspend fun getApiKey(): String {
        val prefs = context.apiKeyDataStore.data.first()
        return prefs[apiKeyKey] ?: ""
    }

    /**
     * 保存 API Key
     */
    suspend fun saveApiKey(key: String) {
        context.apiKeyDataStore.edit { prefs ->
            prefs[apiKeyKey] = key.trim()
        }
    }

    /**
     * 清除 API Key
     */
    suspend fun clearApiKey() {
        context.apiKeyDataStore.edit { prefs ->
            prefs.remove(apiKeyKey)
        }
    }
}
