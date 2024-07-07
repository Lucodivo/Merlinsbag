@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.model.LazyArticlesWithImages
import com.inasweaterpoorlyknit.core.data.model.LazyFilenames
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

data class ArticleEnsembleUiState(
    val articleEnsembles: List<Ensemble>,
    val searchEnsembles: List<Ensemble>,
    val searchIsUniqueTitle: Boolean,
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

  private var cachedArticlesWithFullImages: LazyArticlesWithImages = LazyArticlesWithImages.Empty
  private var cachedArticleEnsembleIdSet: Set<String> = emptySet()
  private var cachedArticleEnsembles: List<Ensemble> = emptyList()
  private var cachedAllEnsembles: List<Ensemble> = emptyList()

  private val _exportedImageUri = MutableSharedFlow<Pair<Int, Uri>>()
  val articleExported: SharedFlow<Pair<Int, Uri>> = _exportedImageUri

  private val _articleId = MutableSharedFlow<String>()
  var articleId: String? = null

  private val _removedArticleIndexWithId = MutableSharedFlow<Pair<Int, String>>(replay = 1).apply { tryEmit(Pair(-1,"")) }

  private var searchQuery = ""
  private val _searchQuery = MutableSharedFlow<String>(replay = 1).apply { tryEmit(searchQuery) }

  fun onArticleFocus(index: Int) = viewModelScope.launch {
    articleIndex = index
    if(index < cachedArticlesWithFullImages.size) {
      val id = cachedArticlesWithFullImages.getArticleId(index)
      articleId = id
      _articleId.emit(id)
    }
  }

  fun deleteArticle(index: Int) {
    val articleId = cachedArticlesWithFullImages.getArticleId(index)
    viewModelScope.launch(Dispatchers.IO) {
      _removedArticleIndexWithId.emit(Pair(index, articleId))
      articleRepository.deleteArticle(articleId)
    }
  }

  fun exportArticle(articleIndex: Int, imageIndex: Int) {
    val imageUri = cachedArticlesWithFullImages.lazyFullImageUris.getUriStrings(articleIndex)[imageIndex]
    viewModelScope.launch(Dispatchers.Default) {
      val exportedImageUri = articleRepository.exportImage(imageUri)
      exportedImageUri?.let { _exportedImageUri.emit(Pair(articleIndex, it)) }
    }
  }

  fun searchEnsembles(query: String) = viewModelScope.launch(Dispatchers.IO) {
    searchQuery = query
    _searchQuery.emit(searchQuery)
  }

  fun removeArticleEnsembles(articleIndex: Int, articleEnsembleIndices: List<Int>) {
    val articleId = cachedArticlesWithFullImages.getArticleId(articleIndex)
    val ensembleIds = articleEnsembleIndices.map { cachedArticleEnsembles[it].id }
    viewModelScope.launch(Dispatchers.IO) {
      ensembleRepository.deleteEnsemblesFromArticle(articleId, ensembleIds)
    }
  }

  fun addArticleToNewEnsemble(articleIndex: Int, title: String) {
    val articleId: String = cachedArticlesWithFullImages.getArticleId(articleIndex)
    viewModelScope.launch(Dispatchers.IO) {
      val isUnique = ensembleRepository.isEnsembleTitleUnique(title).first()
      if(isUnique) ensembleRepository.insertEnsemble(title, listOf(articleId))
    }
  }

  fun addArticleToEnsemble(articleIndex: Int, ensembleId: String) = viewModelScope.launch(Dispatchers.IO){
    ensembleRepository.addEnsemblesToArticle(articleId = cachedArticlesWithFullImages.getArticleId(articleIndex), ensembleIds = listOf(ensembleId))
  }

  val filter: StateFlow<String> = if(ensembleId != null) {
    ensembleRepository.getEnsemble(ensembleId)
  } else {
    emptyFlow()
  }.map { it.title }
      .stateIn(
        scope = viewModelScope,
        initialValue = "",
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS)
      )

  val lazyArticleFilenames: StateFlow<LazyFilenames> = articleRepository.getArticlesWithImages(ensembleId)
      .onEach { images ->
        cachedArticlesWithFullImages = images
        val id = images.getArticleId(articleIndex)
        articleId = id
        if(articleIndex < images.size) _articleId.emit(id)
      }.stateIn(
        scope = viewModelScope,
        initialValue = LazyFilenames.Empty,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS)
      )

  val ensembleUiState: StateFlow<ArticleEnsembleUiState> = combine(
    _articleId.flatMapLatest { ensembleRepository.getEnsemblesByArticle(it) }
        .onEach {
          cachedArticleEnsembles = it
          cachedArticleEnsembleIdSet = it.map { it.id }.toSet()
        },
    _searchQuery.flatMapLatest {  searchQuery ->
      if(searchQuery.isEmpty()) { flowOf(Pair(emptyList(), false)) }
      else ensembleRepository.searchAllEnsembles(searchQuery).zip(ensembleRepository.isEnsembleTitleUnique(searchQuery)){ searchEnsembles, unique ->
        Pair(searchEnsembles, unique)
      }
    },
    ensembleRepository.getAllEnsembles().onEach { cachedAllEnsembles = it },
  ) { articleEnsembles, searchResults, allEnsembles ->
    val searchEnsembles: List<Ensemble> = if(searchQuery.isEmpty()) allEnsembles else searchResults.first
    ArticleEnsembleUiState(
      articleEnsembles = articleEnsembles,
      searchEnsembles = searchEnsembles.filter { !cachedArticleEnsembleIdSet.contains(it.id) },
      searchIsUniqueTitle = searchResults.second
    )
  }.stateIn(
    scope = viewModelScope,
    initialValue = ArticleEnsembleUiState(emptyList(), emptyList(), false),
    started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS)
  )
}