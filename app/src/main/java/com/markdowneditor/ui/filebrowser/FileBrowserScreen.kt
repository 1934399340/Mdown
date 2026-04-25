package com.markdowneditor.ui.filebrowser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markdowneditor.data.model.MarkdownFile
import com.markdowneditor.viewModel.FileBrowserViewModel

@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel = FileBrowserViewModel(
        // 实际实现中需要注入FileRepository
        TODO()
    )
) {
    val files = viewModel.files.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Markdown Editor") }
        )
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(files.value) {
                FileItem(file = it)
            }
        }
        
        FloatingActionButton(
            onClick = { /* 实际实现中打开创建文件对话框 */ },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End)
        ) {
            Text("+")
        }
    }
}

@Composable
fun FileItem(file: MarkdownFile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = file.fileName, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "${file.size} bytes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
