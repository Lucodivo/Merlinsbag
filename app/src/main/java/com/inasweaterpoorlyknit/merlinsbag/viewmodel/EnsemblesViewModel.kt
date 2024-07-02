@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.model.LazyEnsembleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

  companion object {
    const val MAX_ENSEMBLE_TITLE_LENGTH = 256
  }

  private lateinit var articleImages: LazyArticleThumbnails
  private lateinit var ensembles: List<LazyEnsembleThumbnails>
  private var searchEnsemblesQuery: MutableSharedFlow<String> = MutableStateFlow("")

  private val _showPlaceholder = MutableStateFlow(false)
  val showPlaceholder: StateFlow<Boolean> = _showPlaceholder
  private val _showAddEnsembleDialog = MutableStateFlow(false)
  val showAddEnsembleDialog: StateFlow<Boolean> = _showAddEnsembleDialog
  private val _ensembleTitleError = MutableStateFlow<Int?>(null)
  val ensembleTitleError: StateFlow<Int?> = _ensembleTitleError

  val addArticleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { articleImages = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = LazyUriStrings.Empty
      )
  var searchQuery: String = "" // NOTE: Allows compose function to save search query when composable is returned to from the backstack

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
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
      )

  fun onClickSaveAddEnsembleDialog(saveEnsembleData: SaveEnsembleData) {
    viewModelScope.launch(Dispatchers.IO) {
      val ensembleTitleUnique = ensemblesRepository.isEnsembleTitleUnique(saveEnsembleData.title).first()
      if(ensembleTitleUnique){
        val articleIds = saveEnsembleData.articleIndices.map { articleImages.getArticleId(it) }
        _showAddEnsembleDialog.value = false
        ensemblesRepository.insertEnsemble(
          saveEnsembleData.title,
          articleIds,
        )
      } else _ensembleTitleError.value = R.string.ensemble_with_title_already_exists
    }
  }
  fun onClickEnsemble(index: Int): String = ensembles[index].ensemble.id
  fun onSearchQueryUpdate(query: String) {
    searchQuery = query
    searchEnsemblesQuery.tryEmit(if(query.isEmpty()) "" else "$query*")
  }
  fun deleteEnsembles(ensembleIndices: List<Int>) {
    val ensembleIds = ensembles.slice(ensembleIndices).map { it.ensemble.id }
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.deleteEnsembles(ensembleIds)
    }
  }

  fun onClickAddEnsemble() { _showAddEnsembleDialog.value = true }
  fun onClickCloseAddEnsembleDialog() { _showAddEnsembleDialog.value = false }
}