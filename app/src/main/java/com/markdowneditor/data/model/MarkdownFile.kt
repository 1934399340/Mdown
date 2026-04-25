package com.markdowneditor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "markdown_files")
data class MarkdownFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val filePath: String,
    val lastModified: Date,
    val size: Long
)
