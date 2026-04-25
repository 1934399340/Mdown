package com.markdowneditor.ui.editor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import com.markdowneditor.utils.MarkdownRenderer

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val renderer = MarkdownRenderer()
    val html = renderer.render(markdown)
    
    // 使用WebView渲染HTML，支持高级Markdown元素
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.allowContentAccess = true
                settings.domStorageEnabled = true
            }
        },
        update = { webView ->
            val fullHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        margin: 20px; 
                        line-height: 1.6;
                    }
                    h1, h2, h3, h4, h5, h6 { 
                        color: #333;
                        margin-top: 20px;
                        margin-bottom: 10px;
                    }
                    p { 
                        margin-bottom: 15px;
                    }
                    code { 
                        background: #f4f4f4; 
                        padding: 2px 4px;
                        border-radius: 3px;
                    }
                    pre { 
                        background: #f4f4f4; 
                        padding: 10px;
                        border-radius: 5px;
                        overflow-x: auto;
                    }
                    table { 
                        border-collapse: collapse;
                        width: 100%;
                        margin: 20px 0;
                    }
                    th, td { 
                        border: 1px solid #ddd;
                        padding: 8px;
                        text-align: left;
                    }
                    th { 
                        background-color: #f2f2f2;
                    }
                    blockquote { 
                        border-left: 4px solid #ddd;
                        padding-left: 15px;
                        margin: 15px 0;
                        color: #666;
                    }
                    ul, ol { 
                        margin-left: 20px;
                        margin-bottom: 15px;
                    }
                    li { 
                        margin-bottom: 5px;
                    }
                    .task-list-item { 
                        list-style-type: none;
                    }
                    .task-list-item input[type="checkbox"] { 
                        margin-right: 10px;
                    }
                    del { 
                        text-decoration: line-through;
                        color: #999;
                    }
                </style>
                <!-- MathJax for mathematical formulas -->
                <script src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
            </head>
            <body>
                $html
            </body>
            </html>
            """.trimIndent()
            webView.loadDataWithBaseURL("https://example.com", fullHtml, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}
