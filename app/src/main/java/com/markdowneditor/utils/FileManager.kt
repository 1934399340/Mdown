package com.markdowneditor.utils

import com.markdowneditor.data.model.MarkdownFile
import java.io.File
import java.util.Date

class FileManager(private val baseDirectory: File) {
    init {
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs()
        }
    }

    fun createFile(fileName: String, content: String = ""): MarkdownFile {
        val file = File(baseDirectory, "$fileName.md")
        file.writeText(content)
        return MarkdownFile(
            fileName = fileName,
            filePath = file.absolutePath,
            lastModified = Date(file.lastModified()),
            size = file.length()
        )
    }

    fun readFile(filePath: String): String {
        val file = File(filePath)
        return file.readText()
    }

    fun writeFile(filePath: String, content: String) {
        val file = File(filePath)
        file.writeText(content)
    }

    fun listFiles(): List<MarkdownFile> {
        return baseDirectory.listFiles()?.filter { it.extension == "md" }?.map {
            MarkdownFile(
                fileName = it.nameWithoutExtension,
                filePath = it.absolutePath,
                lastModified = Date(it.lastModified()),
                size = it.length()
            )
        } ?: emptyList()
    }

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return file.delete()
    }
}
