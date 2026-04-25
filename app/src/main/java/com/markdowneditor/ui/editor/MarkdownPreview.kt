package com.markdowneditor.ui.editor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.markdowneditor.utils.MarkdownRenderer

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val renderer = MarkdownRenderer()
    val html = renderer.render(markdown)
    
    // 简化实现，实际应用中需要使用WebView或类似组件渲染HTML
    Text(
        text = "预览: $markdown",
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Start
    )
}
