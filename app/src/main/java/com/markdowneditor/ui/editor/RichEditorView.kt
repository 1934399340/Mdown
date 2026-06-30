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
    onShortcut: ((String) -> Unit)? = null,
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
    display: block;
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
    }, 250);
});
observer.observe(document.body, { childList: true, subtree: true, characterData: true });

var selectionTimer = null;
document.addEventListener('selectionchange', function() {
    clearTimeout(selectionTimer);
    selectionTimer = setTimeout(function() {
        var sel = window.getSelection();
        if (sel && sel.rangeCount > 0) {
            MdownBridge.onSelectionChanged(
                sel.anchorOffset,
                sel.focusOffset
            );
        }
    }, 400);
});

// DOM 操作辅助函数（替代已废弃的 document.execCommand）
function MdownWrapTag(tag) {
    document.body.focus();
    var s = window.getSelection();
    if (!s.rangeCount) return;
    var r = s.getRangeAt(0);
    if (r.collapsed) return;
    var e = r.commonAncestorContainer;
    while (e && e.nodeType !== 1) e = e.parentElement;
    var w = e ? e.closest(tag) : null;
    if (w) {
        var f = document.createDocumentFragment(), c;
        while (c = w.firstChild) f.appendChild(c);
        w.parentNode.replaceChild(f, w);
        r.selectNodeContents(f);
    } else {
        var n = document.createElement(tag);
        n.appendChild(r.extractContents());
        r.insertNode(n);
        r.selectNodeContents(n);
    }
    s.removeAllRanges();
    s.addRange(r);
}

function MdownHeading(level) {
    document.body.focus();
    var s = window.getSelection();
    if (!s.rangeCount) return;
    var n = s.getRangeAt(0).startContainer;
    while (n && n.nodeType !== 1) n = n.parentElement;
    var bl = null, t = n;
    while (t && t !== document.body) {
        if (/^(P|DIV|H[1-6]|LI|BLOCKQUOTE|PRE|TD|TH)$/.test(t.nodeName)) { bl = t; break; }
        t = t.parentElement;
    }
    if (!bl) return;
    var tag = 'H' + level;
    if (bl.nodeName === tag) return;
    var nh = document.createElement(tag);
    while (bl.firstChild) nh.appendChild(bl.firstChild);
    bl.parentNode.replaceChild(nh, bl);
    var nr = document.createRange();
    nr.selectNodeContents(nh);
    s.removeAllRanges();
    s.addRange(nr);
}

function MdownInsertList(type) {
    document.body.focus();
    var s = window.getSelection();
    if (!s.rangeCount) return;
    var r = s.getRangeAt(0);
    var sel = r.collapsed ? null : r.extractContents();
    var list = document.createElement(type);
    var li = document.createElement('li');
    if (sel && sel.textContent.trim()) { li.appendChild(sel); }
    else { li.innerHTML = '&nbsp;'; }
    list.appendChild(li);
    r.insertNode(list);
    r.setStartAfter(li.firstChild || li);
    r.collapse(true);
    s.removeAllRanges();
    s.addRange(r);
}

function MdownInsertQuote() {
    document.body.focus();
    var sel = window.getSelection();
    if (!sel.rangeCount) return;
    var r = sel.getRangeAt(0), bq = document.createElement('blockquote');
    if (r.collapsed) {
        var p = document.createElement('p');
        p.appendChild(document.createElement('br'));
        bq.appendChild(p);
        r.insertNode(bq);
    } else {
        bq.appendChild(r.extractContents());
        r.insertNode(bq);
    }
}

function MdownInsertHR() {
    document.body.focus();
    var s = window.getSelection();
    if (!s.rangeCount) return;
    var r = s.getRangeAt(0);
    var hr = document.createElement('hr');
    r.insertNode(hr);
    r.setStartAfter(hr);
    r.collapse(true);
    s.removeAllRanges();
    s.addRange(r);
}

function MdownInsertCodeBlock() {
    document.body.focus();
    var pre = document.createElement('pre');
    var code = document.createElement('code');
    code.textContent = '​';
    pre.appendChild(code);
    var sel = window.getSelection();
    if (sel.rangeCount > 0) {
        var r = sel.getRangeAt(0);
        if (r.collapsed) { r.insertNode(pre); }
        else { r.deleteContents(); r.insertNode(pre); }
        r.setStartAfter(code);
        r.collapse(true);
        sel.removeAllRanges();
        sel.addRange(r);
    } else {
        document.body.appendChild(pre);
    }
}

// Tab 键在表格单元格间导航
function MdownTabInTable(forward) {
    var s = window.getSelection();
    if (!s.rangeCount) return false;
    var n = s.getRangeAt(0).startContainer;
    while (n && n.nodeType !== 1) n = n.parentElement;
    var cell = n ? n.closest('td,th') : null;
    if (!cell) return false;
    var row = cell.parentElement;
    var cells = row.querySelectorAll('td,th');
    var idx = Array.prototype.indexOf.call(cells, cell);
    var target = null;
    if (!forward) {
        if (idx > 0) {
            target = cells[idx - 1];
        } else {
            var prevRow = row.previousElementSibling;
            if (prevRow && prevRow.nodeName === 'TR') {
                var pc = prevRow.querySelectorAll('td,th');
                if (pc.length) target = pc[pc.length - 1];
            }
        }
    } else {
        if (idx < cells.length - 1) {
            target = cells[idx + 1];
        } else {
            var nextRow = row.nextElementSibling;
            if (!nextRow || nextRow.nodeName !== 'TR') {
                // 在末尾新建一行
                var body = row.parentElement;
                nextRow = document.createElement('tr');
                for (var i = 0; i < cells.length; i++) {
                    var nc = document.createElement(cells[i].nodeName.toLowerCase());
                    nc.innerHTML = '&nbsp;';
                    nextRow.appendChild(nc);
                }
                body.appendChild(nextRow);
            }
            var nc2 = nextRow.querySelectorAll('td,th');
            if (nc2.length) target = nc2[0];
        }
    }
    if (target) {
        target.focus();
        var r2 = document.createRange();
        r2.selectNodeContents(target);
        r2.collapse(true);
        s.removeAllRanges();
        s.addRange(r2);
    }
    return true;
}

// 键盘快捷键处理（在 WebView 内拦截，解决小米工作台模式按键路由问题）
document.addEventListener('keydown', function(e) {
    // Tab 键表格导航
    if (e.key === 'Tab' && !e.ctrlKey && !e.metaKey) {
        if (MdownTabInTable(!e.shiftKey)) { e.preventDefault(); return; }
        return;
    }

    if (!e.ctrlKey && !e.metaKey) return;
    var handled = true;
    if (e.shiftKey) {
        switch (e.key) {
            case 'K': case 'k': MdownInsertCodeBlock(); break;
            case 'M': case 'm': MdownBridge.onShortcut('mathBlock'); break;
            case 'I': case 'i': MdownBridge.onShortcut('insertImage'); break;
            case 'U': case 'u': MdownWrapTag('del'); break;
            default: handled = false;
        }
    } else {
        switch (e.key) {
            case 'B': case 'b': MdownWrapTag('strong'); break;
            case 'I': case 'i': MdownWrapTag('em'); break;
            case 'D': case 'd': MdownWrapTag('del'); break;
            case 'U': case 'u': MdownWrapTag('u'); break;
            case 'S': case 's': MdownBridge.onShortcut('save'); break;
            case 'Z': case 'z': MdownBridge.onShortcut('undo'); break;
            case 'K': case 'k': MdownBridge.onShortcut('insertLink'); break;
            case 'L': case 'l': MdownInsertList('ul'); break;
            case 'O': case 'o': MdownInsertList('ol'); break;
            case 'Q': case 'q': MdownInsertQuote(); break;
            case 'T': case 't': MdownBridge.onShortcut('insertTable'); break;
            case 'H': case 'h': MdownInsertHR(); break;
            case '/': MdownInsertCodeBlock(); break;
            case 'Enter': MdownBridge.onShortcut('newLine'); break;
            case '[': MdownBridge.onShortcut('decreaseHeading'); break;
            case ']': MdownBridge.onShortcut('increaseHeading'); break;
            case '1': MdownHeading(1); break;
            case '2': MdownHeading(2); break;
            case '3': MdownHeading(3); break;
            case '4': MdownHeading(4); break;
            case '5': MdownHeading(5); break;
            case '6': MdownHeading(6); break;
            default: handled = false;
        }
    }
    if (handled) e.preventDefault();
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

                    @JavascriptInterface
                    fun onShortcut(action: String) {
                        post {
                            onShortcut?.invoke(action)
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
                    domStorageEnabled = false
                    databaseEnabled = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(false)
                    blockNetworkImage = false
                    mediaPlaybackRequiresUserGesture = true
                }
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false

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
