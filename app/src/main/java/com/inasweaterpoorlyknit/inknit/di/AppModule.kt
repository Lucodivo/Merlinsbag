package com.inasweaterpoorlyknit.inknit.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.inasweaterpoorlyknit.inknit.database.model.AppDatabase
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleWithImagesDao
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
    internal fun providesAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "inknit-db"
        ).build()
    }

    @Provides
    @Singleton
    fun providesClothingArticleWithImagesDao(appDatabase: AppDatabase): ClothingArticleWithImagesDao = appDatabase.clothingArticleWithImagesDao()
}