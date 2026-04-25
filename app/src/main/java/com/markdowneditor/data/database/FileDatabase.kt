package com.markdowneditor.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.markdowneditor.data.model.MarkdownFile

@Database(entities = [MarkdownFile::class], version = 1)
abstract class FileDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}
