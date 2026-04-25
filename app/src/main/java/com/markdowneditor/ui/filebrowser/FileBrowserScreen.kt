package com.markdowneditor.ui.filebrowser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Markdown Editor") }
        )
        
        if (isTablet) {
            // 平板电脑使用网格布局
            LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(files.value) {
                    FileItem(file = it, isTablet = true)
                }
            }
        } else {
            // 手机使用列表布局
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(files.value) {
                    FileItem(file = it, isTablet = false)
                }
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
fun FileItem(file: MarkdownFile, isTablet: Boolean) {
    Card(
        modifier = if (isTablet) {
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(120.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        }
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
