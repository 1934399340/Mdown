package com.markdowneditor.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.webkit.WebView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class ExportManager(private val markdownRenderer: MarkdownRenderer) {

    private fun saveViaMediaStore(
        context: Context,
        fileName: String,
        mimeType: String,
        writeContent: (java.io.OutputStream) -> Unit
    ): Uri? {
        val safeFileName = SecurityHelper.sanitizeFileName(fileName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val relativePath = "Download/Mdown"
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, safeFileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null
            resolver.openOutputStream(uri)?.use { os -> writeContent(os) }
            return uri
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Mdown"
            )
            if (!dir.exists()) dir.mkdirs()
            val outFile = File(dir, safeFileName)
            FileOutputStream(outFile).use { fos -> writeContent(fos) }
            return getUriForFile(context, outFile)
        }
    }

    fun exportToPdf(markdown: String, fileName: String, context: Context, onResult: (String, Uri?) -> Unit) {
        try {
            val pageWidth = 595
            val pageHeight = 842
            val marginX = 40
            val marginY = 50
            val textWidth = pageWidth - marginX * 2

            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1A1C19")
                textSize = 11f
            }
            val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2E7D32")
                isFakeBoldText = true
            }
            val codePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2E7D32")
                textSize = 10f
            }
            val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#666666")
                textSize = 11f
            }

            val parsedLines = parseMarkdownLines(markdown)

            val document = PdfDocument()
            var currentPage = 0
            var currentY = marginY.toFloat()
            var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage + 1).create())

            for (parsedLine in parsedLines) {
                if (parsedLine.type == ParsedLine.Type.EMPTY) {
                    currentY += parsedLine.spacing
                    if (currentY > pageHeight - marginY) {
                        document.finishPage(page)
                        currentPage++
                        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage + 1).create())
                        currentY = marginY.toFloat()
                    }
                    continue
                }

                val linePaint = when (parsedLine.type) {
                    ParsedLine.Type.HEADING1 -> headingPaint.apply { textSize = 20f }
                    ParsedLine.Type.HEADING2 -> headingPaint.apply { textSize = 17f }
                    ParsedLine.Type.HEADING3 -> headingPaint.apply { textSize = 15f }
                    ParsedLine.Type.HEADING4 -> headingPaint.apply { textSize = 13f }
                    ParsedLine.Type.HEADING5 -> headingPaint.apply { textSize = 12f }
                    ParsedLine.Type.HEADING6 -> headingPaint.apply { textSize = 11f }
                    ParsedLine.Type.CODE, ParsedLine.Type.CODE_BLOCK -> codePaint
                    ParsedLine.Type.QUOTE -> quotePaint
                    else -> bodyPaint
                }

                val staticLayout = StaticLayout.Builder
                    .obtain(parsedLine.text, 0, parsedLine.text.length, linePaint, textWidth)
                    .setLineSpacing(2f, 1f)
                    .build()

                val lineHeight = staticLayout.height.toFloat() + parsedLine.spacing
                if (currentY + lineHeight > pageHeight - marginY) {
                    document.finishPage(page)
                    currentPage++
                    page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage + 1).create())
                    currentY = marginY.toFloat()
                }

                val canvas = page.canvas
                canvas.save()
                canvas.translate(marginX.toFloat(), currentY)
                staticLayout.draw(canvas)
                canvas.restore()
                currentY += lineHeight
            }

            document.finishPage(page)

            val uri = saveViaMediaStore(context, "$fileName.pdf", "application/pdf") { os ->
                document.writeTo(os)
            }
            document.close()

            onResult("PDF 已导出到 Download/Mdown/$fileName.pdf", uri)
        } catch (e: Exception) {
            onResult("导出PDF失败: ${e.message}", null)
        }
    }

    fun exportToImage(markdown: String, fileName: String, context: Context, onResult: (String, Uri?) -> Unit) {
        try {
            val imageWidth = 800
            val marginX = 40
            val marginY = 40
            val textWidth = imageWidth - marginX * 2

            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1A1C19")
                textSize = 28f
            }
            val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2E7D32")
                isFakeBoldText = true
            }
            val codePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2E7D32")
                textSize = 24f
            }
            val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#666666")
                textSize = 28f
            }

            val parsedLines = parseMarkdownLines(markdown)
            val layouts = mutableListOf<Pair<StaticLayout, Float>>()

            var currentY = marginY.toFloat()

            for (parsedLine in parsedLines) {
                if (parsedLine.type == ParsedLine.Type.EMPTY) {
                    currentY += parsedLine.spacing
                    continue
                }

                val linePaint: TextPaint = when (parsedLine.type) {
                    ParsedLine.Type.HEADING1 -> TextPaint(headingPaint).apply { textSize = 50f }
                    ParsedLine.Type.HEADING2 -> TextPaint(headingPaint).apply { textSize = 44f }
                    ParsedLine.Type.HEADING3 -> TextPaint(headingPaint).apply { textSize = 38f }
                    ParsedLine.Type.HEADING4 -> TextPaint(headingPaint).apply { textSize = 34f }
                    ParsedLine.Type.HEADING5 -> TextPaint(headingPaint).apply { textSize = 30f }
                    ParsedLine.Type.HEADING6 -> TextPaint(headingPaint).apply { textSize = 28f }
                    ParsedLine.Type.CODE, ParsedLine.Type.CODE_BLOCK -> TextPaint(codePaint)
                    ParsedLine.Type.QUOTE -> TextPaint(quotePaint)
                    else -> TextPaint(bodyPaint)
                }

                val staticLayout = StaticLayout.Builder
                    .obtain(parsedLine.text, 0, parsedLine.text.length, linePaint, textWidth)
                    .setLineSpacing(4f, 1f)
                    .build()

                layouts.add(staticLayout to currentY)
                currentY += staticLayout.height + parsedLine.spacing
            }

            val imageHeight = (currentY + marginY).toInt().coerceAtLeast(100)

            val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            for ((staticLayout, y) in layouts) {
                canvas.save()
                canvas.translate(marginX.toFloat(), y)
                staticLayout.draw(canvas)
                canvas.restore()
            }

            val uri = saveViaMediaStore(context, "$fileName.png", "image/png") { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            bitmap.recycle()

            onResult("图片已导出到 Download/Mdown/$fileName.png", uri)
        } catch (e: Exception) {
            onResult("导出图片失败: ${e.message}", null)
        }
    }

    fun exportToHtml(markdown: String, fileName: String, context: Context, onResult: (String, Uri?) -> Unit) {
        try {
            val html = markdownRenderer.render(markdown)
            val fullHtml = buildStyledHtml(html)

            val uri = saveViaMediaStore(context, "$fileName.html", "text/html") { os ->
                os.write(fullHtml.toByteArray(Charsets.UTF_8))
            }

            onResult("HTML 已导出到 Download/Mdown/$fileName.html", uri)
        } catch (e: Exception) {
            onResult("导出HTML失败: ${e.message}", null)
        }
    }

    fun exportToTxt(markdown: String, fileName: String, context: Context, onResult: (String, Uri?) -> Unit) {
        try {
            val uri = saveViaMediaStore(context, "$fileName.txt", "text/plain") { os ->
                os.write(markdown.toByteArray(Charsets.UTF_8))
            }

            onResult("TXT 已导出到 Download/Mdown/$fileName.txt", uri)
        } catch (e: Exception) {
            onResult("导出TXT失败: ${e.message}", null)
        }
    }

    fun exportToImageFromWebView(webView: WebView, fileName: String, context: Context, onResult: (String, Uri?) -> Unit) {
        try {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                try {
                    // Give WebView time to finish rendering images
                    handler.postDelayed({
                        try {
                            val contentHeight = (webView.contentHeight * webView.resources.displayMetrics.density).toInt()
                            val width = webView.width.coerceAtLeast(1)
                            val height = contentHeight.coerceAtLeast(webView.height).coerceAtLeast(1)

                            webView.measure(
                                android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
                                android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
                            )
                            webView.layout(0, 0, width, height)

                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            webView.draw(canvas)

                            val uri = saveViaMediaStore(context, "$fileName.png", "image/png") { os ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                            }
                            bitmap.recycle()

                            onResult("图片已导出到 Download/Mdown/$fileName.png", uri)
                        } catch (e: Exception) {
                            onResult("导出图片失败: ${e.message}", null)
                        }
                    }, 500)
                } catch (e: Exception) {
                    onResult("导出图片失败: ${e.message}", null)
                }
            }
        } catch (e: Exception) {
            onResult("导出图片失败: ${e.message}", null)
        }
    }

    fun exportToPdfFromWebView(webView: WebView, fileName: String, context: Context, onResult: (String, Uri?) -> Unit) {
        try {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                try {
                    handler.postDelayed({
                    try {
                        val contentHeight = (webView.contentHeight * webView.resources.displayMetrics.density).toInt()
                        val width = webView.width.coerceAtLeast(1)
                        val height = contentHeight.coerceAtLeast(webView.height).coerceAtLeast(1)

                        webView.measure(
                            android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
                            android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
                        )
                        webView.layout(0, 0, width, height)

                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        webView.draw(canvas)

                    val pageWidth = 595
                    val pageHeight = 842
                    val scale = pageWidth.toFloat() / width
                    val scaledHeight = (height * scale).toInt()

                    val document = PdfDocument()
                    var yOffset = 0
                    var pageNum = 1

                    while (yOffset < scaledHeight) {
                        val remaining = scaledHeight - yOffset
                        val thisPageHeight = remaining.coerceAtMost(pageHeight)
                        val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, thisPageHeight, pageNum).create())
                        val pageCanvas = page.canvas
                        pageCanvas.save()
                        pageCanvas.scale(scale, scale)
                        pageCanvas.clipRect(0f, yOffset / scale, width.toFloat(), (yOffset + thisPageHeight) / scale)
                        pageCanvas.drawBitmap(bitmap, 0f, 0f, null)
                        pageCanvas.restore()
                        document.finishPage(page)
                        yOffset += pageHeight
                        pageNum++
                    }

                    val uri = saveViaMediaStore(context, "$fileName.pdf", "application/pdf") { os ->
                        document.writeTo(os)
                    }
                    document.close()
                    bitmap.recycle()

                    onResult("PDF 已导出到 Download/Mdown/$fileName.pdf", uri)
                    } catch (e: Exception) {
                        onResult("导出PDF失败: ${e.message}", null)
                    }
                    }, 500)
                } catch (e: Exception) {
                    onResult("导出PDF失败: ${e.message}", null)
                }
            }
        } catch (e: Exception) {
            onResult("导出PDF失败: ${e.message}", null)
        }
    }

    fun shareMarkdownFile(filePath: String, context: Context): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false

            val uri = getFileUri(context, file)
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = android.content.Intent.createChooser(intent, "分享 Markdown 文件")
            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }

    private data class ParsedLine(
        val text: String,
        val type: Type,
        val spacing: Float
    ) {
        enum class Type {
            EMPTY, NORMAL, HEADING1, HEADING2, HEADING3, HEADING4, HEADING5, HEADING6,
            LIST_UNORDERED, LIST_ORDERED, QUOTE, CODE, CODE_BLOCK, HR, TABLE
        }
    }

    private fun parseMarkdownLines(markdown: String): List<ParsedLine> {
        val result = mutableListOf<ParsedLine>()
        val lines = markdown.split("\n")
        var inCodeBlock = false

        for (line in lines) {
            val trimmed = line.trimEnd()

            if (trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock
                if (inCodeBlock) {
                    val lang = trimmed.removePrefix("```").trim()
                    result.add(ParsedLine(if (lang.isNotEmpty()) "代码: $lang" else "代码块", ParsedLine.Type.CODE_BLOCK, 6f))
                } else {
                    result.add(ParsedLine("", ParsedLine.Type.EMPTY, 8f))
                }
                continue
            }

            if (inCodeBlock) {
                result.add(ParsedLine("  $trimmed", ParsedLine.Type.CODE_BLOCK, 2f))
                continue
            }

            if (trimmed.isEmpty()) {
                result.add(ParsedLine("", ParsedLine.Type.EMPTY, 10f))
                continue
            }

            when {
                trimmed.startsWith("######") -> {
                    val text = trimmed.removePrefix("######").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.HEADING6, 8f))
                }
                trimmed.startsWith("#####") -> {
                    val text = trimmed.removePrefix("#####").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.HEADING5, 8f))
                }
                trimmed.startsWith("####") -> {
                    val text = trimmed.removePrefix("####").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.HEADING4, 10f))
                }
                trimmed.startsWith("###") -> {
                    val text = trimmed.removePrefix("###").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.HEADING3, 12f))
                }
                trimmed.startsWith("##") -> {
                    val text = trimmed.removePrefix("##").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.HEADING2, 14f))
                }
                trimmed.startsWith("#") -> {
                    val text = trimmed.removePrefix("#").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.HEADING1, 18f))
                }
                trimmed == "---" || trimmed == "***" || trimmed == "___" -> {
                    result.add(ParsedLine("────────────────────────", ParsedLine.Type.HR, 12f))
                }
                trimmed.startsWith("> ") -> {
                    val text = "│ " + trimmed.removePrefix("> ").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.QUOTE, 4f))
                }
                trimmed.startsWith(">") -> {
                    val text = "│ " + trimmed.removePrefix(">").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.QUOTE, 4f))
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
                    val text = "• " + trimmed.substring(2).trim()
                    result.add(ParsedLine(text, ParsedLine.Type.LIST_UNORDERED, 4f))
                }
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val match = Regex("^(\\d+\\.)\\s+(.*)").find(trimmed)
                    val text = match?.let { "${it.groupValues[1]} ${it.groupValues[2]}" } ?: trimmed
                    result.add(ParsedLine(text, ParsedLine.Type.LIST_ORDERED, 4f))
                }
                trimmed.startsWith("|") -> {
                    val cells = trimmed.split("|").filter { it.isNotBlank() }.map { it.trim() }
                    val text = cells.joinToString("  |  ")
                    result.add(ParsedLine(text, ParsedLine.Type.TABLE, 4f))
                }
                trimmed.startsWith("- [ ] ") -> {
                    val text = "☐ " + trimmed.removePrefix("- [ ] ").trim()
                    result.add(ParsedLine(text, ParsedLine.Type.LIST_UNORDERED, 4f))
                }
                trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
                    val text = "☑ " + trimmed.substring(6).trim()
                    result.add(ParsedLine(text, ParsedLine.Type.LIST_UNORDERED, 4f))
                }
                else -> {
                    val displayText = trimmed
                        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
                        .replace(Regex("\\*(.+?)\\*"), "$1")
                        .replace(Regex("~~(.+?)~~"), "$1")
                        .replace(Regex("`(.+?)`"), "$1")
                        .replace(Regex("\\[(.+?)]\\(.+?\\)"), "$1")
                        .replace(Regex("!\\[(.+?)]\\(.+?\\)"), "[图片: $1]")
                    result.add(ParsedLine(displayText, ParsedLine.Type.NORMAL, 4f))
                }
            }
        }

        return result
    }

    private fun buildStyledHtml(bodyHtml: String): String {
        val bgColor = "#FFFFFF"
        val textColor = "#1A1C19"
        val headingColor = "#2E7D32"
        val codeBg = "#F0F4EC"
        val codeColor = "#2E7D32"
        val borderColor = "#DDE5D9"
        val linkColor = "#006C4C"
        val tableHeaderBg = "#F0F4EC"

        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'; img-src 'self' file: content: data: https:;">
<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
    background: $bgColor;
    color: $textColor;
    line-height: 1.75;
    padding: 20px 16px;
    font-size: 15px;
    word-wrap: break-word;
    overflow-wrap: break-word;
}
h1, h2, h3, h4, h5, h6 { color: $headingColor; margin-top: 24px; margin-bottom: 12px; font-weight: 600; line-height: 1.4; }
h1 { font-size: 1.75em; border-bottom: 2px solid $borderColor; padding-bottom: 8px; }
h2 { font-size: 1.45em; border-bottom: 1px solid $borderColor; padding-bottom: 6px; }
h3 { font-size: 1.2em; }
p { margin-bottom: 14px; }
a { color: $linkColor; text-decoration: none; }
code { background: $codeBg; color: $codeColor; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 0.88em; word-break: break-all; }
pre { background: $codeBg; padding: 16px; border-radius: 10px; overflow-x: auto; margin: 16px 0; border: 1px solid $borderColor; white-space: pre-wrap; word-wrap: break-word; }
pre code { background: transparent; padding: 0; color: $textColor; font-size: 0.85em; }
table { border-collapse: collapse; width: 100%; margin: 16px 0; border: 1px solid $borderColor; }
th { background: $tableHeaderBg; font-weight: 600; text-align: left; }
th, td { padding: 10px 14px; border-bottom: 1px solid $borderColor; }
blockquote { border-left: 3px solid $headingColor; padding: 8px 16px; margin: 16px 0; color: #666; background: $codeBg; border-radius: 0 8px 8px 0; }
ul, ol { margin: 10px 0 14px 24px; }
li { margin-bottom: 4px; }
hr { border: none; height: 1px; background: $borderColor; margin: 24px 0; }
img { max-width: 100%; border-radius: 8px; margin: 12px 0; }
del { color: #999; text-decoration: line-through; }
</style>
</head>
<body>
$bodyHtml
</body>
</html>
        """.trimIndent()
    }

    private fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun getFileUri(context: Context, file: File): Uri {
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: IllegalArgumentException) {
            queryMediaStoreUri(context, file)
                ?: copyToTempAndGetUri(context, file)
        }
    }

    private fun queryMediaStoreUri(context: Context, file: File): Uri? {
        val projection = arrayOf(android.provider.MediaStore.MediaColumns._ID)
        val selection = "${android.provider.MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        context.contentResolver.query(
            android.provider.MediaStore.Files.getContentUri("external"),
            projection, selection, selectionArgs, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns._ID))
                return Uri.withAppendedPath(android.provider.MediaStore.Files.getContentUri("external"), id.toString())
            }
        }
        return null
    }

    private fun copyToTempAndGetUri(context: Context, file: File): Uri {
        val tempFile = java.io.File(context.cacheDir, "shared_${file.name}")
        file.copyTo(tempFile, overwrite = true)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }
}
