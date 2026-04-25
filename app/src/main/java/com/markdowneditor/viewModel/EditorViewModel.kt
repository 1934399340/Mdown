package com.markdowneditor.viewModel

import androidx.lifecycle.ViewModel

class EditorViewModel : ViewModel() {
    private var _markdownText = ""
    val markdownText: String get() = _markdownText

    fun updateText(text: String) {
        _markdownText = text
    }
}
