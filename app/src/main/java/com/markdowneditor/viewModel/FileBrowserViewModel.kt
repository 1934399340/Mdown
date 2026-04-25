package com.markdowneditor.viewModel

import androidx.lifecycle.ViewModel
import com.markdowneditor.data.model.MarkdownFile
import com.markdowneditor.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileBrowserViewModel(private val repository: FileRepository) : ViewModel() {
    private val _files = MutableStateFlow<List<MarkdownFile>>(emptyList())
    val files: StateFlow<List<MarkdownFile>> get() = _files

    suspend fun loadFiles() {
        repository.getAllFiles().collect {
            _files.value = it
        }
    }

    suspend fun createFile(fileName: String) {
        // 实际实现中需要调用FileManager创建文件
    }

    suspend fun deleteFile(file: MarkdownFile) {
        repository.delete(file)
        // 实际实现中需要调用FileManager删除文件
    }
}
