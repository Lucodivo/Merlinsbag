package com.inasweaterpoorlyknit.inknit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ArticleDetailUiState(
  val articleFullImages: LazyUriStrings,
)

@HiltViewModel(assistedFactory = ArticleDetailViewModel.ArticleDetailViewModelFactory::class)
class ArticleDetailViewModel @AssistedInject constructor(
  @Assisted("articleIndex") private val articleIndex: Int,
  @Assisted("ensembleId") private val ensembleId: String?,
  articleRepository: ArticleRepository,
): ViewModel() {

  @AssistedFactory
  interface ArticleDetailViewModelFactory {
    fun create(
      @Assisted("articleIndex") articleIndex: Int,
      @Assisted("ensembleId") ensembleId: String?
    ): ArticleDetailViewModel
  }

  val articleDetailUiState: StateFlow<ArticleDetailUiState> = articleRepository.getArticlesWithFullImages(ensembleId).map {
      ArticleDetailUiState(articleFullImages = it)
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = ArticleDetailUiState(
        articleFullImages = LazyUriStrings.Empty,
      )
    )
}