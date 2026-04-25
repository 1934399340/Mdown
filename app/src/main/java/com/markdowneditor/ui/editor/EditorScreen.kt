package com.markdowneditor.ui.editor

import android.view.KeyEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.markdowneditor.viewModel.EditorViewModel

@Composable
fun EditorScreen(
    fileName: String,
    viewModel: EditorViewModel = EditorViewModel()
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    
    // 平板电脑默认使用分屏模式
    var viewMode by remember { mutableStateOf(if (isTablet) EditorViewMode.SPLIT else EditorViewMode.PREVIEW) }

    // 键盘事件处理
    DisposableEffect(Unit) {
        val keyEventHandler = android.view.View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when {
                    // 格式快捷键
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_B -> {
                        viewModel.insertMarkdown("**", "**")
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_I -> {
                        viewModel.insertMarkdown("*", "*")
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_U -> {
                        viewModel.insertMarkdown("<u>", "</u>")
                        return@OnKeyListener true
                    }
                    event.isAltPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_5 -> {
                        viewModel.insertMarkdown("~~", "~~")
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_GRAVE -> {
                        viewModel.insertMarkdown("`", "`")
                        return@OnKeyListener true
                    }
                    
                    // 标题快捷键
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_1 -> {
                        viewModel.insertHeading(1)
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_2 -> {
                        viewModel.insertHeading(2)
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_3 -> {
                        viewModel.insertHeading(3)
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_4 -> {
                        viewModel.insertHeading(4)
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_5 -> {
                        viewModel.insertHeading(5)
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_6 -> {
                        viewModel.insertHeading(6)
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_0 -> {
                        // 重置为普通段落
                        return@OnKeyListener true
                    }
                    
                    // 其他Markdown元素
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_T -> {
                        viewModel.insertTable()
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_K -> {
                        viewModel.insertCodeBlock()
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_M -> {
                        viewModel.insertMathBlock()
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_Q -> {
                        viewModel.insertQuote()
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_LBRACKET -> {
                        viewModel.insertOrderedList()
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_RBRACKET -> {
                        viewModel.insertUnorderedList()
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_K -> {
                        viewModel.insertLink()
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_I -> {
                        viewModel.insertImage()
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_H -> {
                        viewModel.insertHorizontalRule()
                        return@OnKeyListener true
                    }
                    
                    // 编辑操作
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_Z -> {
                        // 撤销
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_Y -> {
                        // 重做
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_F -> {
                        // 查找
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_H -> {
                        // 替换
                        return@OnKeyListener true
                    }
                    
                    // 视图切换
                    keyCode == KeyEvent.KEYCODE_F8 -> {
                        // 专注模式
                        return@OnKeyListener true
                    }
                    keyCode == KeyEvent.KEYCODE_F11 -> {
                        // 全屏模式
                        return@OnKeyListener true
                    }
                    event.isCtrlPressed && keyCode == KeyEvent.KEYCODE_SLASH -> {
                        // 源码模式
                        return@OnKeyListener true
                    }
                }
            }
            false
        }
        
        view.setOnKeyListener(keyEventHandler)
        onDispose {
            view.setOnKeyListener(null)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(fileName) },
            actions = {
                IconButton(onClick = { /* 实际实现中保存文件 */ }) {
                    Text("保存")
                }
            }
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(
                onClick = { viewMode = EditorViewMode.EDITOR },
                modifier = Modifier.weight(1f)
            ) {
                Text("编辑")
            }
            Button(
                onClick = { viewMode = EditorViewMode.PREVIEW },
                modifier = Modifier.weight(1f)
            ) {
                Text("预览")
            }
            Button(
                onClick = { viewMode = EditorViewMode.SPLIT },
                modifier = Modifier.weight(1f)
            ) {
                Text("分屏")
            }
        }
        
        when (viewMode) {
            EditorViewMode.EDITOR -> {
                MarkdownEditor(
                    text = viewModel.markdownText,
                    onTextChange = { viewModel.updateText(it) },
                    onCursorPositionChange = { viewModel.setCursorPosition(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            EditorViewMode.PREVIEW -> {
                MarkdownPreview(
                    markdown = viewModel.markdownText,
                    modifier = Modifier.fillMaxSize()
                )
            }
            EditorViewMode.SPLIT -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    MarkdownEditor(
                        text = viewModel.markdownText,
                        onTextChange = { viewModel.updateText(it) },
                        onCursorPositionChange = { viewModel.setCursorPosition(it) },
                        modifier = Modifier.weight(1f)
                    )
                    MarkdownPreview(
                        markdown = viewModel.markdownText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

enum class EditorViewMode {
    EDITOR,
    PREVIEW,
    SPLIT
}
