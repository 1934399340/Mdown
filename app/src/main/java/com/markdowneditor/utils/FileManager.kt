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

    fun createFile(fileName: String, content: String = "", targetDir: File? = null): MarkdownFile {
        val dir = targetDir ?: baseDirectory
        val safeName = SecurityHelper.sanitizeFileName(fileName)
        val file = File(dir, "$safeName.md")

        if (!SecurityHelper.isPathSafe(file.absolutePath, dir)) {
            throw SecurityException("Path traversal detected: $fileName")
        }

        file.writeText(content)
        return MarkdownFile(
            fileName = safeName,
            filePath = file.absolutePath,
            lastModified = Date(file.lastModified()),
            size = file.length(),
            isDirectory = false
        )
    }

    fun createFolder(folderName: String): MarkdownFile {
        val safeName = SecurityHelper.sanitizeFileName(folderName)
        val folder = File(baseDirectory, safeName)

        if (!SecurityHelper.isPathSafe(folder.absolutePath, baseDirectory)) {
            throw SecurityException("Path traversal detected: $folderName")
        }

        folder.mkdirs()
        return MarkdownFile(
            fileName = safeName,
            filePath = folder.absolutePath,
            lastModified = Date(folder.lastModified()),
            size = 0L,
            isDirectory = true
        )
    }

    fun readFile(filePath: String): String {
        val file = File(filePath)
        if (!SecurityHelper.isPathSafe(filePath, baseDirectory)) {
            throw SecurityException("Access denied: path outside base directory")
        }
        return file.readText()
    }

    fun writeFile(filePath: String, content: String) {
        val file = File(filePath)
        if (!SecurityHelper.isPathSafe(filePath, baseDirectory)) {
            throw SecurityException("Access denied: path outside base directory")
        }
        file.writeText(content)
    }

    fun listFiles(): List<MarkdownFile> {
        val systemDirs = setOf("shared")
        return baseDirectory.listFiles()
            ?.filter { !it.isDirectory || it.name !in systemDirs }
            ?.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
            ?.map { file ->
                MarkdownFile(
                    fileName = if (file.isDirectory) file.name else file.nameWithoutExtension,
                    filePath = file.absolutePath,
                    lastModified = Date(file.lastModified()),
                    size = if (file.isFile) file.length() else 0L,
                    isDirectory = file.isDirectory
                )
            }
            ?.filter { it.isDirectory || it.filePath.endsWith(".md", ignoreCase = true) }
            ?: emptyList()
    }

    fun deleteFile(filePath: String): Boolean {
        if (!SecurityHelper.isPathSafe(filePath, baseDirectory)) {
            throw SecurityException("Access denied: path outside base directory")
        }
        val file = File(filePath)
        return if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    fun copyFileToDir(sourcePath: String, targetDir: File): Boolean {
        val source = File(sourcePath)
        if (!source.exists()) return false
        val target = File(targetDir, source.name)
        if (target.absolutePath == source.absolutePath) return false
        return try {
            if (source.isDirectory) {
                source.copyRecursively(target, overwrite = true)
            } else {
                target.writeBytes(source.readBytes())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun moveFileToDir(sourcePath: String, targetDir: File): Boolean {
        val source = File(sourcePath)
        if (!source.exists()) return false
        val target = File(targetDir, source.name)
        if (target.absolutePath == source.absolutePath) return false
        return try {
            if (source.isDirectory) {
                source.copyRecursively(target, overwrite = true)
                source.deleteRecursively()
            } else {
                source.copyTo(target, overwrite = true)
                source.delete()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
