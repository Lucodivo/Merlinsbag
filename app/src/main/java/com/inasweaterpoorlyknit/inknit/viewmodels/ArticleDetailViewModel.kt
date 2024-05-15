package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ArticleDetailUiState(
  val imageUriString: String
)

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
  private val articleRepository: ArticleRepository,
): ViewModel() {
  // TODO: Convert to flow state?
  fun articleDetailUiState(articleId: String): StateFlow<ArticleDetailUiState?> =
    articleRepository.getArticleWithImages(articleId).map {
      ArticleDetailUiState(imageUriString = it.images[0].uri)
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = null
    )
}