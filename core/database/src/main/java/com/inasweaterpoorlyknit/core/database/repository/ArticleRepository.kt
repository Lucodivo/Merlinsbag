package com.inasweaterpoorlyknit.core.database.repository

import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import kotlinx.coroutines.flow.Flow

class ArticleRepository(
  private val articleDao: ArticleDao
) {
  fun getAllArticlesWithImages(): Flow<List<ArticleWithImages>> = articleDao.getAllArticlesWithImages()
  fun insertArticle(imageUri: String, thumbnailUri: String): Unit = articleDao.insertArticle(imageUri, thumbnailUri)
  fun getArticleWithImages(id: String): Flow<ArticleWithImages> = articleDao.getArticleWithImages(id)
}