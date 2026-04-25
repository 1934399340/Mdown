package com.markdowneditor.viewModel

import androidx.lifecycle.ViewModel

class EditorViewModel : ViewModel() {
    private var _markdownText = ""
    val markdownText: String get() = _markdownText
    private var _cursorPosition = 0
    val cursorPosition: Int get() = _cursorPosition

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
            append("| ".repeat(columns)).append("|
")
            // 分隔线
            append("| -".repeat(columns)).append("|
")
            // 数据行
            repeat(rows) {
                append("| ".repeat(columns)).append("|
")
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
}
