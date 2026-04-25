package com.markdowneditor.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.markdowneditor.viewModel.EditorViewModel

@Composable
fun EditorScreen(
    fileName: String,
    viewModel: EditorViewModel = EditorViewModel()
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600
    
    // 平板电脑默认使用分屏模式
    var viewMode by remember { mutableStateOf(if (isTablet) EditorViewMode.SPLIT else EditorViewMode.PREVIEW) }

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
