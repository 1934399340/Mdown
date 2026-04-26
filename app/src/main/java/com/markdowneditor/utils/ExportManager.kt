package com.markdowneditor.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.webkit.WebView
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream

class ExportManager(private val markdownRenderer: MarkdownRenderer) {
    fun exportToPdf(markdown: String, outputFile: File) {
        val writer = PdfWriter(outputFile)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)
        
        val html = markdownRenderer.render(markdown)
        val doc = Jsoup.parse(html)
        
        doc.select("p").forEach {
            document.add(Paragraph(it.text()))
        }
        
        doc.select("h1, h2, h3, h4, h5, h6").forEach {
            document.add(Paragraph(it.text()).setBold())
        }
        
        document.close()
    }

    fun exportToHtml(markdown: String, outputFile: File) {
        val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>Markdown Export</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 40px; }
                h1 { color: #333; }
                h2 { color: #555; }
                p { line-height: 1.6; }
                code { background: #f4f4f4; padding: 2px 4px; }
                pre { background: #f4f4f4; padding: 10px; overflow-x: auto; }
                table { border-collapse: collapse; width: 100%; margin: 20px 0; }
                th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                th { background-color: #f2f2f2; }
                blockquote { border-left: 4px solid #ddd; padding-left: 15px; margin: 15px 0; color: #666; }
                ul, ol { margin-left: 20px; margin-bottom: 15px; }
                li { margin-bottom: 5px; }
                del { text-decoration: line-through; color: #999; }
            </style>
        </head>
        <body>
            ${markdownRenderer.render(markdown)}
        </body>
        </html>
        """.trimIndent()
        
        outputFile.writeText(html)
    }

    fun exportToTxt(markdown: String, outputFile: File) {
        // 直接导出Markdown文本
        outputFile.writeText(markdown)
    }

    fun exportToWord(markdown: String, outputFile: File) {
        val document = XWPFDocument()
        val html = markdownRenderer.render(markdown)
        val doc = Jsoup.parse(html)
        
        // 处理标题和段落
        doc.select("h1, h2, h3, h4, h5, h6, p").forEach {
            val paragraph: XWPFParagraph = document.createParagraph()
            val run: XWPFRun = paragraph.createRun()
            
            when (it.tagName()) {
                "h1" -> run.fontSize = 24f
                "h2" -> run.fontSize = 20f
                "h3" -> run.fontSize = 16f
                "h4" -> run.fontSize = 14f
                "h5" -> run.fontSize = 12f
                "h6" -> run.fontSize = 10f
            }
            
            if (it.tagName().startsWith("h")) {
                run.bold = true
            }
            
            run.text = it.text()
        }
        
        // 处理列表
        doc.select("ul, ol").forEach { list ->
            list.select("li").forEach { item ->
                val paragraph: XWPFParagraph = document.createParagraph()
                val run: XWPFRun = paragraph.createRun()
                run.text = "• ${item.text()}"
            }
        }
        
        // 处理引用
        doc.select("blockquote").forEach { quote ->
            val paragraph: XWPFParagraph = document.createParagraph()
            val run: XWPFRun = paragraph.createRun()
            run.text = "> ${quote.text()}"
        }
        
        // 保存文档
        val fos = FileOutputStream(outputFile)
        document.write(fos)
        fos.close()
        document.close()
    }

    fun exportToImage(webView: WebView, outputFile: File) {
        // 等待WebView加载完成
        webView.post {
            // 创建一个与WebView大小相同的Bitmap
            val bitmap = Bitmap.createBitmap(
                webView.width,
                webView.contentHeight,
                Bitmap.Config.ARGB_8888
            )
            
            // 将WebView内容绘制到Bitmap
            val canvas = Canvas(bitmap)
            webView.draw(canvas)
            
            // 保存Bitmap到文件
            val fos = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        }
    }
}
