package com.inasweaterpoorlyknit.core.database.di

import android.content.Context
import androidx.room.Room
import com.inasweaterpoorlyknit.core.database.InKnitDatabase
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImagesDao
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
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
    internal fun providesAppDatabase(@ApplicationContext context: Context): InKnitDatabase {
        return Room.databaseBuilder(
            context,
            InKnitDatabase::class.java,
            "inknit-database"
        ).build()
    }

    @Provides
    @Singleton
    fun providesArticleWithImagesDao(appDatabase: InKnitDatabase): ArticleWithImagesDao = appDatabase.ArticleWithImagesDao()

    @Provides
    @Singleton
    fun providesArticleRepository(articleWithImagesDao: ArticleWithImagesDao): ArticleRepository{
        return ArticleRepository(articleWithImagesDao)
    }
}