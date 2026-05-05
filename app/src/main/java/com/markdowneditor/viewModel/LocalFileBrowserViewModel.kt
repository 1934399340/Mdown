package com.markdowneditor.viewModel

import android.os.Environment
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class LocalFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val lastModified: Long,
    val size: Long
)

@HiltViewModel
class LocalFileBrowserViewModel @Inject constructor() : ViewModel() {

    private val storageRoot = Environment.getExternalStorageDirectory()

    private val _currentPath = MutableStateFlow(storageRoot.absolutePath)
    val currentPath: StateFlow<String> = _currentPath

    private val _pathHistory = MutableStateFlow(listOf(storageRoot.absolutePath))
    val pathHistory: StateFlow<List<String>> = _pathHistory

    fun navigateTo(path: String) {
        _pathHistory.value = _pathHistory.value + path
        _currentPath.value = path
    }

    fun goBack(): Boolean {
        val history = _pathHistory.value
        if (history.size > 1) {
            _pathHistory.value = history.dropLast(1)
            _currentPath.value = _pathHistory.value.last()
            return true
        }
        return false
    }

    fun getStorageRoot(): File = storageRoot
}
