package com.markdowneditor.di

import android.content.Context
import androidx.room.Room
import com.markdowneditor.data.database.FileDao
import com.markdowneditor.data.database.FileDatabase
import com.markdowneditor.data.repository.FileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFileDatabase(@ApplicationContext context: Context): FileDatabase {
        return Room.databaseBuilder(
            context,
            FileDatabase::class.java,
            "markdown_files.db"
        ).addMigrations(FileDatabase.MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideFileDao(database: FileDatabase): FileDao {
        return database.fileDao()
    }

    @Provides
    @Singleton
    fun provideFileRepository(fileDao: FileDao): FileRepository {
        return FileRepository(fileDao)
    }
}