package com.markdowneditor.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdowneditor.data.repository.ApiKeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val apiKeyRepository: ApiKeyRepository
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isKeySaved = MutableStateFlow(false)
    val isKeySaved: StateFlow<Boolean> = _isKeySaved.asStateFlow()

    init {
        viewModelScope.launch {
            apiKeyRepository.apiKeyFlow.collect { key ->
                _apiKey.value = key
                _isKeySaved.value = key.isNotBlank()
            }
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            apiKeyRepository.saveApiKey(key)
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            apiKeyRepository.clearApiKey()
        }
    }
}
