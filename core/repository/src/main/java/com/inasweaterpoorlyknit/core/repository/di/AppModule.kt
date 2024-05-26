package com.inasweaterpoorlyknit.core.repository.di

import android.content.Context
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.EnsembleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  /* Repositories */
  @Provides
  @Singleton
  fun providesArticleRepository(@ApplicationContext context: Context, articleDao: ArticleDao, ensembleDao: EnsembleDao): ArticleRepository = ArticleRepository(context, articleDao, ensembleDao)

  @Provides
  @Singleton
  fun providesEnsembleRepository(@ApplicationContext context: Context, ensembleDao: EnsembleDao): EnsembleRepository = EnsembleRepository(context, ensembleDao)
}
