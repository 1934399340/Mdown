package com.markdowneditor.data.repository

import com.markdowneditor.data.database.FileDao
import com.markdowneditor.data.model.MarkdownFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FileRepository(private val fileDao: FileDao) {
    suspend fun insert(file: MarkdownFile) {
        fileDao.insert(file)
    }

    suspend fun update(file: MarkdownFile) {
        fileDao.update(file)
    }

    fun getAllFiles(): Flow<List<MarkdownFile>> = flow {
        emit(fileDao.getAllFiles())
    }

    suspend fun delete(file: MarkdownFile) {
        fileDao.deleteById(file.id)
    }
}
