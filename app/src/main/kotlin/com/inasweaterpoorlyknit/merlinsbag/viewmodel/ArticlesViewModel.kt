package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
): ViewModel() {

  enum class EditState {
    ENABLED_GENERAL,
    ENABLED_SELECTED_ARTICLES,
    DISABLED
  }

  sealed interface NavigationState {
    data object Camera: NavigationState
    data object PhotoAlbum: NavigationState
    data object Settings: NavigationState
    data class ArticleDetail(val index: Int): NavigationState
    data class AddArticle(val uriStrings: List<String>): NavigationState
  }

  private lateinit var lazyArticleImagesCache: LazyArticleThumbnails

  var showDeleteArticlesAlert by mutableStateOf(false)
  var editState by mutableStateOf(EditState.DISABLED)
  val onBackEnabled get() = editState != EditState.DISABLED
  val selectedArticleIndices = mutableStateSetOf<Int>()
  var navigationEventState by mutableStateOf(Event<NavigationState>(null))
  val articleThumbnails: StateFlow<LazyUriStrings?> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { lazyArticleImagesCache = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = null,
      )

  fun onClickArticle(index: Int){
    if(editState == EditState.ENABLED_SELECTED_ARTICLES) {
      if(selectedArticleIndices.contains(index)) {
        selectedArticleIndices.remove(index)
        if(selectedArticleIndices.isEmpty()) editState = EditState.ENABLED_GENERAL
      } else selectedArticleIndices.add(index)
    } else {
      navigationEventState = Event(NavigationState.ArticleDetail(index))
    }
  }

  fun onLongPressArticle(index: Int) {
    if(editState != EditState.ENABLED_SELECTED_ARTICLES) {
      editState = EditState.ENABLED_SELECTED_ARTICLES
      selectedArticleIndices.clear()
    }
    if(selectedArticleIndices.contains(index)) {
      selectedArticleIndices.remove(index)
      if(selectedArticleIndices.isEmpty()) editState = EditState.ENABLED_GENERAL
    } else selectedArticleIndices.add(index)
  }

  fun onClickClearSelection() {
    selectedArticleIndices.clear()
    editState = EditState.ENABLED_GENERAL
  }
  fun onClickSettings(){ navigationEventState = Event(NavigationState.Settings) }
  fun onClickAddPhotoCamera(){ navigationEventState = Event(NavigationState.Camera) }
  fun onClickAddPhotoAlbum(){ navigationEventState = Event(NavigationState.PhotoAlbum) }
  fun onPhotoAlbumResults(uris: List<Uri>){
    if(uris.isNotEmpty()) navigationEventState = Event(NavigationState.AddArticle(uris.map{ it.toString() }))
  }

  fun onClickEdit(){ editState = EditState.ENABLED_GENERAL }
  fun onClickMinimizeButtonControl(){
    editState = EditState.DISABLED
    if(selectedArticleIndices.isNotEmpty()) selectedArticleIndices.clear()
  }

  fun onClickDelete(){ showDeleteArticlesAlert = true  }
  fun onDismissDeleteArticlesAlert() { showDeleteArticlesAlert = false }
  fun onConfirmDeleteArticlesAlert() {
    showDeleteArticlesAlert = false
    editState = EditState.ENABLED_GENERAL
    val articleIds = selectedArticleIndices.map { lazyArticleImagesCache.getArticleId(it) }
    selectedArticleIndices.clear()
    viewModelScope.launch(Dispatchers.IO) { articleRepository.deleteArticles(articleIds) }
  }

  fun onBack() {
    if(editState != EditState.DISABLED) editState = EditState.DISABLED
  }
}