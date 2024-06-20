@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.model.LazyArticleFullImages
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ArticleDetailViewModel.ArticleDetailViewModelFactory::class)
class ArticleDetailViewModel @AssistedInject constructor(
    @Assisted("ensembleId") private val ensembleId: String?,
    val articleRepository: ArticleRepository,
    val ensembleRepository: EnsembleRepository,
): ViewModel() {

  @AssistedFactory
  interface ArticleDetailViewModelFactory {
    fun create(
        @Assisted("ensembleId") ensembleId: String?,
    ): ArticleDetailViewModel
  }

  var articlesWithFullImages: LazyArticleFullImages = LazyArticleFullImages.Empty

  private val _exportedImageUri = MutableSharedFlow<Pair<Int,Uri>>()
  val articleExported: SharedFlow<Pair<Int,Uri>> = _exportedImageUri

  private var articleIndex: Int? = null
  private val _articleId = MutableSharedFlow<String>()

  fun onArticleFocus(index: Int) = viewModelScope.launch {
    articleIndex = index
    if(index < articlesWithFullImages.size) _articleId.emit(articlesWithFullImages.getArticleId(index))
  }

  fun deleteArticle(index: Int) = viewModelScope.launch(Dispatchers.IO) {
    articleRepository.deleteArticle(articlesWithFullImages.getArticleId(index))
  }

  fun exportArticle(index: Int) = viewModelScope.launch(Dispatchers.IO) {
    val exportedImageUri = articleRepository.exportArticle(articlesWithFullImages.getArticleId(index))
    exportedImageUri?.let { _exportedImageUri.emit(Pair(index, exportedImageUri)) }
  }

  val articleLazyUriStrings: StateFlow<LazyUriStrings> = articleRepository.getArticlesWithFullImages(ensembleId)
      .onEach{ images ->
        articlesWithFullImages = images
        articleIndex?.let { i -> _articleId.emit(images.getArticleId(i)) }
      }
      .stateIn(
        scope = viewModelScope,
        initialValue = LazyUriStrings.Empty,
        started = SharingStarted.WhileSubscribed()
      )

  val articleEnsembles: StateFlow<List<Ensemble>> = _articleId.flatMapLatest { ensembleRepository.getEnsemblesByArticle(it) }
      .stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed()
      )
}