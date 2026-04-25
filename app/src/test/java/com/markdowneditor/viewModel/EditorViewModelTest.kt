package com.markdowneditor.viewModel

import org.junit.Test
import org.junit.Assert.*

class EditorViewModelTest {
    private val viewModel = EditorViewModel()

    @Test
    fun testUpdateText() {
        val testText = "# Test"
        viewModel.updateText(testText)
        assertEquals(testText, viewModel.markdownText)
    }
}
