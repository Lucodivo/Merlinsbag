package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.model.ArticleWithFullImages
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleFullImages
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ArticleDetailUiState(
    val articleFullImages: LazyUriStrings,
)

@HiltViewModel(assistedFactory = ArticleDetailViewModel.ArticleDetailViewModelFactory::class)
class ArticleDetailViewModel @AssistedInject constructor(
    @Assisted("ensembleId") private val ensembleId: String?,
    val articleRepository: ArticleRepository,
): ViewModel() {

  lateinit var articlesWithFullImages: LazyArticleFullImages

  fun deleteArticle(index: Int) = viewModelScope.launch(Dispatchers.IO) {
    val articleId = articlesWithFullImages.getArticleId(index)
    articleRepository.deleteArticle(articleId)
  }

  fun exportArticle() {
    TODO()
  }

  @AssistedFactory
  interface ArticleDetailViewModelFactory {
    fun create(
        @Assisted("ensembleId") ensembleId: String?,
    ): ArticleDetailViewModel
  }

  val articleDetailUiState: StateFlow<ArticleDetailUiState> = articleRepository.getArticlesWithFullImages(ensembleId)
      .onEach{ articlesWithFullImages = it }
      .map { ArticleDetailUiState(articleFullImages = it) }
      .stateIn(
        scope = viewModelScope,
        initialValue = ArticleDetailUiState(articleFullImages = LazyUriStrings.Empty),
        started = SharingStarted.WhileSubscribed()
      )
}