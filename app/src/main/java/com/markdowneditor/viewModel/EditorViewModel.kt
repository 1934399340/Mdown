package com.markdowneditor.viewModel

import android.content.Context
import android.content.Intent
import android.net.Uri

import android.webkit.WebView
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _markdownText = MutableStateFlow("")
    val markdownText: StateFlow<String> get() = _markdownText

    private var _cursorPosition = 0
    val cursorPosition: Int get() = _cursorPosition

    private var _selectionStart = 0
    val selectionStart: Int get() = _selectionStart

    private var _selectionEnd = 0
    val selectionEnd: Int get() = _selectionEnd

    private var currentFilePath: String? = null
    private var currentFileName: String = ""

    private val _fileName = MutableStateFlow("")
    val fileName: StateFlow<String> get() = _fileName

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> get() = _isLoaded

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> get() = _exportMessage

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> get() = _showExportDialog

    private val _lastExportUri = MutableStateFlow<Uri?>(null)
    val lastExportUri: StateFlow<Uri?> get() = _lastExportUri

    private val _showShareDialog = MutableStateFlow(false)
    val showShareDialog: StateFlow<Boolean> get() = _showShareDialog

    private val _pickImageTrigger = MutableStateFlow(false)
    val pickImageTrigger: StateFlow<Boolean> get() = _pickImageTrigger

    private val _showLinkDialog = MutableStateFlow(false)
    val showLinkDialog: StateFlow<Boolean> get() = _showLinkDialog

    // Undo stack
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private val maxUndoDepth = 50

    // WebView reference for screenshot export
    var webViewRef: WeakReference<WebView>? = null

    private val fileManager: FileManager by lazy {
        FileManager(context.getExternalFilesDir(null) ?: context.filesDir)
    }

    private val myDocsDir: File by lazy {
        context.getExternalFilesDir(null) ?: context.filesDir
    }

    private val markdownRenderer by lazy { MarkdownRenderer() }
    private val exportManager by lazy { ExportManager(markdownRenderer) }

    fun loadFile(filePath: String) {
        if (_isLoaded.value && currentFilePath == filePath) return

        viewModelScope.launch(Dispatchers.IO) {
            val file = File(filePath)
            if (file.exists()) {
                currentFilePath = filePath
                currentFileName = file.nameWithoutExtension
                _fileName.value = currentFileName
                _markdownText.value = file.readText()
                _isLoaded.value = true
            } else {
                val name = file.nameWithoutExtension
                currentFilePath = filePath
                currentFileName = name
                _fileName.value = name
                file.parentFile?.mkdirs()
                file.writeText("")
                _markdownText.value = ""
                _isLoaded.value = true
            }
            undoStack.clear()
            redoStack.clear()
        }
    }

    fun saveFile() {
        viewModelScope.launch(Dispatchers.IO) {
            val path = currentFilePath
            if (path != null) {
                val file = File(path)
                file.parentFile?.mkdirs()
                file.writeText(_markdownText.value)

                val myDocsPath = myDocsDir.absolutePath
                val isInMyDocsDirectly = path.startsWith(myDocsPath) &&
                    path.removePrefix(myDocsPath).trim('/').let { rel ->
                        !rel.contains('/') && rel.endsWith(".md", ignoreCase = true)
                    }

                if (!isInMyDocsDirectly) {
                    val myDocsFile = File(myDocsDir, file.name)
                    if (myDocsFile.absolutePath != path) {
                        myDocsFile.writeText(_markdownText.value)
                        if (path.startsWith(myDocsPath) && file.isFile) {
                            file.delete()
                        }
                        currentFilePath = myDocsFile.absolutePath
                    }
                }
            }
        }
    }

    fun updateText(text: String) {
        if (text != _markdownText.value) {
            pushUndo(_markdownText.value)
        }
        _markdownText.value = text
    }

    fun setCursorPosition(position: Int) {
        _cursorPosition = position
    }

    fun setSelection(start: Int, end: Int) {
        _selectionStart = start
        _selectionEnd = end
        _cursorPosition = end
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(_markdownText.value)
        _markdownText.value = undoStack.removeAt(undoStack.lastIndex)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(_markdownText.value)
        _markdownText.value = redoStack.removeAt(redoStack.lastIndex)
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    private fun pushUndo(text: String) {
        undoStack.add(text)
        if (undoStack.size > maxUndoDepth) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun insertBold() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript("document.execCommand('bold');", null)
            return
        }
        insertMarkdown("**", "**")
    }

    fun insertItalic() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript("document.execCommand('italic');", null)
            return
        }
        insertMarkdown("*", "*")
    }

    fun insertMarkdown(prefix: String, suffix: String) {
        val wv = webViewRef?.get()
        if (wv != null) {
            val ep = jsEsc(prefix)
            val es = jsEsc(suffix)
            wv.evaluateJavascript(
                "(function(){var p='$ep',s='$es',sel=window.getSelection();if(sel.rangeCount>0){var r=sel.getRangeAt(0),t=r.toString()||'',n=document.createTextNode(p+t+s);r.deleteContents();r.insertNode(n);r.selectNodeContents(n);sel.removeAllRanges();sel.addRange(r);}})()",
                null
            )
            return
        }
        val text = _markdownText.value
        val selStart = minOf(_selectionStart, text.length)
        val selEnd = minOf(_selectionEnd, text.length)

        if (selStart != selEnd) {
            val selected = text.substring(minOf(selStart, selEnd), maxOf(selStart, selEnd))
            val before = text.substring(0, minOf(selStart, selEnd))
            val after = text.substring(maxOf(selStart, selEnd))
            pushUndo(text)
            _markdownText.value = before + prefix + selected + suffix + after
            _cursorPosition = minOf(selStart, selEnd) + prefix.length + selected.length
        } else {
            val pos = minOf(_cursorPosition, text.length)
            val beforeCursor = text.substring(0, pos)
            val afterCursor = text.substring(pos)
            pushUndo(text)
            _markdownText.value = beforeCursor + prefix + suffix + afterCursor
            _cursorPosition = pos + prefix.length
        }
    }

    fun insertHeading(level: Int) {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                "document.execCommand('formatBlock',false,'<h$level>');",
                null
            )
            return
        }
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val currentLineStart = text.lastIndexOf('\n', pos - 1) + 1
        val currentLineEnd = text.indexOf('\n', pos).let { if (it == -1) text.length else it }
        val currentLine = text.substring(currentLineStart, currentLineEnd)
        val strippedLine = currentLine.replace(Regex("^#{1,6}\\s*"), "")
        val headingPrefix = "#".repeat(level) + " "
        val newLine = headingPrefix + strippedLine
        pushUndo(text)
        _markdownText.value = text.substring(0, currentLineStart) + newLine + text.substring(currentLineEnd)
        _cursorPosition = currentLineStart + newLine.length
    }

    fun insertCodeBlock(language: String = "") {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                """(function(){var pre=document.createElement('pre'),code=document.createElement('code');code.textContent='​';pre.appendChild(code);var sel=window.getSelection();if(sel.rangeCount>0){var r=sel.getRangeAt(0);if(r.collapsed){r.insertNode(pre)}else{r.deleteContents();r.insertNode(pre)}r.setStartAfter(code);r.collapse(true);sel.removeAllRanges();sel.addRange(r)}else{document.body.appendChild(pre)}})()""",
                null
            )
            return
        }
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val beforeCursor = text.substring(0, pos)
        val afterCursor = text.substring(pos)
        val codeBlock = "```$language\n\n```"
        pushUndo(text)
        _markdownText.value = beforeCursor + codeBlock + afterCursor
        _cursorPosition = pos + "```$language\n".length
    }

    fun insertTable(rows: Int = 2, columns: Int = 3) {
        val wv = webViewRef?.get()
        if (wv != null) {
            val tableHtml = buildString {
                append("<table><thead><tr>")
                repeat(columns) { append("<th>列${it + 1}</th>") }
                append("</tr></thead><tbody>")
                repeat(rows) {
                    append("<tr>")
                    repeat(columns) { append("<td>&#160;</td>") }
                    append("</tr>")
                }
                append("</tbody></table>")
            }.replace("\"", "\\\"")
            wv.evaluateJavascript(
                """(function(){var tbl=document.createElement('div');tbl.innerHTML='$tableHtml';var sel=window.getSelection();if(sel.rangeCount>0){var r=sel.getRangeAt(0);r.insertNode(tbl)}else{document.body.appendChild(tbl)}})()""",
                null
            )
            return
        }
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val beforeCursor = text.substring(0, pos)
        val afterCursor = text.substring(pos)

        val table = buildString {
            append("| ")
            for (c in 1..columns) append("列$c | ")
            append("\n| ")
            for (c in 1..columns) append(" --- | ")
            append("\n")
            repeat(rows) {
                append("| ")
                for (c in 1..columns) append("   | ")
                append("\n")
            }
        }

        pushUndo(text)
        _markdownText.value = beforeCursor + table + afterCursor
        _cursorPosition = pos + table.indexOf("\n") + 1
    }

    fun insertMathBlock() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                """(function(){var pre=document.createElement('pre'),code=document.createElement('code');code.textContent='$$​$$';pre.appendChild(code);var sel=window.getSelection();if(sel.rangeCount>0){var r=sel.getRangeAt(0);r.insertNode(pre)}else{document.body.appendChild(pre)}})()""",
                null
            )
            return
        }
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val beforeCursor = text.substring(0, pos)
        val afterCursor = text.substring(pos)
        val mathBlock = "$$\n\n$$"
        pushUndo(text)
        _markdownText.value = beforeCursor + mathBlock + afterCursor
        _cursorPosition = pos + "$$\n".length
    }

    fun insertQuote() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                "(function(){var sel=window.getSelection();if(!sel.rangeCount)return;var r=sel.getRangeAt(0),bq=document.createElement('blockquote');if(r.collapsed){var p=document.createElement('p');p.appendChild(document.createElement('br'));bq.appendChild(p);r.insertNode(bq)}else{bq.appendChild(r.extractContents());r.insertNode(bq)}})()",
                null
            )
            return
        }
        insertMarkdown("> ", "")
    }

    fun insertUnorderedList() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript("document.execCommand('insertUnorderedList');", null)
            return
        }
        insertMarkdown("- ", "")
    }

    fun insertOrderedList() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript("document.execCommand('insertOrderedList');", null)
            return
        }
        insertMarkdown("1. ", "")
    }

    fun insertLink() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                "(function(){var s=window.getSelection();return (s&&s.rangeCount>0)?s.getRangeAt(0).toString():'';})()",
                { result ->
                    val raw = result?.trim('"')?.replace("\\n", "\n")?.take(200) ?: ""
                    _linkPresetText = raw
                    _showLinkDialog.value = true
                }
            )
            return
        }
        val text = _markdownText.value
        val selStart = minOf(_selectionStart, text.length)
        val selEnd = minOf(_selectionEnd, text.length)
        if (selStart != selEnd) {
            _linkPresetText = text.substring(minOf(selStart, selEnd), maxOf(selStart, selEnd))
        }
        _showLinkDialog.value = true
    }

    private var _linkPresetText: String = ""

    fun getLinkPresetText(): String = _linkPresetText

    fun insertLinkWithUrl(url: String) {
        _showLinkDialog.value = false
        val wv = webViewRef?.get()
        if (wv != null) {
            val safeUrl = jsEsc(url.ifBlank { "https://" })
            wv.evaluateJavascript(
                "(function(){" +
                "var sel=window.getSelection();" +
                "if(!sel.rangeCount)return;" +
                "var r=sel.getRangeAt(0);" +
                "var txt=r.toString().trim()||'链接';" +
                "var a=document.createElement('a');" +
                "a.href='$safeUrl';a.textContent=txt;" +
                "if(r.collapsed){a.textContent='$safeUrl';r.insertNode(a);r.setStartAfter(a);r.collapse(true);sel.removeAllRanges();sel.addRange(r)}" +
                "else{r.deleteContents();r.insertNode(a);r.setStartAfter(a);r.collapse(true);sel.removeAllRanges();sel.addRange(r)}" +
                "})()",
                null
            )
            return
        }
        val safeUrl = url.ifBlank { "https://" }
        val text = _markdownText.value
        val selStart = minOf(_selectionStart, text.length)
        val selEnd = minOf(_selectionEnd, text.length)

        if (selStart != selEnd) {
            val selected = text.substring(minOf(selStart, selEnd), maxOf(selStart, selEnd))
            val before = text.substring(0, minOf(selStart, selEnd))
            val after = text.substring(maxOf(selStart, selEnd))
            pushUndo(text)
            _markdownText.value = before + "[$selected]($safeUrl)" + after
            _cursorPosition = minOf(selStart, selEnd) + selected.length + 3
        } else {
            val pos = minOf(_cursorPosition, text.length)
            val beforeCursor = text.substring(0, pos)
            val afterCursor = text.substring(pos)
            val linkText = _linkPresetText.ifBlank { "链接" }
            pushUndo(text)
            _markdownText.value = beforeCursor + "[$linkText]($safeUrl)" + afterCursor
            _cursorPosition = pos + linkText.length + 2
        }
    }

    fun dismissLinkDialog() {
        _showLinkDialog.value = false
    }

    fun insertHorizontalRule() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript("document.execCommand('insertHorizontalRule');", null)
            return
        }
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val beforeCursor = text.substring(0, pos)
        val afterCursor = text.substring(pos)
        val hr = "\n---\n"
        pushUndo(text)
        _markdownText.value = beforeCursor + hr + afterCursor
        _cursorPosition = pos + hr.length
    }

    fun insertImage() {
        _pickImageTrigger.value = true
    }

    fun onImagePickerHandled() {
        _pickImageTrigger.value = false
    }

    fun insertImageFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imagesDir = File(context.getExternalFilesDir(null), "images")
                imagesDir.mkdirs()
                val timestamp = System.currentTimeMillis()
                val ext = getExtensionFromUri(uri) ?: "jpg"
                val destFile = File(imagesDir, "img_${timestamp}.$ext")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        destFile
                    )
                    val wv = webViewRef?.get()
                    if (wv != null) {
                        // RICH mode: insert via JavaScript at cursor
                        val src = contentUri.toString().replace("'", "\\'")
                        wv.evaluateJavascript(
                            "(function(){var img=document.createElement('img');img.src='$src';img.alt='图片';var sel=window.getSelection();if(sel.rangeCount>0){var r=sel.getRangeAt(0);r.insertNode(img);r.setStartAfter(img);r.collapse(true);sel.removeAllRanges();sel.addRange(r)}else{document.body.appendChild(img)}})()",
                            null
                        )
                    } else {
                        // SOURCE mode: insert markdown text
                        val text = _markdownText.value
                        val pos = minOf(_cursorPosition, text.length)
                        val beforeCursor = text.substring(0, pos)
                        val afterCursor = text.substring(pos)
                        val image = "![图片](${contentUri})"
                        pushUndo(text)
                        _markdownText.value = beforeCursor + image + afterCursor
                        _cursorPosition = pos + "![图片]".length
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _exportMessage.value = "图片插入失败: ${e.message}"
                }
            }
        }
    }

    private fun getExtensionFromUri(uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri) ?: return null
        return when {
            mimeType.contains("png") -> "png"
            mimeType.contains("gif") -> "gif"
            mimeType.contains("webp") -> "webp"
            mimeType.contains("bmp") -> "bmp"
            else -> "jpg"
        }
    }

    fun insertStrikethrough() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript("document.execCommand('strikeThrough');", null)
            return
        }
        insertMarkdown("~~", "~~")
    }

    fun insertTaskList() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                "(function(){var ul=document.createElement('ul');var li=document.createElement('li');li.className='task-list-item';var cb=document.createElement('input');cb.type='checkbox';li.appendChild(cb);li.appendChild(document.createTextNode(' 任务'));ul.appendChild(li);var sel=window.getSelection();if(sel.rangeCount>0){var r=sel.getRangeAt(0);r.insertNode(ul)}else{document.body.appendChild(ul)}})()",
                null
            )
            return
        }
        insertMarkdown("- [ ] ", "")
    }

    fun insertTableRow() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                "(function(){var sel=window.getSelection();if(!sel.rangeCount)return;var n=sel.getRangeAt(0).startContainer;while(n&&n.nodeName!=='TR'&&n.nodeName!=='BODY')n=n.parentElement;if(!n||n.nodeName!=='TR')return;var cs=n.querySelectorAll('td,th');var nr=document.createElement('tr');for(var i=0;i<cs.length;i++){var c=document.createElement(cs[i].nodeName.toLowerCase());c.innerHTML='&nbsp;';nr.appendChild(c)}n.parentNode.insertBefore(nr,n.nextSibling)})()",
                null
            )
            return
        }
        // SOURCE mode: insert a new row in markdown table at cursor
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val rowSep = "\n| "
        val before = text.substring(0, pos)
        val after = text.substring(pos)
        pushUndo(text)
        _markdownText.value = before + rowSep + after
        _cursorPosition = pos + rowSep.length
    }

    fun insertTableColumn() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                "(function(){var sel=window.getSelection();if(!sel.rangeCount)return;var n=sel.getRangeAt(0).startContainer;while(n&&n.nodeName!=='TD'&&n.nodeName!=='TH'&&n.nodeName!=='BODY')n=n.parentElement;if(!n||n.nodeName!=='TD'&&n.nodeName!=='TH')return;var ci=Array.prototype.indexOf.call(n.parentElement.children,n);var tbl=n;while(tbl&&tbl.nodeName!=='TABLE')tbl=tbl.parentElement;if(!tbl)return;var rows=tbl.querySelectorAll('tr');for(var i=0;i<rows.length;i++){var cs=rows[i].querySelectorAll('td,th');if(cs.length<=ci)continue;var nc=document.createElement(cs[0].nodeName.toLowerCase());nc.innerHTML='&nbsp;';if(cs[ci])cs[ci].parentElement.insertBefore(nc,cs[ci].nextSibling)}})()",
                null
            )
            return
        }
        // SOURCE mode: add column to markdown table
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val before = text.substring(0, pos)
        val after = text.substring(pos)
        pushUndo(text)
        _markdownText.value = before + " | " + after
        _cursorPosition = pos + 3
    }

    fun deleteTableRow() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                "(function(){var sel=window.getSelection();if(!sel.rangeCount)return;var n=sel.getRangeAt(0).startContainer;while(n&&n.nodeName!=='TR'&&n.nodeName!=='TBODY'&&n.nodeName!=='THEAD'&&n.nodeName!=='TABLE'&&n.nodeName!=='BODY')n=n.parentElement;if(!n||n.nodeName!=='TR')return;var tbl=n.parentElement;n.parentElement.removeChild(n);if(tbl&&tbl.nodeName==='TBODY'&&!tbl.querySelector('tr')){tbl.removeChild(tbl.querySelector('tr')||tbl);}})()",
                null
            )
            return
        }
    }

    fun deleteTableColumn() {
        val wv = webViewRef?.get()
        if (wv != null) {
            wv.evaluateJavascript(
                "(function(){var sel=window.getSelection();if(!sel.rangeCount)return;var n=sel.getRangeAt(0).startContainer;while(n&&n.nodeName!=='TD'&&n.nodeName!=='TH'&&n.nodeName!=='BODY')n=n.parentElement;if(!n||(n.nodeName!=='TD'&&n.nodeName!=='TH'))return;var ci=Array.prototype.indexOf.call(n.parentElement.children,n);var tbl=n;while(tbl&&tbl.nodeName!=='TABLE')tbl=tbl.parentElement;if(!tbl)return;var rows=tbl.querySelectorAll('tr');for(var i=0;i<rows.length;i++){var cs=rows[i].querySelectorAll('td,th');if(cs.length>ci)cs[ci].parentElement.removeChild(cs[ci])}})()",
                null
            )
            return
        }
    }

    private fun jsEsc(s: String): String = s.replace("\"", "\\\"").replace("'", "\\'")

    fun showExportDialog() {
        _showExportDialog.value = true
    }

    fun dismissExportDialog() {
        _showExportDialog.value = false
    }

    fun exportAsPdf() {
        val name = currentFileName.ifBlank { "未命名" }
        val wv = webViewRef?.get()
        if (wv != null) {
            exportManager.exportToPdfFromWebView(wv, name, context) { msg, uri ->
                _exportMessage.value = msg
                _lastExportUri.value = uri
                _showShareDialog.value = uri != null
            }
        } else {
            exportManager.exportToPdf(_markdownText.value, name, context) { msg, uri ->
                _exportMessage.value = msg
                _lastExportUri.value = uri
                _showShareDialog.value = uri != null
            }
        }
    }

    fun exportAsHtml() {
        val name = currentFileName.ifBlank { "未命名" }
        exportManager.exportToHtml(_markdownText.value, name, context) { msg, uri ->
            _exportMessage.value = msg
            _lastExportUri.value = uri
            _showShareDialog.value = uri != null
        }
    }

    fun exportAsTxt() {
        val name = currentFileName.ifBlank { "未命名" }
        exportManager.exportToTxt(_markdownText.value, name, context) { msg, uri ->
            _exportMessage.value = msg
            _lastExportUri.value = uri
            _showShareDialog.value = uri != null
        }
    }

    fun exportAsMd() {
        val path = currentFilePath
        if (path != null) {
            val success = exportManager.shareMarkdownFile(path, context)
            if (!success) {
                _exportMessage.value = "分享失败：文件不存在"
            }
        } else {
            val name = currentFileName.ifBlank { "未命名" }
            try {
                val tempFile = File(context.cacheDir, "$name.md")
                tempFile.writeText(_markdownText.value)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/markdown"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "分享 Markdown 文件")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                _exportMessage.value = "分享失败: ${e.message}"
            }
        }
    }

    fun exportAsImage() {
        val name = currentFileName.ifBlank { "未命名" }
        val wv = webViewRef?.get()
        if (wv != null) {
            exportManager.exportToImageFromWebView(wv, name, context) { msg, uri ->
                _exportMessage.value = msg
                _lastExportUri.value = uri
                _showShareDialog.value = uri != null
            }
        } else {
            exportManager.exportToImage(_markdownText.value, name, context) { msg, uri ->
                _exportMessage.value = msg
                _lastExportUri.value = uri
                _showShareDialog.value = uri != null
            }
        }
    }

    fun shareExportedFile() {
        val uri = _lastExportUri.value ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "分享导出文件")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
        _showShareDialog.value = false
    }

    fun dismissShareDialog() {
        _showShareDialog.value = false
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    fun getExportFormats(): List<ExportFormat> {
        val name = currentFileName.ifBlank { "未命名" }
        return listOf(
            ExportFormat("PDF", "$name.pdf"),
            ExportFormat("HTML", "$name.html"),
            ExportFormat("图片 PNG", "$name.png"),
            ExportFormat("Markdown", "$name.md"),
            ExportFormat("纯文本 TXT", "$name.txt")
        )
    }

    fun insertNewLine() {
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val beforeCursor = text.substring(0, pos)
        val afterCursor = text.substring(pos)
        pushUndo(text)
        _markdownText.value = beforeCursor + "\n" + afterCursor
        _cursorPosition = pos + 1
    }

    fun increaseHeadingLevel() {
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val currentLineStart = text.lastIndexOf('\n', pos - 1) + 1
        val currentLineEnd = text.indexOf('\n', pos).let { if (it == -1) text.length else it }
        val currentLine = text.substring(currentLineStart, currentLineEnd)
        val headingMatch = Regex("^(#{1,6})\\s*(.*)$").find(currentLine)
        if (headingMatch != null) {
            val currentLevel = headingMatch.groupValues[1].length
            if (currentLevel < 6) {
                val newLine = "#".repeat(currentLevel + 1) + " " + headingMatch.groupValues[2]
                pushUndo(text)
                _markdownText.value = text.substring(0, currentLineStart) + newLine + text.substring(currentLineEnd)
                _cursorPosition = currentLineStart + newLine.length
            }
        } else {
            val newLine = "# $currentLine"
            pushUndo(text)
            _markdownText.value = text.substring(0, currentLineStart) + newLine + text.substring(currentLineEnd)
            _cursorPosition = currentLineStart + newLine.length
        }
    }

    fun decreaseHeadingLevel() {
        val text = _markdownText.value
        val pos = minOf(_cursorPosition, text.length)
        val currentLineStart = text.lastIndexOf('\n', pos - 1) + 1
        val currentLineEnd = text.indexOf('\n', pos).let { if (it == -1) text.length else it }
        val currentLine = text.substring(currentLineStart, currentLineEnd)
        val headingMatch = Regex("^(#{1,6})\\s*(.*)$").find(currentLine)
        if (headingMatch != null) {
            val currentLevel = headingMatch.groupValues[1].length
            if (currentLevel > 1) {
                val newLine = "#".repeat(currentLevel - 1) + " " + headingMatch.groupValues[2]
                pushUndo(text)
                _markdownText.value = text.substring(0, currentLineStart) + newLine + text.substring(currentLineEnd)
                _cursorPosition = currentLineStart + newLine.length
            } else {
                val newLine = headingMatch.groupValues[2]
                pushUndo(text)
                _markdownText.value = text.substring(0, currentLineStart) + newLine + text.substring(currentLineEnd)
                _cursorPosition = currentLineStart + newLine.length
            }
        }
    }
}

data class ExportFormat(val formatName: String, val fileName: String)
