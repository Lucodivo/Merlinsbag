package com.inasweaterpoorlyknit.core.database.repository

import com.inasweaterpoorlyknit.core.database.dao.ClothingArticleWithImagesDao

// TODO: The repository layer is of more use when abstracting data sources.
//  When pulling/pushing data from more places than the local Room database,
//  it can be pulled out of the database module.
class ClothingArticleRepository(
  private val clothingArticleWithImagesDao: ClothingArticleWithImagesDao
) {
  fun getAllClothingArticlesWithImages() = clothingArticleWithImagesDao.getAllClothingArticlesWithImages()
  fun insertClothingArticle(imageUri: String, thumbnailUri: String){
    clothingArticleWithImagesDao.insertClothingArticle(imageUri, thumbnailUri)
  }
  fun getClothingArticleWithImages(id: String) = clothingArticleWithImagesDao.getClothingArticleWithImages(id)
}