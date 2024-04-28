package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.core.database.repository.ClothingArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
  private val clothingArticleRepository: ClothingArticleRepository,
) : ViewModel() {
  data class ClothingThumbnails(
    val articleId: String,
    val thumbnailUri: String,
  )

  val thumbnailDetails: LiveData<List<ClothingThumbnails>>
    get() = clothingArticleRepository.getAllClothingArticlesWithImages().map { clothingArticlesWithImages ->
        clothingArticlesWithImages.map {
          ClothingThumbnails(
            articleId = it.clothingArticleEntity.id,
            thumbnailUri = it.images[0].thumbnailUri
          )
        }
      }
}