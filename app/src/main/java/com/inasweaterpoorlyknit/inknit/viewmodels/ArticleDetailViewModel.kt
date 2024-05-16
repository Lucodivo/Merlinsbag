package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ArticleDetailUiState(
  val imageUriString: String?
)

@HiltViewModel(assistedFactory = ArticleDetailViewModel.ArticleDetailViewModelFactory::class)
class ArticleDetailViewModel @AssistedInject constructor(
  @Assisted private val articleId: String,
  private val articleRepository: ArticleRepository,
): ViewModel() {

  @AssistedFactory
  interface ArticleDetailViewModelFactory {
    fun create(articleId: String): ArticleDetailViewModel
  }

  val articleDetailUiState: StateFlow<ArticleDetailUiState> = articleRepository.getArticleWithImages(articleId).map {
      ArticleDetailUiState(imageUriString = it.images[0].uri)
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = ArticleDetailUiState(imageUriString = null)
    )
}