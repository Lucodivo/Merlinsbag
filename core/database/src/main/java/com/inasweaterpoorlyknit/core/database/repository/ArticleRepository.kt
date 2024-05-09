package com.inasweaterpoorlyknit.core.database.repository

import com.inasweaterpoorlyknit.core.database.dao.ArticleDao

class ArticleRepository(
  private val articleDao: ArticleDao
) {
  fun getAllArticlesWithImages() = articleDao.getAllArticlesWithImages()
  fun insertArticle(imageUri: String, thumbnailUri: String) = articleDao.insertArticle(imageUri, thumbnailUri)
  fun getArticleWithImages(id: String) = articleDao.getArticleWithImages(id)
}