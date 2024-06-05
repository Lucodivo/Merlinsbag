package com.inasweaterpoorlyknit.core.database.di

import android.content.Context
import androidx.room.Room
import com.inasweaterpoorlyknit.core.database.NoopDatabase
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.dao.PurgeDao
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
    internal fun providesAppDatabase(@ApplicationContext context: Context): NoopDatabase {
        return Room.databaseBuilder(
            context,
            NoopDatabase::class.java,
            "noop-database"
        ).build()
    }

    /* DAOs */
    @Provides
    @Singleton
    fun providesArticleWithImagesDao(appDatabase: NoopDatabase): ArticleDao = appDatabase.ArticleDao()

    @Provides
    @Singleton
    fun providesEnsembleDao(appDatabase: NoopDatabase): EnsembleDao = appDatabase.EnsembleDao()

    @Provides
    @Singleton
    fun providesPurgeDao(appDatabase: NoopDatabase): PurgeDao = PurgeDao(appDatabase)
}