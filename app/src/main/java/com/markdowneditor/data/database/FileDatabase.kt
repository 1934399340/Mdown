package com.markdowneditor.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.markdowneditor.data.model.MarkdownFile

@Database(entities = [MarkdownFile::class], version = 2)
@TypeConverters(Converters::class)
abstract class FileDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE markdown_files ADD COLUMN isDirectory INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
