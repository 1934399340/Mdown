package com.markdowneditor.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.markdowneditor.data.model.MarkdownFile

@Dao
interface FileDao {
    @Insert
    suspend fun insert(file: MarkdownFile)

    @Update
    suspend fun update(file: MarkdownFile)

    @Query("SELECT * FROM markdown_files ORDER BY lastModified DESC")
    suspend fun getAllFiles(): List<MarkdownFile>

    @Query("DELETE FROM markdown_files WHERE id = :id")
    suspend fun deleteById(id: Int)
}
