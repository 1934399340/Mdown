package com.markdowneditor.viewModel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    init {
        scope.launch {
            context.themeDataStore.data.collect { prefs ->
                _darkMode.value = prefs[darkModeKey] ?: false
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        scope.launch {
            context.themeDataStore.edit { prefs ->
                prefs[darkModeKey] = enabled
            }
        }
    }
}
