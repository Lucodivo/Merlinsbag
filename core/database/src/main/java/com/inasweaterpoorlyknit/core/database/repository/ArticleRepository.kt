package com.inasweaterpoorlyknit.core.database.repository

import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import kotlinx.coroutines.flow.Flow

class ArticleRepository(
  private val articleDao: ArticleDao,
  private val ensembleDao: EnsembleDao
) {
  fun getAllArticlesWithImages(): Flow<List<ArticleWithImages>> = articleDao.getAllArticlesWithImages()
  fun insertArticle(imageUri: String, thumbnailUri: String): Unit = articleDao.insertArticle(imageUri, thumbnailUri)
  fun getArticleWithImages(id: String): Flow<ArticleWithImages> = articleDao.getArticleWithImages(id)
  fun getArticlesWithImages(ensembleId: String?): Flow<List<ArticleWithImages>> {
    return if(ensembleId == null) getAllArticlesWithImages()
    else ensembleDao.getEnsembleArticleImages(ensembleId)
  }
}