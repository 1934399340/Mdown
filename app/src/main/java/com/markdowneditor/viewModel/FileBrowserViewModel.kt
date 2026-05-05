package com.markdowneditor.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdowneditor.data.model.MarkdownFile
import com.markdowneditor.data.repository.FileRepository
import com.markdowneditor.utils.ExportManager
import com.markdowneditor.utils.FileManager
import com.markdowneditor.utils.MarkdownRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class ClipboardAction { COPY, CUT }

data class ClipboardState(
    val file: MarkdownFile,
    val action: ClipboardAction
)

@HiltViewModel
class FileBrowserViewModel @Inject constructor(
    private val repository: FileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _files = MutableStateFlow<List<MarkdownFile>>(emptyList())
    val files: StateFlow<List<MarkdownFile>> get() = _files

    private val _clipboard = MutableStateFlow<ClipboardState?>(null)
    val clipboard: StateFlow<ClipboardState?> get() = _clipboard

    private val _pasteMessage = MutableStateFlow<String?>(null)
    val pasteMessage: StateFlow<String?> get() = _pasteMessage

    private val _currentFolder = MutableStateFlow<String?>(null)
    val currentFolder: StateFlow<String?> get() = _currentFolder

    fun setCurrentFolder(folder: String?) {
        _currentFolder.value = folder
    }

    private val fileManager: FileManager by lazy {
        FileManager(context.getExternalFilesDir(null) ?: context.filesDir)
    }

    private val docsDir: File by lazy {
        context.getExternalFilesDir(null) ?: context.filesDir
    }

    suspend fun loadFiles() {
        repository.getAllFiles().collect {
            _files.value = it
        }
    }

    fun loadFilesFromDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            val fileList = fileManager.listFiles()
            withContext(Dispatchers.Main) {
                _files.value = fileList
            }
        }
    }

    fun createFile(fileName: String, folderPath: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetDir = folderPath?.let { File(it) }
            val newFile = fileManager.createFile(fileName, targetDir = targetDir)
            repository.insert(newFile)
            withContext(Dispatchers.Main) {
                loadFilesFromDisk()
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fileManager.createFolder(folderName)
            withContext(Dispatchers.Main) {
                loadFilesFromDisk()
            }
        }
    }

    fun deleteFile(file: MarkdownFile) {
        viewModelScope.launch(Dispatchers.IO) {
            fileManager.deleteFile(file.filePath)
            repository.delete(file)
            withContext(Dispatchers.Main) {
                loadFilesFromDisk()
            }
        }
    }

    fun copyFile(file: MarkdownFile) {
        _clipboard.value = ClipboardState(file, ClipboardAction.COPY)
    }

    fun cutFile(file: MarkdownFile) {
        _clipboard.value = ClipboardState(file, ClipboardAction.CUT)
    }

    fun pasteToFolder(targetFolderPath: String?) {
        val clip = _clipboard.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val targetDir = if (targetFolderPath != null) File(targetFolderPath) else docsDir
            val success = when (clip.action) {
                ClipboardAction.COPY -> fileManager.copyFileToDir(clip.file.filePath, targetDir)
                ClipboardAction.CUT -> {
                    val moved = fileManager.moveFileToDir(clip.file.filePath, targetDir)
                    if (moved) _clipboard.value = null
                    moved
                }
            }
            withContext(Dispatchers.Main) {
                if (success) {
                    val actionName = if (clip.action == ClipboardAction.COPY) "复制" else "移动"
                    _pasteMessage.value = "${actionName}成功"
                } else {
                    _pasteMessage.value = "操作失败，目标位置可能已存在同名文件"
                }
                loadFilesFromDisk()
            }
        }
    }

    fun clearPasteMessage() {
        _pasteMessage.value = null
    }

    fun clearClipboard() {
        _clipboard.value = null
    }

    fun getMyDocsDir(): File = docsDir

    private val markdownRenderer by lazy { MarkdownRenderer() }
    private val exportManager by lazy { ExportManager(markdownRenderer) }

    fun exportFile(file: MarkdownFile, format: String, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val sourceFile = File(file.filePath)
            if (!sourceFile.exists()) {
                withContext(Dispatchers.Main) { onResult("文件不存在") }
                return@launch
            }
            val markdown = sourceFile.readText()
            val name = file.fileName

            when (format) {
                "pdf" -> exportManager.exportToPdf(markdown, name, context) { msg, _ ->
                    viewModelScope.launch(Dispatchers.Main) { onResult(msg) }
                }
                "html" -> exportManager.exportToHtml(markdown, name, context) { msg, _ ->
                    viewModelScope.launch(Dispatchers.Main) { onResult(msg) }
                }
                "txt" -> exportManager.exportToTxt(markdown, name, context) { msg, _ ->
                    viewModelScope.launch(Dispatchers.Main) { onResult(msg) }
                }
                "md" -> {
                    val success = exportManager.shareMarkdownFile(file.filePath, context)
                    withContext(Dispatchers.Main) {
                        if (!success) onResult("分享失败：文件不存在") else onResult("已打开分享")
                    }
                }
                "image" -> exportManager.exportToImage(markdown, name, context) { msg, _ ->
                    viewModelScope.launch(Dispatchers.Main) { onResult(msg) }
                }
            }
        }
    }
}
