package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ArticleDetailUiState(
  val imageUriString: String
)

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
  private val articleRepository: ArticleRepository,
): ViewModel() {
  // TODO: Convert to flow state?
  fun getArticleDetails(articleId: String): LiveData<ArticleDetailUiState?> {
    return articleRepository.getArticleWithImages(articleId).map {
        ArticleDetailUiState(imageUriString = it.images[0].uri)
    }
  }
}