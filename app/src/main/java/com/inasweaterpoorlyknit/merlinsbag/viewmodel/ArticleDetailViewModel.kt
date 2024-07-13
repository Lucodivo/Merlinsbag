@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
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
    @Assisted("articleIndex") var articleIndex: Int,
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

  // TODO: No mutableStateSetOf ??
  private val _selectedThumbnails = mutableStateMapOf<Int, Unit>()
  private val _selectedEnsembles = mutableStateMapOf<Int, Unit>()
  private val _articleBeingExported = mutableStateMapOf<Int, Unit>()
  private val _newlyAddedEnsembles = mutableStateMapOf<String, Unit>()

  var editMode by mutableStateOf(false)
  var showDeleteArticleAlertDialog by mutableStateOf(false)
  var showExportPermissionsAlertDialog by mutableStateOf(false)
  var showRemoveFromEnsemblesAlertDialog by mutableStateOf(false)
  var showRemoveImagesAlertDialog by mutableStateOf(false)
  var showAddToEnsemblesDialog by mutableStateOf(false)

  var exportedImage by mutableStateOf(Event<Uri>(null))
  var finished by mutableStateOf(Event<Unit>(null))
  var launchSettings by mutableStateOf(Event<Unit>(null))
  var navigateToCamera by mutableStateOf(Event<String>(null))
  var navigateToEnsembleDetail by mutableStateOf(Event<String>(null))

  var articleId by mutableStateOf<String?>(null)
  var ensemblesSearchQuery by mutableStateOf("")
  var articleImageIndices = mutableStateListOf<Int>()

  val selectedEnsembles get() = _selectedEnsembles.keys
  val selectedThumbnails get() = _selectedThumbnails.keys
  val newlyAddedEnsembles get() = _newlyAddedEnsembles.keys
  val exportButtonEnabled get() = !_articleBeingExported.containsKey(articleIndex)

  var ensembleListState by mutableStateOf(LazyListState())
  var thumbnailAltsListState by mutableStateOf(LazyListState())

  private val _articleId = MutableSharedFlow<String>()
  private val _ensemblesSearchQuery = MutableSharedFlow<String>(replay = 1).apply { tryEmit(ensemblesSearchQuery) }

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
        articleImageIndices = mutableStateListOf(*Array(images.size) { 0 })
        cachedArticlesWithFullImages = images
        // Note: If deleting the last article in the list, it can cause the articleIndex to be out of bounds
        if(articleIndex < images.size){
          val id = images.getArticleId(articleIndex)
          articleId = id
          if(articleIndex < images.size) _articleId.emit(id)
        }
      }.stateIn(
        scope = viewModelScope,
        initialValue = LazyFilenames.Empty,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS)
      )

  val ensembleUiState: StateFlow<ArticleEnsembleUiState> = combine(
    _articleId.flatMapLatest { ensembleRepository.getEnsemblesByArticle(it) }
        .onEach { ensembles ->
          cachedArticleEnsembles = ensembles
          cachedArticleEnsembleIdSet = ensembles.map { it.id }.toSet()
        },
    _ensemblesSearchQuery.flatMapLatest { searchQuery ->
      if(searchQuery.isEmpty()) { flowOf(Pair(emptyList(), false)) }
      else ensembleRepository.searchAllEnsembles(searchQuery).zip(ensembleRepository.isEnsembleTitleUnique(searchQuery)){ searchEnsembles, unique ->
        Pair(searchEnsembles, unique)
      }
    },
    ensembleRepository.getAllEnsembles().onEach { cachedAllEnsembles = it },
  ) { articleEnsembles, searchResults, allEnsembles ->
    val searchEnsembles: List<Ensemble> = if(ensemblesSearchQuery.isEmpty()) allEnsembles else searchResults.first
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

  fun onClickEdit() {
    if(editMode) {
      _selectedEnsembles.clear()
      _selectedThumbnails.clear()
    }
    editMode = !editMode
  }
  fun onClickCamera() { navigateToCamera = Event(articleId) }

  fun onClickAddToEnsemble() { showAddToEnsemblesDialog = true }
  fun onCloseAddToEnsembleDialog() {
    showAddToEnsemblesDialog = false
    searchEnsembles("")
    _newlyAddedEnsembles.clear()
  }

  fun onClickDelete() { showDeleteArticleAlertDialog = true }
  fun onDismissDeleteArticleDialog() { showDeleteArticleAlertDialog = false }
  fun onConfirmDeleteArticleDialog() {
    showDeleteArticleAlertDialog = false
    _selectedEnsembles.clear()
    _selectedThumbnails.clear()
    val articleId = cachedArticlesWithFullImages.getArticleId(articleIndex)
    viewModelScope.launch(Dispatchers.IO) {
      articleRepository.deleteArticle(articleId)
    }
    if(lazyArticleFilenames.value.size == 1) finished = Event(Unit)
  }

  fun onClickRemoveFromEnsembles() { showRemoveFromEnsemblesAlertDialog = true }
  fun onDismissRemoveFromEnsemblesArticleDialog() { showRemoveFromEnsemblesAlertDialog = false }
  fun onConfirmRemoveFromEnsemblesArticleDialog() {
    val articleId = cachedArticlesWithFullImages.getArticleId(articleIndex)
    val articleEnsembleIds = _selectedEnsembles.keys.toList().map { cachedArticleEnsembles[it].id }
    showRemoveFromEnsemblesAlertDialog = false
    _selectedEnsembles.clear()
    ensembleListState = LazyListState()
    viewModelScope.launch(Dispatchers.IO) {
      ensembleRepository.deleteEnsemblesFromArticle(articleId, articleEnsembleIds)
    }
  }

  fun onClickRemoveImages() { showRemoveImagesAlertDialog= true }
  fun onDismissRemoveImagesArticleDialog() { showRemoveImagesAlertDialog = false }
  fun onConfirmRemoveImagesArticleDialog() {
    showRemoveImagesAlertDialog = false
    val articleImageFilenamesThumb = _selectedThumbnails.keys.toList().map {
      cachedArticlesWithFullImages.articleWithImages[articleIndex].imagePaths[it].filenameThumb
    }
    _selectedThumbnails.clear()
    thumbnailAltsListState = LazyListState()
    viewModelScope.launch(Dispatchers.IO) {
      articleRepository.deleteArticleImages(articleImageFilenamesThumb)
    }
  }

  fun neverAskExportPermissionAgain() { showExportPermissionsAlertDialog = true }
  fun onDismissExportPermissionsArticleDialog() { showExportPermissionsAlertDialog = false }
  fun onConfirmExportPermissionsArticleDialog() {
    showExportPermissionsAlertDialog = false
    launchSettings = Event(Unit)
  }

  fun onArticleFocus(index: Int) = viewModelScope.launch {
    articleIndex = index
    _selectedEnsembles.clear()
    _selectedThumbnails.clear()
    ensembleListState = LazyListState()
    thumbnailAltsListState = LazyListState()
    if(ensemblesSearchQuery.isNotEmpty()) searchEnsembles("")
    if(index < cachedArticlesWithFullImages.size) {
      val id = cachedArticlesWithFullImages.getArticleId(index)
      articleId = id
      _articleId.emit(id)
    }
  }

  fun onExportPermissionsGranted() {
    val thumbnailIndex = articleImageIndices[articleIndex]
    _articleBeingExported[articleIndex] = Unit
    val imageUri = cachedArticlesWithFullImages.lazyFullImageUris.getUriStrings(articleIndex)[thumbnailIndex]
    viewModelScope.launch(Dispatchers.Default) {
      val exportedImageUri = articleRepository.exportImage(imageUri)
      exportedImageUri?.let {
        _articleBeingExported.remove(articleIndex)
        exportedImage = Event(it)
      }
    }
  }

  fun onClickArticleThumbnail(thumbnailIndex: Int) {
    articleImageIndices[articleIndex] = thumbnailIndex
    if(_selectedEnsembles.isNotEmpty()) _selectedEnsembles.clear()
    if(editMode) {
      if(_selectedThumbnails.containsKey(thumbnailIndex)) _selectedThumbnails.remove(thumbnailIndex)
      else _selectedThumbnails[thumbnailIndex] = Unit
    }
  }

  fun onLongPressArticleThumbnail(thumbnailIndex: Int) {
    articleImageIndices[articleIndex] = thumbnailIndex
    if(_selectedEnsembles.isNotEmpty()) _selectedEnsembles.clear()
    if(!editMode) {
      editMode = true
      _selectedThumbnails.clear()
    }
    if(_selectedThumbnails.containsKey(thumbnailIndex)) _selectedThumbnails.remove(thumbnailIndex)
    else _selectedThumbnails[thumbnailIndex] = Unit
  }

  fun searchEnsembles(query: String) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesSearchQuery = query
    _ensemblesSearchQuery.emit(query)
  }

  fun addArticleToNewEnsemble(title: String) {
    val articleId: String = cachedArticlesWithFullImages.getArticleId(articleIndex)
    _newlyAddedEnsembles[title] = Unit
    viewModelScope.launch(Dispatchers.IO) {
      val isUnique = ensembleRepository.isEnsembleTitleUnique(title).first()
      if(isUnique) ensembleRepository.insertEnsemble(title, listOf(articleId))
    }
  }

  fun addArticleToEnsemble(ensembleId: String) = viewModelScope.launch(Dispatchers.IO){
    ensembleRepository.addEnsemblesToArticle(
      articleId = cachedArticlesWithFullImages.getArticleId(articleIndex),
      ensembleIds = listOf(ensembleId)
    )
  }

  fun onClickEnsemble(ensembleIndex: Int) {
    if(editMode){
      if(_selectedEnsembles.containsKey(ensembleIndex)) _selectedEnsembles.remove(ensembleIndex)
      else _selectedEnsembles[ensembleIndex] = Unit
      if(_selectedThumbnails.isNotEmpty()) _selectedThumbnails.clear()
    } else navigateToEnsembleDetail = Event(cachedArticleEnsembles[ensembleIndex].id)
  }

  fun onLongPressEnsemble(ensembleIndex: Int) {
    if(!editMode) {
      _selectedEnsembles.clear()
      editMode = true
    }
    if(_selectedEnsembles.containsKey(ensembleIndex)) _selectedEnsembles.remove(ensembleIndex)
    else _selectedEnsembles[ensembleIndex] = Unit
    if(_selectedThumbnails.isNotEmpty()) _selectedThumbnails.clear()
  }

  fun onClickCancelSelection() {
    _selectedEnsembles.clear()
    _selectedThumbnails.clear()
  }

  fun onBack() {
    if(editMode) onClickEdit() else finished = Event(Unit)
  }
}