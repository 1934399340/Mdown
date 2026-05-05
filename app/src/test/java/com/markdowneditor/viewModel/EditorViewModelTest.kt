package com.markdowneditor.viewModel

import org.junit.Test
import org.junit.Assert.*

class EditorViewModelTest {
    private val viewModel = EditorViewModel()

    @Test
    fun testUpdateText() {
        val text = "Test text"
        viewModel.updateText(text)
        assertEquals(text, viewModel.markdownText)
    }

    @Test
    fun testSetCursorPosition() {
        val position = 5
        viewModel.setCursorPosition(position)
        assertEquals(position, viewModel.cursorPosition)
    }

    @Test
    fun testInsertMarkdown() {
        viewModel.updateText("Hello world")
        viewModel.setCursorPosition(5)
        viewModel.insertMarkdown("**", "**")
        assertEquals("Hello **world**", viewModel.markdownText)
        assertEquals(7, viewModel.cursorPosition)
    }

    @Test
    fun testInsertHeading() {
        viewModel.updateText("Hello")
        viewModel.setCursorPosition(0)
        viewModel.insertHeading(1)
        assertEquals("# Hello", viewModel.markdownText)
        assertEquals(2, viewModel.cursorPosition)
    }

    @Test
    fun testInsertCodeBlock() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertCodeBlock("kotlin")
        assertEquals("```kotlin\n\n```", viewModel.markdownText)
        assertEquals(10, viewModel.cursorPosition)
    }

    @Test
    fun testInsertTable() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertTable(2, 2)
        assertTrue(viewModel.markdownText.contains("| | |"))
        assertTrue(viewModel.markdownText.contains("| -| -|"))
    }

    @Test
    fun testInsertMathBlock() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertMathBlock()
        assertEquals("$$\n\n$$", viewModel.markdownText)
        assertEquals(3, viewModel.cursorPosition)
    }

    @Test
    fun testInsertQuote() {
        viewModel.updateText("Hello")
        viewModel.setCursorPosition(0)
        viewModel.insertQuote()
        assertEquals("> Hello", viewModel.markdownText)
        assertEquals(2, viewModel.cursorPosition)
    }

    @Test
    fun testInsertUnorderedList() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertUnorderedList()
        assertEquals("- ", viewModel.markdownText)
        assertEquals(2, viewModel.cursorPosition)
    }

    @Test
    fun testInsertOrderedList() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertOrderedList()
        assertEquals("1. ", viewModel.markdownText)
        assertEquals(3, viewModel.cursorPosition)
    }

    @Test
    fun testInsertLink() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertLink()
        assertEquals("[链接文本](https://example.com)", viewModel.markdownText)
        assertEquals(5, viewModel.cursorPosition)
    }

    @Test
    fun testInsertImage() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertImage()
        assertEquals("![图片描述](https://example.com/image.jpg)", viewModel.markdownText)
        assertEquals(6, viewModel.cursorPosition)
    }

    @Test
    fun testInsertHorizontalRule() {
        viewModel.updateText("Hello")
        viewModel.setCursorPosition(5)
        viewModel.insertHorizontalRule()
        assertEquals("Hello\n---\n", viewModel.markdownText)
        assertEquals(8, viewModel.cursorPosition)
    }

    @Test
    fun testInsertFootnote() {
        viewModel.updateText("Hello")
        viewModel.setCursorPosition(5)
        viewModel.insertFootnote()
        assertTrue(viewModel.markdownText.contains("[^1]"))
        assertTrue(viewModel.markdownText.contains("[^1]: 脚注内容"))
    }

    @Test
    fun testInsertAbbreviation() {
        viewModel.updateText("Hello")
        viewModel.setCursorPosition(5)
        viewModel.insertAbbreviation()
        assertTrue(viewModel.markdownText.contains("HTML"))
        assertTrue(viewModel.markdownText.contains("*[HTML]: Hyper Text Markup Language"))
    }

    @Test
    fun testInsertStrikethrough() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertStrikethrough()
        assertEquals("~~文本~~", viewModel.markdownText)
        assertEquals(3, viewModel.cursorPosition)
    }

    @Test
    fun testInsertTaskList() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertTaskList()
        assertTrue(viewModel.markdownText.contains("- [ ] 任务1"))
        assertTrue(viewModel.markdownText.contains("- [x] 任务2"))
    }

    @Test
    fun testInsertAutoLink() {
        viewModel.updateText("")
        viewModel.setCursorPosition(0)
        viewModel.insertAutoLink()
        assertEquals("<https://example.com>", viewModel.markdownText)
        assertEquals(21, viewModel.cursorPosition)
    }

    @Test
    fun testVoiceInputActive() {
        assertFalse(viewModel.isVoiceInputActive())
    }
}
