@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.model.LazyEnsembleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SaveEnsembleData(
    val title: String,
    val articleIndices: List<Int>,
)

@HiltViewModel
class EnsemblesViewModel @Inject constructor(
    articleRepository: ArticleRepository,
    val ensemblesRepository: EnsembleRepository,
): ViewModel() {
  private lateinit var articleImages: LazyArticleThumbnails
  private lateinit var ensembles: List<LazyEnsembleThumbnails>
  private var searchEnsemblesQuery: MutableSharedFlow<String> = MutableStateFlow("")

  private val _showAddEnsembleDialog = MutableStateFlow(false)
  val showAddEnsembleDialog: StateFlow<Boolean> = _showAddEnsembleDialog
  private val _showPlaceholder = MutableStateFlow(false)
  val showPlaceholder: StateFlow<Boolean> = _showPlaceholder
  val addArticleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { articleImages = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = LazyUriStrings.Empty
      )

  val lazyEnsembles: StateFlow<List<Pair<String, LazyUriStrings>>> =
      combine(
        ensemblesRepository.getAllEnsembleArticleThumbnails().onEach {
          if(it.isEmpty()){
            if(!_showPlaceholder.value) _showPlaceholder.value = true
          } else if(_showPlaceholder.value) {
            _showPlaceholder.value = false
          }
        },
        searchEnsemblesQuery,
        searchEnsemblesQuery.flatMapLatest { query ->
          ensemblesRepository.searchEnsembleArticleThumbnails(query)
        },
      ) { allEnsembleArticleThumbnails, searchQuery, searchEnsembleArticleThumbnails ->
        if(searchQuery.isEmpty()) allEnsembleArticleThumbnails else searchEnsembleArticleThumbnails
      }.onEach {
        ensembles = it
      }.listMap {
        Pair(it.ensemble.title, it.thumbnails)
      }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
      )

  private fun closeDialog() { _showAddEnsembleDialog.value = false }

  fun onClickAddEnsemble() { _showAddEnsembleDialog.value = true }
  fun onClickCloseAddEnsembleDialog() = closeDialog()
  fun onClickSaveAddEnsembleDialog(saveEnsembleData: SaveEnsembleData) {
    closeDialog()
    if(saveEnsembleData.title.isNotEmpty() || saveEnsembleData.articleIndices.isNotEmpty()) {
      val articleIds = saveEnsembleData.articleIndices.map { articleImages.getArticleId(it) }
      viewModelScope.launch(Dispatchers.IO) {
        ensemblesRepository.insertEnsemble(
          saveEnsembleData.title,
          articleIds,
        )
      }
    }
  }
  fun onClickEnsemble(index: Int): String = ensembles[index].ensemble.id
  fun searchQuery(query: String) = searchEnsemblesQuery.tryEmit(if(query.isEmpty()) "" else "$query*")

  companion object {
    const val MAX_ENSEMBLE_TITLE_LENGTH = 128
  }
}