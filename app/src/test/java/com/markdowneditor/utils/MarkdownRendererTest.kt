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

    @Test
    fun testRenderTable() {
        val markdown = "| Header 1 | Header 2 |\n| --- | --- |\n| Row 1 | Row 2 |"
        val result = renderer.render(markdown)
        assertTrue(result.contains("<table"))
        assertTrue(result.contains("<th>Header 1</th>"))
        assertTrue(result.contains("<th>Header 2</th>"))
        assertTrue(result.contains("<td>Row 1</td>"))
        assertTrue(result.contains("<td>Row 2</td>"))
    }

    @Test
    fun testRenderFootnote() {
        val markdown = "Text with footnote[^1]\n\n[^1]: Footnote content"
        val result = renderer.render(markdown)
        assertTrue(result.contains("<sup class=\"footnote-ref\" id=\"fnref-1\">"))
        assertTrue(result.contains("<div class=\"footnotes\""))
    }

    @Test
    fun testRenderMathBlock() {
        val markdown = "$$E = mc^2$$"
        val result = renderer.render(markdown)
        assertTrue(result.contains("<div class=\"math-block\""))
        assertTrue(result.contains("E = mc^2"))
    }

    @Test
    fun testRenderStrikethrough() {
        val markdown = "~~Strikethrough text~~"
        val result = renderer.render(markdown)
        assertTrue(result.contains("<del>Strikethrough text</del>"))
    }

    @Test
    fun testRenderTaskList() {
        val markdown = "- [ ] Task 1\n- [x] Task 2"
        val result = renderer.render(markdown)
        assertTrue(result.contains("<input type=\"checkbox\" disabled=\"disabled\""))
        assertTrue(result.contains("Task 1"))
        assertTrue(result.contains("Task 2"))
    }

    @Test
    fun testRenderAutoLink() {
        val markdown = "<https://example.com>"
        val result = renderer.render(markdown)
        assertTrue(result.contains("<a href=\"https://example.com\">https://example.com</a>"))
    }

    @Test
    fun testRenderAbbreviation() {
        val markdown = "HTML is great\n\n*[HTML]: Hyper Text Markup Language"
        val result = renderer.render(markdown)
        assertTrue(result.contains("<abbr title=\"Hyper Text Markup Language\">HTML</abbr>"))
    }

    @Test
    fun testRenderQuote() {
        val markdown = "> This is a quote"
        val expected = "<blockquote>\n<p>This is a quote</p>\n</blockquote>\n"
        assertEquals(expected, renderer.render(markdown))
    }

    @Test
    fun testRenderList() {
        val markdown = "- Item 1\n- Item 2"
        val expected = "<ul>\n<li>Item 1</li>\n<li>Item 2</li>\n</ul>\n"
        assertEquals(expected, renderer.render(markdown))
    }

    @Test
    fun testRenderOrderedList() {
        val markdown = "1. Item 1\n2. Item 2"
        val expected = "<ol>\n<li>Item 1</li>\n<li>Item 2</li>\n</ol>\n"
        assertEquals(expected, renderer.render(markdown))
    }

    @Test
    fun testRenderCodeBlock() {
        val markdown = "```kotlin\nfun main() {\n    println(\"Hello\")\n}\n```"
        val result = renderer.render(markdown)
        assertTrue(result.contains("<pre><code class=\"language-kotlin\">"))
        assertTrue(result.contains("fun main() {"))
    }

    @Test
    fun testRenderHorizontalRule() {
        val markdown = "---"
        val expected = "<hr />\n"
        assertEquals(expected, renderer.render(markdown))
    }

    @Test
    fun testRenderLink() {
        val markdown = "[Google](https://google.com)"
        val expected = "<p><a href=\"https://google.com\">Google</a></p>\n"
        assertEquals(expected, renderer.render(markdown))
    }

    @Test
    fun testRenderImage() {
        val markdown = "![Image](https://example.com/image.jpg)"
        val expected = "<p><img src=\"https://example.com/image.jpg\" alt=\"Image\" /></p>\n"
        assertEquals(expected, renderer.render(markdown))
    }
}
