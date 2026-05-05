package com.markdowneditor.ui.editor

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.markdowneditor.ui.theme.AppTheme
import com.markdowneditor.utils.MarkdownRenderer
import com.markdowneditor.utils.SecurityHelper
import kotlinx.coroutines.delay

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier,
    onWebViewReady: ((WebView) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    var pendingUrl by remember { mutableStateOf<String?>(null) }

    val bgColor = if (isDark) "#1A1C19" else "#FBFDF7"
    val textColor = if (isDark) "#E2E3DD" else "#1A1C19"
    val headingColor = if (isDark) "#6BDB73" else "#2E7D32"
    val codeBg = if (isDark) "#2A2D28" else "#F0F4EC"
    val codeColor = if (isDark) "#6BDB73" else "#2E7D32"
    val borderColor = if (isDark) "#414942" else "#DDE5D9"
    val quoteColor = if (isDark) "#6BDB73" else "#2E7D32"
    val linkColor = if (isDark) "#6DDBA8" else "#006C4C"
    val tableHeaderBg = if (isDark) "#2A2D28" else "#F0F4EC"
    val subtleText = if (isDark) "#8B938C" else "#717971"

    val renderer = remember { MarkdownRenderer() }

    var debouncedMarkdown by remember { mutableStateOf(markdown) }
    LaunchedEffect(markdown) {
        delay(100)
        debouncedMarkdown = markdown
    }

    val html = remember(debouncedMarkdown) { renderer.render(debouncedMarkdown) }

    pendingUrl?.let { url ->
        AlertDialog(
            onDismissRequest = { pendingUrl = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "打开链接",
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        "即将在外部浏览器中打开以下链接：",
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            url,
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(AppTheme.spacing.md)
                        )
                    }
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        try {
                            if (SecurityHelper.sanitizeUrl(url)) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        } catch (_: Exception) {
                        }
                        pendingUrl = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("打开", style = AppTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingUrl = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "取消",
                        style = AppTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                        val url = request?.url?.toString() ?: return false
                        pendingUrl = url
                        return true
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?
                    ): android.webkit.WebResourceResponse? {
                        val url = request?.url ?: return null
                        if (url.scheme == "content") {
                            try {
                                val mimeType = ctx.contentResolver.getType(url) ?: "image/*"
                                val inputStream = ctx.contentResolver.openInputStream(url)
                                return android.webkit.WebResourceResponse(mimeType, "UTF-8", inputStream)
                            } catch (_: Exception) {
                            }
                        }
                        return null
                    }
                }
                settings.javaScriptEnabled = false
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                @Suppress("DEPRECATION")
                settings.allowUniversalAccessFromFileURLs = true
                settings.domStorageEnabled = false
                settings.databaseEnabled = false
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(false)
                settings.blockNetworkImage = false
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                onWebViewReady?.invoke(this)
            }
        },
        update = { webView ->
            val fullHtml = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta http-equiv="Content-Security-Policy" content="default-src file: 'self'; style-src 'unsafe-inline' 'self'; img-src 'self' file: content: data: https:;">
<style>
* { box-sizing: border-box; margin: 0; padding: 0; }

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
    background: $bgColor;
    color: $textColor;
    line-height: 1.75;
    padding: 20px 16px 200px 16px;
    font-size: 15px;
    -webkit-font-smoothing: antialiased;
}

h1, h2, h3, h4, h5, h6 {
    color: $headingColor;
    margin-top: 28px;
    margin-bottom: 12px;
    font-weight: 600;
    line-height: 1.4;
}
h1 { font-size: 1.75em; border-bottom: 2px solid $borderColor; padding-bottom: 8px; }
h2 { font-size: 1.45em; border-bottom: 1px solid $borderColor; padding-bottom: 6px; }
h3 { font-size: 1.2em; }
h4 { font-size: 1.05em; }

p { margin-bottom: 14px; }

a { color: $linkColor; text-decoration: none; border-bottom: 1px solid transparent; }
a:hover { border-bottom-color: $linkColor; }

code {
    background: $codeBg;
    color: $codeColor;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
    font-size: 0.88em;
}

pre {
    background: $codeBg;
    padding: 16px;
    border-radius: 10px;
    overflow-x: auto;
    margin: 16px 0;
    border: 1px solid $borderColor;
}
pre code {
    background: transparent;
    padding: 0;
    color: $textColor;
    font-size: 0.85em;
    line-height: 1.6;
}

table {
    border-collapse: collapse;
    width: 100%;
    margin: 16px 0;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid $borderColor;
}
th {
    background: $tableHeaderBg;
    font-weight: 600;
    text-align: left;
    border-right: 1px solid $borderColor;
}
th:last-child { border-right: none; }
th, td {
    padding: 10px 14px;
    border-bottom: 1px solid $borderColor;
    border-right: 1px solid $borderColor;
    min-width: 60px;
    min-height: 24px;
}
td:last-child { border-right: none; }
tr:last-child td { border-bottom: none; }

blockquote {
    border-left: 3px solid $quoteColor;
    padding: 8px 16px;
    margin: 16px 0;
    color: $subtleText;
    background: $codeBg;
    border-radius: 0 8px 8px 0;
}

ul, ol {
    margin: 10px 0 14px 24px;
}
li { margin-bottom: 4px; }

hr {
    border: none;
    height: 1px;
    background: $borderColor;
    margin: 24px 0;
}

img {
    max-width: 100%;
    max-height: 480px;
    object-fit: contain;
    border-radius: 8px;
    margin: 12px 0;
}

del { color: $subtleText; }

.task-list-item { list-style-type: none; margin-left: -20px; }
.task-list-item input[type="checkbox"] { margin-right: 8px; }

.footnote { font-size: 0.85em; color: $subtleText; }
</style>
</head>
<body>
$html
</body>
</html>
            """.trimIndent()
            webView.loadDataWithBaseURL("file:///", fullHtml, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    )
}
