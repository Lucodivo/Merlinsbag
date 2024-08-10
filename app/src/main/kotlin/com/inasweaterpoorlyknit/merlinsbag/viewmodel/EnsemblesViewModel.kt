@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
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

@HiltViewModel
class EnsemblesViewModel @Inject constructor(
    articleRepository: ArticleRepository,
    val ensemblesRepository: EnsembleRepository,
): ViewModel() {

  enum class DialogState{
    None,
    AddEnsemble,
    DeleteEnsembleAlert,
  }

  companion object {
    const val MAX_ENSEMBLE_TITLE_LENGTH = 256
  }

  private lateinit var articleImages: LazyArticleThumbnails
  private lateinit var ensembles: List<LazyEnsembleThumbnails>
  private var searchEnsemblesQuery: MutableSharedFlow<String> = MutableStateFlow("")

  var showPlaceholder by mutableStateOf(false)
  var dialogState by mutableStateOf(DialogState.None)
  var ensembleTitleError by mutableStateOf<Int?>(null)
  var editMode by mutableStateOf(false)
  var searchQuery by mutableStateOf("")
  var newEnsembleTitle by mutableStateOf("")
  var selectedNewEnsembleArticles = mutableStateSetOf<Int>()
  val onBackEnabled get() = editMode

  val selectedEnsembleIndices = mutableStateSetOf<Int>()

  var navigateToEnsembleDetail by mutableStateOf(Event<String>(null))

  val addArticleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { articleImages = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = LazyUriStrings.Empty
      )

  val lazyEnsembles: StateFlow<List<Pair<String, LazyUriStrings>>> =
      combine(
        ensemblesRepository.getAllEnsembleArticleThumbnails().onEach {
          if(it.isEmpty()){
            if(!showPlaceholder) showPlaceholder = true
          } else if(showPlaceholder) {
            showPlaceholder = false
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

  private fun dismissDialog() {
    if(dialogState != DialogState.None) dialogState = DialogState.None
  }

  fun onClickAddEnsemble() { dialogState = DialogState.AddEnsemble }
  fun onClickCloseAddEnsembleDialog() = dismissDialog()

  fun onClickMinimizeButtonControl() {
    editMode = false
    if(selectedEnsembleIndices.isNotEmpty()) selectedEnsembleIndices.clear()
  }

  fun onSearchQueryClear() { onSearchQueryUpdate("") }

  fun onLongPressEnsemble(index: Int){
    if(!editMode) {
      editMode = true
      selectedEnsembleIndices.clear()
    }
    toggleSelectedEnsemble(index)
  }

  fun onClickEnsemble(index: Int) =
    if(editMode) toggleSelectedEnsemble(index)
    else navigateToEnsembleDetail = Event(ensembles[index].ensemble.id)

  fun onUpdateNewEnsembleTitle(newTitle: String){
    if(newTitle.length <= MAX_ENSEMBLE_TITLE_LENGTH) newEnsembleTitle = newTitle
  }

  fun onClickNewEnsembleArticle(articleIndex: Int) =
    if(selectedNewEnsembleArticles.contains(articleIndex)) {
      selectedNewEnsembleArticles.remove(articleIndex)
    } else selectedNewEnsembleArticles.add(articleIndex)

  fun onClickSaveAddEnsembleDialog() {
    val articleIds = selectedNewEnsembleArticles.map { articleImages.getArticleId(it) }
    val title = newEnsembleTitle
    newEnsembleTitle = ""
    selectedNewEnsembleArticles.clear()
    viewModelScope.launch(Dispatchers.IO) {
      val ensembleTitleUnique = ensemblesRepository.isEnsembleTitleUnique(title).first()
      if(ensembleTitleUnique){
        dismissDialog()
        ensemblesRepository.insertEnsemble(
          title,
          articleIds,
        )
      } else ensembleTitleError = R.string.ensemble_with_title_already_exists
    }
  }

  fun onSearchQueryUpdate(query: String) {
    editMode = false
    searchQuery = query
    searchEnsemblesQuery.tryEmit(if(query.isEmpty()) "" else "$query*")
  }

  fun onBack() = onClickMinimizeButtonControl()

  fun onClickDeleteSelectedEnsembles() {
    dialogState = DialogState.DeleteEnsembleAlert
  }
  fun onDismissDeleteEnsemblesAlertDialog() = dismissDialog()
  fun onDeleteEnsemblesAlertDialogPositive() {
    dismissDialog()
    editMode = false
    val ensembleIds = selectedEnsembleIndices.map { ensembles[it].ensemble.id }
    viewModelScope.launch(Dispatchers.IO) { ensemblesRepository.deleteEnsembles(ensembleIds) }
  }

  private fun toggleSelectedEnsemble(index: Int){
    if(selectedEnsembleIndices.contains(index)) selectedEnsembleIndices.remove(index)
    else selectedEnsembleIndices.add(index)
    if(selectedEnsembleIndices.isEmpty()) editMode = false
  }
}