package com.markdowneditor.utils

import com.markdowneditor.data.model.MarkdownFile
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.IOException

class FileManagerTest {
    private lateinit var tempDir: File
    private lateinit var fileManager: FileManager

    @Before
    fun setup() {
        tempDir = File.createTempFile("test", null).apply { delete() }
        tempDir.mkdir()
        fileManager = FileManager(tempDir)
    }

    @After
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testCreateFile() {
        val content = "# Test File"
        val file = fileManager.createFile("test", content)
        assertEquals("test", file.fileName)
        assertTrue(file.filePath.endsWith("test.md"))
        assertEquals(content, fileManager.readFile(file.filePath))
    }

    @Test
    fun testListFiles() {
        fileManager.createFile("file1")
        fileManager.createFile("file2")
        val files = fileManager.listFiles()
        assertEquals(2, files.size)
    }

    @Test
    fun testDeleteFile() {
        val file = fileManager.createFile("test")
        assertTrue(fileManager.deleteFile(file.filePath))
        assertEquals(0, fileManager.listFiles().size)
    }
}
