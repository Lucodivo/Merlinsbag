package com.inasweaterpoorlyknit.core.database.repository

import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImagesDao

// TODO: The repository layer is of more use when abstracting data sources.
//  When pulling/pushing data from more places than the local Room database,
//  it can be pulled out of the database module.
class ArticleRepository(
  private val articleWithImagesDao: ArticleWithImagesDao
) {
  fun getAllArticlesWithImages() = articleWithImagesDao.getAllArticlesWithImages()
  fun insertArticle(imageUri: String, thumbnailUri: String){
    articleWithImagesDao.insertArticle(imageUri, thumbnailUri)
  }
  fun getArticleWithImages(id: String) = articleWithImagesDao.getArticleWithImages(id)
}