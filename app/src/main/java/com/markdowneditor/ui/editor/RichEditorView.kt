package com.markdowneditor.ui.editor

import android.webkit.JavascriptInterface
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.markdowneditor.utils.HtmlToMarkdown
import com.markdowneditor.utils.MarkdownRenderer
import java.util.concurrent.atomic.AtomicInteger

@Composable
fun RichEditorView(
    markdownText: String,
    onTextChange: (String) -> Unit,
    cursorPosition: Int,
    onCursorPositionChange: (Int) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onWebViewReady: ((WebView) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val renderer = remember { MarkdownRenderer() }

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

    val styles = """
* { box-sizing: border-box; margin: 0; padding: 0; }

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
    background: $bgColor;
    color: $textColor;
    line-height: 1.75;
    padding: 20px 16px 200px 16px;
    font-size: 15px;
    -webkit-font-smoothing: antialiased;
    outline: none;
    -webkit-tap-highlight-color: transparent;
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
""".trimIndent()

    fun buildHtml(content: String): String = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta http-equiv="Content-Security-Policy" content="default-src 'self' file:; script-src 'unsafe-inline' 'self' file:; style-src 'unsafe-inline' 'self'; img-src 'self' file: content: data: https:; connect-src 'self';">
<style>
$styles
</style>
</head>
<body contenteditable="true">
$content
<p><br></p>
</body>
<script>
var debounceTimer = null;
var observer = new MutationObserver(function() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(function() {
        MdownBridge.onContentChanged(document.body.innerHTML);
    }, 80);
});
observer.observe(document.body, { childList: true, subtree: true, characterData: true });

function insertImageAtCursor(src, alt) {
    var img = document.createElement('img');
    img.src = src;
    img.alt = alt || '';
    img.setAttribute('data-mdown-image', '1');
    var sel = window.getSelection();
    if (sel.rangeCount > 0) {
        var range = sel.getRangeAt(0);
        range.insertNode(img);
        range.setStartAfter(img);
        range.collapse(true);
        sel.removeAllRanges();
        sel.addRange(range);
    } else {
        document.body.appendChild(img);
    }
    clearTimeout(debounceTimer);
    MdownBridge.onContentChanged(document.body.innerHTML);
}

document.addEventListener('selectionchange', function() {
    var sel = window.getSelection();
    if (sel && sel.rangeCount > 0) {
        MdownBridge.onSelectionChanged(
            sel.anchorOffset,
            sel.focusOffset
        );
    }
});
</script>
</html>
""".trimIndent()

    val programmaticLoadCount = remember { AtomicInteger(0) }
    val lastKnownMarkdown = remember { mutableStateOf(markdownText) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isReady by remember { mutableStateOf(false) }

    // Initial HTML to load in factory
    val initialHtml = remember { buildHtml(renderer.render(markdownText)) }

    // Sync external markdown changes -> WebView
    LaunchedEffect(markdownText, isReady) {
        if (!isReady) return@LaunchedEffect
        if (markdownText == lastKnownMarkdown.value) return@LaunchedEffect
        lastKnownMarkdown.value = markdownText
        val wv = webView ?: return@LaunchedEffect
        programmaticLoadCount.incrementAndGet()
        val html = renderer.render(markdownText)
        wv.loadDataWithBaseURL("file:///", buildHtml(html), "text/html", "UTF-8", null)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onContentChanged(html: String) {
                        if (programmaticLoadCount.get() > 0) {
                            programmaticLoadCount.decrementAndGet()
                            return
                        }
                        val md = HtmlToMarkdown.convert(html)
                        if (md == lastKnownMarkdown.value) return
                        lastKnownMarkdown.value = md
                        post {
                            onTextChange(md)
                        }
                    }

                    @JavascriptInterface
                    fun onSelectionChanged(anchorOffset: Int, focusOffset: Int) {
                        post {
                            onSelectionChange(anchorOffset, focusOffset)
                            onCursorPositionChange(focusOffset)
                        }
                    }
                }, "MdownBridge")

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url ?: return null
                        if (url.scheme == "content") {
                            try {
                                val mimeType = ctx.contentResolver.getType(url) ?: "image/*"
                                val inputStream = ctx.contentResolver.openInputStream(url)
                                return WebResourceResponse(mimeType, "UTF-8", inputStream)
                            } catch (_: Exception) {
                            }
                        }
                        return null
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isReady = true
                    }
                }

                settings.apply {
                    javaScriptEnabled = true
                    allowContentAccess = true
                    allowFileAccess = true
                    @Suppress("DEPRECATION")
                    allowUniversalAccessFromFileURLs = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(false)
                    blockNetworkImage = false
                }

                loadDataWithBaseURL("file:///", initialHtml, "text/html", "UTF-8", null)
                onWebViewReady?.invoke(this)
            }
        },
        update = { wv ->
            if (webView == null) {
                webView = wv
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
