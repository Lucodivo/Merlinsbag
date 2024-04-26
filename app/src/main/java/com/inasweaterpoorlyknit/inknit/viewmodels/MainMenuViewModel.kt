package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.inknit.database.model.ClothingArticleWithImagesDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
  private val clothingArticleWithImagesDao: ClothingArticleWithImagesDao,
) : ViewModel() {
  data class ThumbnailDetails(
    val articleId: String,
    val thumbnailUri: String
  )

  val thumbnailDetails: LiveData<List<ThumbnailDetails>>
    get() = clothingArticleWithImagesDao
      .getAllClothingArticlesWithImages().map { clothingArticlesWithImages ->
        clothingArticlesWithImages.map {
          ThumbnailDetails(
            articleId = it.clothingArticleEntity.id,
            thumbnailUri = it.images[0].thumbnailUri
          )
        }
      }
}