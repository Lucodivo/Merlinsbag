package com.inasweaterpoorlyknit.core.database.di

import android.content.Context
import androidx.room.Room
import com.inasweaterpoorlyknit.core.database.model.AppDatabase
import com.inasweaterpoorlyknit.core.database.model.ClothingArticleWithImagesDao
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
            "inknit-database"
        ).build()
    }

    @Provides
    @Singleton
    fun providesClothingArticleWithImagesDao(appDatabase: AppDatabase): ClothingArticleWithImagesDao = appDatabase.clothingArticleWithImagesDao()
}