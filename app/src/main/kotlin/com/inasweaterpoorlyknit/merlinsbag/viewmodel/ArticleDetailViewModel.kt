@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.common.listMap
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

@HiltViewModel(assistedFactory = ArticleDetailViewModel.ArticleDetailViewModelFactory::class)
class ArticleDetailViewModel @AssistedInject constructor(
    @Assisted("ensembleId") private val ensembleId: String?,
    @Assisted("articleIndex") var articleIndex: Int,
    val articleRepository: ArticleRepository,
    val ensembleRepository: EnsembleRepository,
): ViewModel() {

  data class ArticleEnsembleState(
      val articleEnsembleTitles: List<String>,
      val searchEnsembles: List<Ensemble>,
      val searchIsUniqueTitle: Boolean,
  )

  enum class EditState {
    EnabledGeneral,
    EnabledSelectedEnsembles,
    EnabledSelectedThumbnails,
    EnabledAllThumbnails,
    Disabled,
  }

  enum class AlertDialogState {
    DeleteArticle,
    DeleteArticleByRemovingAllArticles,
    ExportPermissions,
    RemoveFromEnsembles,
    RemoveImages,
    None,
  }

  sealed interface NavigationState {
    data object Back: NavigationState
    data object SystemAppSettings: NavigationState
    data object PhotoAlbum: NavigationState
    data class Camera(val articleId: String): NavigationState
    data class EnsembleDetail(val ensembleId: String): NavigationState
    data class AddArticle(val uriStrings: List<String>, val articleId: String): NavigationState
  }

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

  val selectedThumbnails = mutableStateSetOf<Int>()
  val selectedEnsembles = mutableStateSetOf<Int>()
  val articleBeingExported = mutableStateSetOf<Int>()
  val newlyAddedEnsembles = mutableStateSetOf<String>()

  var editState by mutableStateOf(EditState.Disabled)
  var alertDialogState by mutableStateOf(AlertDialogState.None)
  var showAddToEnsemblesDialog by mutableStateOf(false)

  var exportedImage by mutableStateOf(Event<Uri>(null))
  var navigationEventState by mutableStateOf(Event<NavigationState>(null))

  var articleId by mutableStateOf<String?>(null)
  var ensemblesSearchQuery by mutableStateOf("")
  var articleImageIndices = mutableStateListOf<Int>()

  val exportButtonEnabled get() = !articleBeingExported.contains(articleIndex)

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

  val ensembleUiState: StateFlow<ArticleEnsembleState> = combine(
    _articleId.flatMapLatest {
      ensembleRepository.getEnsemblesByArticle(it)
    }.onEach { ensembles ->
      cachedArticleEnsembles = ensembles
      cachedArticleEnsembleIdSet = ensembles.map { it.id }.toSet()
    }.listMap { it.title },
    _ensemblesSearchQuery.flatMapLatest { searchQuery ->
      if(searchQuery.isEmpty()) { flowOf(Pair(emptyList(), false)) }
      else ensembleRepository.searchAllEnsembles(searchQuery).zip(ensembleRepository.isEnsembleTitleUnique(searchQuery)){ searchEnsembles, unique ->
        Pair(searchEnsembles, unique)
      }
    },
    ensembleRepository.getAllEnsembles().onEach { cachedAllEnsembles = it },
  ) { articleEnsembles, searchResults, allEnsembles ->
    val searchEnsembles: List<Ensemble> = if(ensemblesSearchQuery.isEmpty()) allEnsembles else searchResults.first
    ArticleEnsembleState(
      articleEnsembleTitles = articleEnsembles,
      searchEnsembles = searchEnsembles.filter { !cachedArticleEnsembleIdSet.contains(it.id) },
      searchIsUniqueTitle = searchResults.second
    )
  }.stateIn(
    scope = viewModelScope,
    initialValue = ArticleEnsembleState(emptyList(), emptyList(), false),
    started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS)
  )

  fun dismissAlertDialog() {
    if(alertDialogState != AlertDialogState.None) alertDialogState = AlertDialogState.None
  }

  fun onClickEdit() { editState = EditState.EnabledGeneral }
  fun onClickMinimizeButtonControl() { editState = EditState.Disabled }
  fun onClickCamera() = articleId?.let{ navigationEventState = Event(NavigationState.Camera(it)) }

  fun onClickAddToEnsemble() { showAddToEnsemblesDialog = true }
  fun onCloseAddToEnsembleDialog() {
    showAddToEnsemblesDialog = false
    searchEnsembles("")
    newlyAddedEnsembles.clear()
  }

  fun onClickDelete() { alertDialogState = AlertDialogState.DeleteArticle }
  fun onDismissDeleteArticleDialog() = dismissAlertDialog()
  fun onConfirmDeleteArticleDialog() {
    dismissAlertDialog()
    selectedEnsembles.clear()
    selectedThumbnails.clear()
    val articleId = cachedArticlesWithFullImages.getArticleId(articleIndex)
    viewModelScope.launch(Dispatchers.IO) {
      articleRepository.deleteArticle(articleId)
    }
    if(lazyArticleFilenames.value.size == 1) navigationEventState = Event(NavigationState.Back)
  }

  fun onClickRemoveFromEnsembles() { if(selectedEnsembles.isNotEmpty()) { alertDialogState = AlertDialogState.RemoveFromEnsembles } }
  fun onDismissRemoveFromEnsemblesArticleDialog() = dismissAlertDialog()
  fun onConfirmRemoveFromEnsemblesArticleDialog() {
    val articleId = cachedArticlesWithFullImages.getArticleId(articleIndex)
    val articleEnsembleIds = selectedEnsembles.toList().map { cachedArticleEnsembles[it].id }
    dismissAlertDialog()
    selectedEnsembles.clear()
    ensembleListState = LazyListState()
    editState = EditState.EnabledGeneral
    viewModelScope.launch(Dispatchers.IO) {
      ensembleRepository.deleteEnsemblesFromArticle(articleId, articleEnsembleIds)
    }
  }

  fun onClickRemoveImages() {
    if(selectedThumbnails.isNotEmpty()) {
      alertDialogState = if(selectedThumbnails.size == cachedArticlesWithFullImages.articleWithImages[articleIndex].imagePaths.size){
        AlertDialogState.DeleteArticleByRemovingAllArticles
      } else {
        AlertDialogState.RemoveImages
      }
    }
  }
  fun onDismissRemoveImagesArticleDialog() = dismissAlertDialog()
  fun onConfirmRemoveImagesArticleDialog() {
    dismissAlertDialog()
    val articleImageFilenamesThumb = selectedThumbnails.toList().map {
      cachedArticlesWithFullImages.articleWithImages[articleIndex].imagePaths[it].filenameThumb
    }
    selectedThumbnails.clear()
    thumbnailAltsListState = LazyListState()
    editState = EditState.EnabledGeneral
    viewModelScope.launch(Dispatchers.IO) {
      articleRepository.deleteArticleImages(articleImageFilenamesThumb)
    }
  }

  fun neverAskExportPermissionAgain() { alertDialogState = AlertDialogState.ExportPermissions }
  fun onDismissExportPermissionsArticleDialog() = dismissAlertDialog()
  fun onConfirmExportPermissionsArticleDialog() {
    dismissAlertDialog()
    navigationEventState = Event(NavigationState.SystemAppSettings)
  }

  fun onArticleFocus(index: Int) = viewModelScope.launch {
    articleIndex = index
    selectedEnsembles.clear()
    selectedThumbnails.clear()
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
    articleBeingExported.add(articleIndex)
    val imageUri = cachedArticlesWithFullImages.lazyFullImageUris.getUriStrings(articleIndex)[thumbnailIndex]
    viewModelScope.launch(Dispatchers.Default) {
      val exportedImageUri = articleRepository.exportImage(imageUri)
      exportedImageUri?.let {
        articleBeingExported.remove(articleIndex)
        exportedImage = Event(it)
      }
    }
  }

  fun onClickArticleThumbnail(thumbnailIndex: Int) {
    articleImageIndices[articleIndex] = thumbnailIndex
    if(editState == EditState.EnabledSelectedThumbnails || editState == EditState.EnabledAllThumbnails) {
      if(selectedThumbnails.contains(thumbnailIndex)) {
        selectedThumbnails.remove(thumbnailIndex)
        if(selectedThumbnails.isEmpty()) editState = EditState.EnabledGeneral
      } else selectedThumbnails.add(thumbnailIndex)
    }
  }

  fun onLongPressArticleThumbnail(thumbnailIndex: Int) {
    articleImageIndices[articleIndex] = thumbnailIndex
    if(editState != EditState.EnabledSelectedThumbnails){
      if(selectedThumbnails.isNotEmpty()) selectedThumbnails.clear()
    }
    if(selectedThumbnails.contains(thumbnailIndex)) {
      selectedThumbnails.remove(thumbnailIndex)
      if(selectedThumbnails.isEmpty()) editState = EditState.EnabledGeneral
    } else selectedThumbnails.add(thumbnailIndex)
    if(selectedThumbnails.size == cachedArticlesWithFullImages.size) {
      editState = EditState.EnabledAllThumbnails
    } else if(editState != EditState.EnabledSelectedThumbnails){
      editState = EditState.EnabledSelectedThumbnails
    }
  }

  fun searchEnsembles(query: String) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesSearchQuery = query
    _ensemblesSearchQuery.emit(query)
  }

  fun addArticleToNewEnsemble(title: String) {
    val articleId: String = cachedArticlesWithFullImages.getArticleId(articleIndex)
    newlyAddedEnsembles.add(title)
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
    if(editState == EditState.EnabledSelectedEnsembles){
      if(selectedEnsembles.contains(ensembleIndex)) {
        selectedEnsembles.remove(ensembleIndex)
        if(selectedEnsembles.isEmpty()) editState = EditState.EnabledGeneral
      } else selectedEnsembles.add(ensembleIndex)
    } else navigationEventState = Event(NavigationState.EnsembleDetail(cachedArticleEnsembles[ensembleIndex].id))
  }

  fun onLongPressEnsemble(ensembleIndex: Int) {
    if(editState != EditState.EnabledSelectedEnsembles){
      if(selectedEnsembles.isNotEmpty()) selectedEnsembles.clear()
      editState = EditState.EnabledSelectedEnsembles
    }
    if(selectedEnsembles.contains(ensembleIndex)) {
      selectedEnsembles.remove(ensembleIndex)
      if(selectedEnsembles.isEmpty()) editState = EditState.EnabledGeneral
    } else selectedEnsembles.add(ensembleIndex)
  }

  fun onClickCancelSelection() {
    if(editState == EditState.EnabledSelectedEnsembles) {
      if(selectedEnsembles.isNotEmpty()) selectedEnsembles.clear()
    } else {
      if(selectedThumbnails.isNotEmpty()) selectedThumbnails.clear()
    }
    editState = EditState.Disabled
  }

  fun onBack() {
    if(editState != EditState.Disabled) onClickMinimizeButtonControl() else navigationEventState = Event(NavigationState.Back)
  }

  fun onPhotoAlbumResult(uris: List<Uri>) {
    val articleId = articleId
    if(uris.isNotEmpty() && articleId != null){
      navigationEventState = Event(NavigationState.AddArticle(uris.map { it.toString() }, articleId))
    }
  }

  fun onClickAddPhotoFromAlbum() { navigationEventState = Event(NavigationState.PhotoAlbum) }
}