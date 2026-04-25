package com.markdowneditor.viewModel

import android.content.Context
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel

class EditorViewModel : ViewModel() {
    private var _markdownText = ""
    val markdownText: String get() = _markdownText
    private var _cursorPosition = 0
    val cursorPosition: Int get() = _cursorPosition
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    fun updateText(text: String) {
        _markdownText = text
    }

    fun setCursorPosition(position: Int) {
        _cursorPosition = position
    }

    // 插入Markdown格式
    fun insertMarkdown(prefix: String, suffix: String) {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        _markdownText = beforeCursor + prefix + suffix + afterCursor
        _cursorPosition += prefix.length
    }

    // 插入标题
    fun insertHeading(level: Int) {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val headingPrefix = "#".repeat(level) + " "
        _markdownText = beforeCursor + headingPrefix + afterCursor
        _cursorPosition += headingPrefix.length
    }

    // 插入代码块
    fun insertCodeBlock(language: String = "") {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val codeBlock = "```$language\n\n```"
        _markdownText = beforeCursor + codeBlock + afterCursor
        _cursorPosition += "```$language\n".length
    }

    // 插入表格
    fun insertTable(rows: Int = 2, columns: Int = 2) {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        
        val table = buildString {
            // 表头
            append("| ".repeat(columns)).append("|\n")
            // 分隔线
            append("| -".repeat(columns)).append("|\n")
            // 数据行
            repeat(rows) {
                append("| ".repeat(columns)).append("|\n")
            }
        }
        
        _markdownText = beforeCursor + table + afterCursor
        _cursorPosition += table.indexOf("\n") + 1
    }

    // 插入数学公式
    fun insertMathBlock() {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val mathBlock = "$$\n\n$$"
        _markdownText = beforeCursor + mathBlock + afterCursor
        _cursorPosition += "$$\n".length
    }

    // 插入引用
    fun insertQuote() {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val quote = "> "
        _markdownText = beforeCursor + quote + afterCursor
        _cursorPosition += quote.length
    }

    // 插入无序列表
    fun insertUnorderedList() {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val listItem = "- "
        _markdownText = beforeCursor + listItem + afterCursor
        _cursorPosition += listItem.length
    }

    // 插入有序列表
    fun insertOrderedList() {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val listItem = "1. "
        _markdownText = beforeCursor + listItem + afterCursor
        _cursorPosition += listItem.length
    }

    // 插入链接
    fun insertLink() {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val link = "[链接文本](https://example.com)"
        _markdownText = beforeCursor + link + afterCursor
        _cursorPosition += "[链接文本]".length
    }

    // 插入图片
    fun insertImage() {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val image = "![图片描述](https://example.com/image.jpg)"
        _markdownText = beforeCursor + image + afterCursor
        _cursorPosition += "![图片描述]".length
    }

    // 插入分隔线
    fun insertHorizontalRule() {
        val beforeCursor = _markdownText.substring(0, _cursorPosition)
        val afterCursor = _markdownText.substring(_cursorPosition)
        val rule = "\n---\n"
        _markdownText = beforeCursor + rule + afterCursor
        _cursorPosition += rule.length
    }

    // 开始语音输入
    fun startVoiceInput(context: Context, onError: (String) -> Unit) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "开始说话...")
            }
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    isListening = false
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "音频错误"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                        SpeechRecognizer.ERROR_NO_MATCH -> "无匹配结果"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
                        else -> "未知错误"
                    }
                    onError(errorMessage)
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let { text ->
                        val beforeCursor = _markdownText.substring(0, _cursorPosition)
                        val afterCursor = _markdownText.substring(_cursorPosition)
                        _markdownText = beforeCursor + text + afterCursor
                        _cursorPosition += text.length
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            speechRecognizer?.startListening(intent)
            isListening = true
        } else {
            onError("语音输入不可用")
        }
    }

    // 停止语音输入
    fun stopVoiceInput() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }

    // 检查是否正在语音输入
    fun isVoiceInputActive(): Boolean {
        return isListening
    }

    // 清理资源
    override fun onCleared() {
        super.onCleared()
        stopVoiceInput()
    }
}
