@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.inasweaterpoorlyknit.merlinsbag.Constants.Companion.MAX_ENSEMBLE_TITLE_LENGTH
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEffect.NavigateToEnsembleDetail
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEffect.NavigateToSettings
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.Back
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ClickAddEnsemble
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ClickCloseAddEnsembleDialog
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ClickDeleteSelectedEnsembles
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ClickEnsemble
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ClickMinimizeButtonControl
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ClickNewEnsembleArticle
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ClickSaveAddEnsembleDialog
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ClickSettings
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.ConfirmDeleteEnsemblesAlertDialog
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.DismissDeleteEnsemblesAlertDialog
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.LongPressEnsemble
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.UpdateSearchQuery
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIState.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EnsemblesUIEffect {
  data class NavigateToEnsembleDetail(val ensembleId: String): EnsemblesUIEffect
  data object NavigateToSettings: EnsemblesUIEffect
}

data class EnsemblesUIState(
  val showPlaceholder: Boolean,
  val dialogState: DialogState,
  val editMode: Boolean,
  @StringRes val newEnsembleTitleError: Int?,
  val selectedNewEnsembleArticles: Set<Int>,
  val selectedEnsembleIndices: Set<Int>,
  val addArticleThumbnails: LazyUriStrings,
  val lazyEnsembles: List<Pair<String, LazyUriStrings>>
) {
  val handleBackPress: Boolean
    get() = editMode
  enum class DialogState{
    None,
    AddEnsemble,
    DeleteEnsembleAlert,
  }
}

sealed interface EnsemblesUIEvent {
  data object ClickAddEnsemble: EnsemblesUIEvent
  data object ClickCloseAddEnsembleDialog: EnsemblesUIEvent
  data object ClickMinimizeButtonControl: EnsemblesUIEvent
  data object Back: EnsemblesUIEvent
  data object ClickDeleteSelectedEnsembles: EnsemblesUIEvent
  data object DismissDeleteEnsemblesAlertDialog: EnsemblesUIEvent
  data object ConfirmDeleteEnsemblesAlertDialog: EnsemblesUIEvent
  data object ClickSettings: EnsemblesUIEvent
  data class LongPressEnsemble(val index: Int): EnsemblesUIEvent
  data class ClickEnsemble(val index: Int): EnsemblesUIEvent
  data class ClickSaveAddEnsembleDialog(val newTitle: String): EnsemblesUIEvent
  data class ClickNewEnsembleArticle(val articleIndex: Int): EnsemblesUIEvent
  data class UpdateSearchQuery(val query: String): EnsemblesUIEvent
}

@HiltViewModel
class EnsemblesComposeViewModel @Inject constructor(ensemblesPresenter: EnsemblesUIStateManager)
  : MoleculeViewModel<EnsemblesUIEvent, EnsemblesUIState, EnsemblesUIEffect>(uiStateManager = ensemblesPresenter)

class EnsemblesUIStateManager @Inject constructor(
    val articleRepository: ArticleRepository,
    val ensembleRepository: EnsembleRepository,
): ComposeUIStateManager<EnsemblesUIEvent, EnsemblesUIState, EnsemblesUIEffect> {
  override var cachedState = EnsemblesUIState(
    showPlaceholder = false,
    dialogState = DialogState.None,
    editMode = false,
    newEnsembleTitleError = null,
    selectedNewEnsembleArticles = emptySet(),
    selectedEnsembleIndices = emptySet(),
    addArticleThumbnails = LazyUriStrings.Empty,
    lazyEnsembles = emptyList(),
  )

  private val _searchQuery = MutableStateFlow("")

  @Composable
  override fun uiState(
      uiEvents: Flow<EnsemblesUIEvent>,
      launchUiEffect: (EnsemblesUIEffect) -> Unit
  ): EnsemblesUIState {
    var dialogState by remember { mutableStateOf(DialogState.None) }
    var editMode by remember { mutableStateOf(false) }
    var newEnsembleTitleError by remember { mutableStateOf<Int?>(null) }
    val selectedNewEnsembleArticles = remember { mutableStateSetOf<Int>() }
    val selectedEnsembleIndices = remember { mutableStateSetOf<Int>() }

    val ensembleCount by remember { ensembleRepository.getCountEnsembles() }.collectAsState(-1)

    var addEnsembleTriggered by remember { mutableStateOf(false) }
    val addArticleThumbnails by remember(addEnsembleTriggered) {
      if(addEnsembleTriggered) articleRepository.getAllArticlesWithThumbnails()
      else emptyFlow()
    }.collectAsState(LazyArticleThumbnails.Empty)

    val searchQuery by remember { _searchQuery }.collectAsState("")
    val allEnsembleArticles = remember {
      ensembleRepository.getAllEnsembleArticleThumbnails()
    }.collectAsState(emptyList())
    val searchEnsembleArticles = remember {
      _searchQuery
          .debounce(250L)
          .flatMapLatest { searchQuery ->
            if(searchQuery.isEmpty()) flowOf(emptyList())
            else ensembleRepository.searchEnsembleArticleThumbnails("$searchQuery*")
          }
    }.collectAsState(emptyList())
    val ensembleArticleThumbnails =
      if(searchQuery.isEmpty()) allEnsembleArticles
      else searchEnsembleArticles

    val ensembleNamesAndThumbnailUris = remember(ensembleArticleThumbnails.value) {
      ensembleArticleThumbnails.value.map { ensembleThumbnails ->
        Pair(ensembleThumbnails.ensemble.title, ensembleThumbnails.thumbnails)
      }
    }

    LaunchedEffect(Unit) {
      fun getEnsembleId(index: Int) = ensembleArticleThumbnails.value[index].ensemble.id
      fun toggleSelectedEnsemble(index: Int) {
        if(selectedEnsembleIndices.contains(index)) selectedEnsembleIndices.remove(index)
        else selectedEnsembleIndices.add(index)
        if(selectedEnsembleIndices.isEmpty()) editMode = false
      }
      fun exitEditMode() {
        editMode = false
        selectedEnsembleIndices.clear()
      }
      uiEvents.collect { uiEvent ->
        when(uiEvent) {
          ClickAddEnsemble -> {
            addEnsembleTriggered = true
            dialogState = DialogState.AddEnsemble
          }
          ClickCloseAddEnsembleDialog -> {
            dialogState = DialogState.None
            newEnsembleTitleError = null
            selectedNewEnsembleArticles.clear()
          }
          DismissDeleteEnsemblesAlertDialog -> {
            dialogState = DialogState.None
          }
          Back -> exitEditMode()
          ClickDeleteSelectedEnsembles -> {
            dialogState = DialogState.DeleteEnsembleAlert
          }
          is ClickEnsemble -> {
            if(editMode) toggleSelectedEnsemble(uiEvent.index)
            else {
              val ensembleId = getEnsembleId(uiEvent.index)
/*
                if(searchQuery.isEmpty()) { allEnsembleArticles[uiEvent.index].ensemble.id }
                else { searchEnsembleArticles[uiEvent.index].ensemble.id }
*/
              launchUiEffect(NavigateToEnsembleDetail(ensembleId))
            }
          }
          ClickMinimizeButtonControl -> exitEditMode()
          is ClickNewEnsembleArticle -> {
            if(selectedNewEnsembleArticles.contains(uiEvent.articleIndex)) {
              selectedNewEnsembleArticles.remove(uiEvent.articleIndex)
            } else selectedNewEnsembleArticles.add(uiEvent.articleIndex)
          }
          is ClickSaveAddEnsembleDialog -> {
            val articleIds = selectedNewEnsembleArticles.map { addArticleThumbnails.getArticleId(it) }
            val title = uiEvent.newTitle
            launch(Dispatchers.IO) {
              val ensembleTitleUnique = ensembleRepository.isEnsembleTitleUnique(title).first()
              if(ensembleTitleUnique){
                dialogState = DialogState.None
                newEnsembleTitleError = null
                selectedNewEnsembleArticles.clear()
                ensembleRepository.insertEnsemble(
                  title,
                  articleIds,
                )
              } else newEnsembleTitleError = R.string.ensemble_with_title_already_exists
            }
          }
          ConfirmDeleteEnsemblesAlertDialog -> {
            dialogState = DialogState.None
            editMode = false
            val ensembleIds =
                selectedEnsembleIndices.map {
                  if(searchQuery.isEmpty()) allEnsembleArticles.value[it].ensemble.id
                  else searchEnsembleArticles.value[it].ensemble.id
                }
            launch(Dispatchers.IO) { ensembleRepository.deleteEnsembles(ensembleIds) }
          }
          is LongPressEnsemble -> {
            if(!editMode) {
              editMode = true
              selectedEnsembleIndices.clear()
            }
            toggleSelectedEnsemble(uiEvent.index)
          }
          is UpdateSearchQuery -> {
            editMode = false
            _searchQuery.value = uiEvent.query
          }
          ClickSettings -> launchUiEffect(NavigateToSettings)
        }
      }
    }

    return EnsemblesUIState(
      showPlaceholder = ensembleCount == 0,
      dialogState = dialogState,
      editMode = editMode,
      newEnsembleTitleError = newEnsembleTitleError,
      selectedNewEnsembleArticles = selectedNewEnsembleArticles,
      selectedEnsembleIndices = selectedEnsembleIndices,
      addArticleThumbnails = addArticleThumbnails,
      lazyEnsembles = ensembleNamesAndThumbnailUris,
    )
  }
}