package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.model.LazyArticleFullImages
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ArticleDetailViewModel.ArticleDetailViewModelFactory::class)
class ArticleDetailViewModel @AssistedInject constructor(
    @Assisted("ensembleId") private val ensembleId: String?,
    val articleRepository: ArticleRepository,
): ViewModel() {

  lateinit var articlesWithFullImages: LazyArticleFullImages

  private val _exportedImageUri = MutableSharedFlow<Pair<Int,Uri>>()
  val articleExported: SharedFlow<Pair<Int,Uri>> = _exportedImageUri

  fun deleteArticle(index: Int) = viewModelScope.launch(Dispatchers.IO) {
    val articleId = articlesWithFullImages.getArticleId(index)
    articleRepository.deleteArticle(articleId)
  }

  fun exportArticle(index: Int) = viewModelScope.launch(Dispatchers.IO) {
    val exportedImageUri = articleRepository.exportArticle(articlesWithFullImages.getArticleId(index))
    exportedImageUri?.let { _exportedImageUri.emit(Pair(index, exportedImageUri)) }
  }

  @AssistedFactory
  interface ArticleDetailViewModelFactory {
    fun create(
        @Assisted("ensembleId") ensembleId: String?,
    ): ArticleDetailViewModel
  }

  val articleDetailUiState: StateFlow<LazyUriStrings> = articleRepository.getArticlesWithFullImages(ensembleId)
      .onEach{ articlesWithFullImages = it }
      .stateIn(
        scope = viewModelScope,
        initialValue = LazyUriStrings.Empty,
        started = SharingStarted.WhileSubscribed()
      )
}