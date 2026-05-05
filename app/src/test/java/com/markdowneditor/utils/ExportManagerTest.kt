package com.markdowneditor.utils

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ExportManagerTest {
    private val renderer = MarkdownRenderer()
    private val exportManager = ExportManager(renderer)

    @Test
    fun testExportToTxt() {
        val markdown = "# Test\n**Bold** *italic*"
        val outputFile = File.createTempFile("test", ".txt")
        exportManager.exportToTxt(markdown, outputFile)
        assertEquals(markdown, outputFile.readText())
        outputFile.delete()
    }

    @Test
    fun testExportToHtml() {
        val markdown = "# Test\n**Bold** *italic*"
        val outputFile = File.createTempFile("test", ".html")
        exportManager.exportToHtml(markdown, outputFile)
        val content = outputFile.readText()
        assertTrue(content.contains("<h1>Test</h1>"))
        assertTrue(content.contains("<strong>Bold</strong>"))
        assertTrue(content.contains("<em>italic</em>"))
        outputFile.delete()
    }

    @Test
    fun testExportToPdf() {
        val markdown = "# Test\n**Bold** *italic*"
        val outputFile = File.createTempFile("test", ".pdf")
        exportManager.exportToPdf(markdown, outputFile)
        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)
        outputFile.delete()
    }

    @Test
    fun testExportToWord() {
        val markdown = "# Test\n**Bold** *italic*"
        val outputFile = File.createTempFile("test", ".docx")
        exportManager.exportToWord(markdown, outputFile)
        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 0)
        outputFile.delete()
    }
}
