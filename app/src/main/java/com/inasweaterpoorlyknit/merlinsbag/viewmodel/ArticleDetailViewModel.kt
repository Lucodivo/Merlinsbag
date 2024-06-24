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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ArticleDetailViewModel.ArticleDetailViewModelFactory::class)
class ArticleDetailViewModel @AssistedInject constructor(
    @Assisted("ensembleId") private val ensembleId: String?,
    @Assisted("articleIndex") private var articleIndex: Int,
    val articleRepository: ArticleRepository,
    val ensembleRepository: EnsembleRepository,
): ViewModel() {

  @AssistedFactory
  interface ArticleDetailViewModelFactory {
    fun create(
        @Assisted("ensembleId") ensembleId: String?,
        @Assisted("articleIndex") articleIndex: Int,
    ): ArticleDetailViewModel
  }

  private var cachedArticlesWithFullImages: LazyArticleFullImages = LazyArticleFullImages.Empty
  private var cachedEnsembles: List<Ensemble> = emptyList()
  private var cachedEnsembleIdSet: Set<String> = emptySet()

  private val _exportedImageUri = MutableSharedFlow<Pair<Int,Uri>>()
  val articleExported: SharedFlow<Pair<Int,Uri>> = _exportedImageUri

  private val _articleId = MutableSharedFlow<String>()

  fun onArticleFocus(index: Int) = viewModelScope.launch {
    articleIndex = index
    if(index < cachedArticlesWithFullImages.size) _articleId.emit( cachedArticlesWithFullImages.getArticleId(index))
  }

  fun deleteArticle(index: Int) = viewModelScope.launch(Dispatchers.IO) {
    articleRepository.deleteArticle(cachedArticlesWithFullImages.getArticleId(index))
  }

  fun exportArticle(index: Int) = viewModelScope.launch(Dispatchers.Default){
        val exportedImageUri = articleRepository.exportArticle(cachedArticlesWithFullImages.getArticleId(index))
        exportedImageUri?.let { _exportedImageUri.emit(Pair(index, it)) }
  }

  fun removeArticleEnsembles(articleIndex: Int, articleEnsembleIndices: List<Int>) {
    val articleId = cachedArticlesWithFullImages.getArticleId(articleIndex)
    val ensembleIds = articleEnsembleIndices.map { cachedEnsembles[it].id }
    viewModelScope.launch(Dispatchers.IO) {
      ensembleRepository.deleteEnsemblesFromArticle(articleId, ensembleIds)
    }
  }

  fun addArticleToEnsembles(articleIndex: Int, ensembleIds: List<String>) = viewModelScope.launch(Dispatchers.IO) {
    ensembleRepository.addEnsemblesToArticle(articleId = cachedArticlesWithFullImages.getArticleId(articleIndex), ensembleIds = ensembleIds)
  }

  val filter: StateFlow<String> = if(ensembleId != null) { ensembleRepository.getEnsemble(ensembleId) } else { emptyFlow() }
      .map { it.title }
      .stateIn(
        scope = viewModelScope,
        initialValue = "",
        started = SharingStarted.WhileSubscribed()
      )

  val articleLazyUriStrings: StateFlow<LazyUriStrings> = articleRepository.getArticlesWithFullImages(ensembleId)
      .take(1) // A constantly updating article set in the ArticleDetailScreen is a bad user experience
      .onEach{ images ->
        cachedArticlesWithFullImages = images
        articleIndex?.let { i ->
          if(i < images.size) _articleId.emit(images.getArticleId(i))
        }
      }
      .stateIn(
        scope = viewModelScope,
        initialValue = LazyUriStrings.Empty,
        started = SharingStarted.WhileSubscribed()
      )

  data class EnsembleUiState(
      val articleEnsembles: List<Ensemble>,
      val addEnsembles: List<Ensemble>,
  )

  val ensembleUiState: StateFlow<EnsembleUiState> = combine(
    _articleId.flatMapLatest { ensembleRepository.getEnsemblesByArticle(it) }
        .onEach {
          cachedEnsembles = it
          cachedEnsembleIdSet = it.map { it.id }.toSet()
        },
    ensembleRepository.getAllEnsembles(),
  ){ articleEnsembles, allEnsembles ->
    EnsembleUiState(
      articleEnsembles = articleEnsembles,
      addEnsembles = allEnsembles.filter { !cachedEnsembleIdSet.contains(it.id) }
    )
  }.stateIn(
    scope = viewModelScope,
    initialValue = EnsembleUiState(emptyList(), emptyList()),
    started = SharingStarted.WhileSubscribed()
  )
}