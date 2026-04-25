package com.markdowneditor.utils

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.jsoup.Jsoup
import java.io.File

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
            </style>
        </head>
        <body>
            ${markdownRenderer.render(markdown)}
        </body>
        </html>
        """.trimIndent()
        
        outputFile.writeText(html)
    }
}
