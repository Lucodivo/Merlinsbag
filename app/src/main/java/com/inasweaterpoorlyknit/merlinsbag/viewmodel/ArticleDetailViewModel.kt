@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.model.LazyArticleFullImages
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

data class ArticleEnsembleUiState(
    val articleEnsembles: List<Ensemble>,
    val addEnsembles: List<Ensemble>,
)

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
  private var cachedArticleEnsembleIdSet: Set<String> = emptySet()
  private var cachedArticleEnsembles: List<Ensemble> = emptyList()
  private var cachedAllEnsembles: List<Ensemble> = emptyList()

  private val _exportedImageUri = MutableSharedFlow<Pair<Int, Uri>>()
  val articleExported: SharedFlow<Pair<Int, Uri>> = _exportedImageUri

  private val _articleId = MutableSharedFlow<String>()

  private var searchQuery = ""
  private val _searchQuery = MutableSharedFlow<String>(replay = 1).apply { tryEmit(searchQuery) }

  fun onArticleFocus(index: Int) = viewModelScope.launch {
    articleIndex = index
    if(index < cachedArticlesWithFullImages.size) _articleId.emit(cachedArticlesWithFullImages.getArticleId(index))
  }

  fun deleteArticle(index: Int) = viewModelScope.launch(Dispatchers.IO) {
    articleRepository.deleteArticle(cachedArticlesWithFullImages.getArticleId(index))
  }

  fun exportArticle(index: Int) = viewModelScope.launch(Dispatchers.Default) {
    val exportedImageUri = articleRepository.exportArticle(cachedArticlesWithFullImages.getArticleId(index))
    exportedImageUri?.let { _exportedImageUri.emit(Pair(index, it)) }
  }

  fun searchEnsembles(query: String) = viewModelScope.launch(Dispatchers.IO) {
    searchQuery = if(query.isEmpty()) "" else "$query*"
    _searchQuery.emit(searchQuery)
  }

  fun removeArticleEnsembles(articleIndex: Int, articleEnsembleIndices: List<Int>) {
    val articleId = cachedArticlesWithFullImages.getArticleId(articleIndex)
    val ensembleIds = articleEnsembleIndices.map { cachedArticleEnsembles[it].id }
    viewModelScope.launch(Dispatchers.IO) {
      ensembleRepository.deleteEnsemblesFromArticle(articleId, ensembleIds)
    }
  }

  fun addArticleToEnsembles(articleIndex: Int, ensembleIds: List<String>) = viewModelScope.launch(Dispatchers.IO) {
    ensembleRepository.addEnsemblesToArticle(articleId = cachedArticlesWithFullImages.getArticleId(articleIndex), ensembleIds = ensembleIds)
  }

  val filter: StateFlow<String> = if(ensembleId != null) {
    ensembleRepository.getEnsemble(ensembleId)
  } else {
    emptyFlow()
  }
      .map { it.title }
      .stateIn(
        scope = viewModelScope,
        initialValue = "",
        started = SharingStarted.WhileSubscribed()
      )

  val articleLazyUriStrings: StateFlow<LazyUriStrings> = articleRepository.getArticlesWithFullImages(ensembleId)
      .take(1) // A constantly updating article set in the ArticleDetailScreen is a bad user experience
      .onEach { images ->
        cachedArticlesWithFullImages = images
        if(articleIndex < images.size) _articleId.emit(images.getArticleId(articleIndex))
      }
      .stateIn(
        scope = viewModelScope,
        initialValue = LazyUriStrings.Empty,
        started = SharingStarted.WhileSubscribed()
      )

  val ensembleUiState: StateFlow<ArticleEnsembleUiState> = combine(
    _articleId.flatMapLatest { ensembleRepository.getEnsemblesByArticle(it) }
        .onEach {
          cachedArticleEnsembles = it
          cachedArticleEnsembleIdSet = it.map { it.id }.toSet()
        },
    _searchQuery.flatMapLatest { searchQuery -> ensembleRepository.searchAllEnsembles(searchQuery) },
    ensembleRepository.getAllEnsembles().onEach { cachedAllEnsembles = it },
  ) { articleEnsembles, searchEnsembles, _ ->
    val searchResults = if(searchQuery.isEmpty()) cachedAllEnsembles else searchEnsembles
    ArticleEnsembleUiState(
      articleEnsembles = articleEnsembles,
      addEnsembles = searchResults.filter { !cachedArticleEnsembleIdSet.contains(it.id) }
    )
  }.stateIn(
    scope = viewModelScope,
    initialValue = ArticleEnsembleUiState(emptyList(), emptyList()),
    started = SharingStarted.WhileSubscribed()
  )
}