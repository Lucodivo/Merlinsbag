@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
class EnsemblesOldViewModel @Inject constructor(
    articleRepository: ArticleRepository,
    val ensembleRepository: EnsembleRepository,
): ViewModel() {
  private lateinit var articleImages: LazyArticleThumbnails
  private lateinit var ensembles: List<LazyEnsembleThumbnails>
  private var searchEnsemblesQuery: MutableSharedFlow<String> = MutableStateFlow("")

  var showPlaceholder by mutableStateOf(false)
  var dialogState by mutableStateOf(DialogState.None)
  var editMode by mutableStateOf(false)
  var navigateToEnsembleDetail by mutableStateOf(Event<String>(null))
  var ensembleTitleError by mutableStateOf<Int?>(null)
  var searchQuery by mutableStateOf("")
  var newEnsembleTitle by mutableStateOf("")
  var selectedNewEnsembleArticles = mutableStateSetOf<Int>()
  val selectedEnsembleIndices = mutableStateSetOf<Int>()
  val addArticleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { articleImages = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = LazyUriStrings.Empty
      )
  val lazyEnsembles: StateFlow<List<Pair<String, LazyUriStrings>>> =
      combine(
        ensembleRepository.getAllEnsembleArticleThumbnails().onEach {
          if(it.isEmpty()){
            if(!showPlaceholder) showPlaceholder = true
          } else if(showPlaceholder) {
            showPlaceholder = false
          }
        },
        searchEnsemblesQuery,
        searchEnsemblesQuery.flatMapLatest { query ->
          ensembleRepository.searchEnsembleArticleThumbnails(query)
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
      val ensembleTitleUnique = ensembleRepository.isEnsembleTitleUnique(title).first()
      if(ensembleTitleUnique){
        dismissDialog()
        ensembleRepository.insertEnsemble(
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
    viewModelScope.launch(Dispatchers.IO) { ensembleRepository.deleteEnsembles(ensembleIds) }
  }

  private fun toggleSelectedEnsemble(index: Int){
    if(selectedEnsembleIndices.contains(index)) selectedEnsembleIndices.remove(index)
    else selectedEnsembleIndices.add(index)
    if(selectedEnsembleIndices.isEmpty()) editMode = false
  }
}

// Currently contains at least one bug.
// The search box fails to properly adjust cursor when adding
// characters to the end of the search. Weird.
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

  val _searchQuery = MutableStateFlow("")

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

    val addArticleThumbnails by remember {
      articleRepository.getAllArticlesWithThumbnails()
    }.collectAsState(LazyArticleThumbnails.Empty)

    val searchQuery by remember { _searchQuery }.collectAsState("")
    val allEnsembleArticles by remember {
      ensembleRepository.getAllEnsembleArticleThumbnails()
    }.collectAsState(emptyList())
    val searchEnsembleArticles by remember {
      _searchQuery
          .debounce(250L)
          .flatMapLatest { searchQuery ->
            if(searchQuery.isEmpty()) emptyFlow()
            else ensembleRepository.searchEnsembleArticleThumbnails("$searchQuery*")
          }
    }.collectAsState(emptyList())
    val ensembleArticleThumbnails = remember(searchQuery, allEnsembleArticles, searchEnsembleArticles) {
      if(searchQuery.isEmpty()) allEnsembleArticles
      else searchEnsembleArticles
    }

    val ensembleNamesAndThumbnailUris = remember(ensembleArticleThumbnails) {
      ensembleArticleThumbnails.map { ensembleThumbnails ->
        Pair(ensembleThumbnails.ensemble.title, ensembleThumbnails.thumbnails)
      }
    }

    // TODO: Why does this not work? Index out of bounds exception inside uiEvent.collect{}...
    fun getEnsembleId(index: Int) =
      ensembleArticleThumbnails[index].ensemble.id

    LaunchedEffect(Unit) {
      fun toggleSelectedEnsemble(index: Int) {
        if(selectedEnsembleIndices.contains(index)) selectedEnsembleIndices.remove(index)
        else selectedEnsembleIndices.add(index)
        if(selectedEnsembleIndices.isEmpty()) editMode = false
      }
      fun updateSearchQuery(query: String) {
        editMode = false
        _searchQuery.value = query
      }
      fun exitEditMode() {
        editMode = false
        if(selectedEnsembleIndices.isNotEmpty()) selectedEnsembleIndices.clear()
      }
      uiEvents.collect { uiEvent ->
        when(uiEvent) {
          ClickAddEnsemble -> { dialogState = DialogState.AddEnsemble }
          ClickCloseAddEnsembleDialog -> {
            dialogState = DialogState.None
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
              val ensembleId =
                if(searchQuery.isEmpty()) { allEnsembleArticles[uiEvent.index].ensemble.id }
                else { searchEnsembleArticles[uiEvent.index].ensemble.id }
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
            selectedNewEnsembleArticles.clear()
            launch(Dispatchers.IO) {
              val ensembleTitleUnique = ensembleRepository.isEnsembleTitleUnique(title).first()
              if(ensembleTitleUnique){
                dialogState = DialogState.None
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
                  if(searchQuery.isEmpty()) allEnsembleArticles[it].ensemble.id
                  else searchEnsembleArticles[it].ensemble.id
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
            updateSearchQuery(uiEvent.query)
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
    ).also { cachedState = it }
  }
}

@HiltViewModel
class EnsemblesViewModel @Inject constructor(
    val articleRepository: ArticleRepository,
    val ensembleRepository: EnsembleRepository,
): NoopViewModel2<EnsemblesUIEvent, EnsemblesUIState, EnsemblesUIEffect>() {

  private data class LocallyManagedState (
    val dialogState: DialogState = DialogState.None,
    val editMode: Boolean = false,
    val searchQuery: String = "",
    val newEnsembleTitle: String = "",
    val newEnsembleTitleError: Int? = null,
    val selectedNewEnsembleArticles: Set<Int> = mutableSetOf(),
    val selectedEnsembleIndices: Set<Int> = mutableSetOf(),
  )
  private val locallyManagedState = MutableStateFlow(LocallyManagedState())
  private var cachedEnsembleIds: List<String> = emptyList()
  private var cachedArticleIds: List<String> = emptyList()

  override fun initialUiState() = EnsemblesUIState(
    showPlaceholder = false,
    dialogState = DialogState.None,
    editMode = false,
    newEnsembleTitleError = null,
    selectedNewEnsembleArticles = emptySet(),
    selectedEnsembleIndices = emptySet(),
    addArticleThumbnails = LazyUriStrings.Empty,
    lazyEnsembles = emptyList(),
  )
  override fun uiStateFlow(): Flow<EnsemblesUIState> = combine(
    locallyManagedState,
    articleRepository.getAllArticlesWithThumbnails().onEach { articlesWithThumbnails ->
      cachedArticleIds = articlesWithThumbnails.articleIds()
    },
    ensembleRepository.getCountEnsembles(),
    locallyManagedState
        .map { it.searchQuery }
        .distinctUntilChanged()
        .flatMapLatest { searchQuery ->
          if(searchQuery.isEmpty()){
            ensembleRepository.getAllEnsembleArticleThumbnails()
          } else {
            ensembleRepository
                .searchEnsembleArticleThumbnails("$searchQuery*")
                .debounce(250L)
          }
        }.onEach { lazyEnsembleThumbnails ->
          cachedEnsembleIds = lazyEnsembleThumbnails.map { it.ensemble.id }
        }.listMap {
          Pair(it.ensemble.title, it.thumbnails)
        }
  ) {
    lms: LocallyManagedState,
    lazyArticleThumbnails: LazyArticleThumbnails,
    ensembleCount: Int,
    lazyEnsembles: List<Pair<String, LazyArticleThumbnails>> ->
    EnsemblesUIState(
      showPlaceholder = ensembleCount == 0,
      dialogState = lms.dialogState,
      editMode = lms.editMode,
      newEnsembleTitleError = lms.newEnsembleTitleError,
      selectedNewEnsembleArticles = lms.selectedNewEnsembleArticles,
      selectedEnsembleIndices = lms.selectedEnsembleIndices,
      addArticleThumbnails = lazyArticleThumbnails,
      lazyEnsembles = lazyEnsembles,
    )
  }
  override fun onUiEvent(uiEvent: EnsemblesUIEvent) {
    with(locallyManagedState.value){
      fun dismissDialog() {
        locallyManagedState.update { it.copy(dialogState = DialogState.None) }
      }
      fun toggleSelectedEnsemble(index: Int) {
        val newSelectedEnsembleIndices =
          if(selectedEnsembleIndices.contains(index)) selectedEnsembleIndices - index
          else selectedEnsembleIndices + index
        locallyManagedState.value = copy(
          selectedEnsembleIndices = newSelectedEnsembleIndices,
          editMode = newSelectedEnsembleIndices.isNotEmpty(),
        )
      }
      fun updateSearchQuery(query: String) {
        locallyManagedState.value = copy(
          editMode = false,
          selectedEnsembleIndices = emptySet(),
          searchQuery = query,
        )
      }
      fun exitEditMode() {
        locallyManagedState.value = copy(
          editMode = false,
          selectedEnsembleIndices = emptySet(),
        )
      }
      when(uiEvent) {
        ClickAddEnsemble -> {
          locallyManagedState.value = copy(dialogState = DialogState.AddEnsemble)
        }
        ClickCloseAddEnsembleDialog -> dismissDialog()
        DismissDeleteEnsemblesAlertDialog -> dismissDialog()
        Back -> exitEditMode()
        ClickDeleteSelectedEnsembles -> {
          locallyManagedState.value = copy(dialogState = DialogState.DeleteEnsembleAlert)
        }
        is ClickEnsemble -> {
          if(editMode) toggleSelectedEnsemble(uiEvent.index)
          else launchUiEffect(NavigateToEnsembleDetail(cachedEnsembleIds[uiEvent.index]))
        }
        ClickMinimizeButtonControl -> exitEditMode()
        is ClickNewEnsembleArticle -> {
          locallyManagedState.value = copy(
            selectedNewEnsembleArticles =
              if(selectedNewEnsembleArticles.contains(uiEvent.articleIndex))
                selectedNewEnsembleArticles - uiEvent.articleIndex
              else selectedNewEnsembleArticles + uiEvent.articleIndex
          )
        }
        is ClickSaveAddEnsembleDialog -> {
          viewModelScope.launch(Dispatchers.IO) {
            val newTitle = uiEvent.newTitle
            val ensembleTitleUnique = ensembleRepository.isEnsembleTitleUnique(newTitle).first()
            if(ensembleTitleUnique){
              ensembleRepository.insertEnsemble(
                newTitle,
                selectedNewEnsembleArticles.map { cachedArticleIds[it] },
              )
              locallyManagedState.value = copy(
                newEnsembleTitle = "",
                selectedNewEnsembleArticles = emptySet(),
                dialogState = DialogState.None,
                newEnsembleTitleError = null,
              )
            } else {
              locallyManagedState.value = copy(
                newEnsembleTitleError = R.string.ensemble_with_title_already_exists,
              )
            }
          }
        }
        ConfirmDeleteEnsemblesAlertDialog -> {
          val ensembleIds = selectedEnsembleIndices.map { cachedEnsembleIds[it] }
          locallyManagedState.value = copy(
            dialogState = DialogState.None,
            editMode = false,
            selectedEnsembleIndices = emptySet(),
          )
          viewModelScope.launch(Dispatchers.IO) {
            ensembleRepository.deleteEnsembles(ensembleIds)
          }
        }
        is LongPressEnsemble -> toggleSelectedEnsemble(uiEvent.index)
        is UpdateSearchQuery -> {
          updateSearchQuery(uiEvent.query)
        }
        ClickSettings -> launchUiEffect(NavigateToSettings)
      }
    }
  }
}