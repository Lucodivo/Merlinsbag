package com.inasweaterpoorlyknit.inknit.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleWithImagesDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
  private val clothingArticleWithImagesDao: ClothingArticleWithImagesDao,
): ViewModel() {
  data class ArticleDetails(val imageUriString: String)
  fun getArticleDetails(clothingArticleId: String): LiveData<ArticleDetails?> {
    return clothingArticleWithImagesDao
      .getClothingArticleWithImagesLive(clothingArticleId).map {
        ArticleDetails(imageUriString = it.images[0].uri)
    }
  }
}