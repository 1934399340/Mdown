package com.markdowneditor.utils

import org.junit.Test
import org.junit.Assert.*

class MarkdownRendererTest {
    private val renderer = MarkdownRenderer()

    @Test
    fun testRenderBoldText() {
        val markdown = "**Bold text**"
        val expected = "<p><strong>Bold text</strong></p>\n"
        assertEquals(expected, renderer.render(markdown))
    }

    @Test
    fun testRenderItalicText() {
        val markdown = "*Italic text*"
        val expected = "<p><em>Italic text</em></p>\n"
        assertEquals(expected, renderer.render(markdown))
    }

    @Test
    fun testRenderHeading() {
        val markdown = "# Heading 1"
        val expected = "<h1>Heading 1</h1>\n"
        assertEquals(expected, renderer.render(markdown))
    }
}
