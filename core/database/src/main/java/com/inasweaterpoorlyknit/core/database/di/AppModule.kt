package com.inasweaterpoorlyknit.core.database.di

import android.content.Context
import androidx.room.Room
import com.inasweaterpoorlyknit.core.database.InKnitDatabase
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
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

    /* DAOs */
    @Provides
    @Singleton
    fun providesArticleWithImagesDao(appDatabase: InKnitDatabase): ArticleDao = appDatabase.ArticleDao()

    @Provides
    @Singleton
    fun providesEnsembleDao(appDatabase: InKnitDatabase): EnsembleDao = appDatabase.EnsembleDao()
}