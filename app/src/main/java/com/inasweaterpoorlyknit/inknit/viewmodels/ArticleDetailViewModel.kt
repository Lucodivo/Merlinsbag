package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.core.database.repository.ClothingArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
  private val clothingArticleRepository: ClothingArticleRepository,
): ViewModel() {
  data class ArticleDetails(val imageUriString: String)
  fun getArticleDetails(clothingArticleId: String): LiveData<ArticleDetails?> {
    return clothingArticleRepository.getClothingArticleWithImages(clothingArticleId).map {
        ArticleDetails(imageUriString = it.images[0].uri)
    }
  }
}